package com.budget.controller.modules;

import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.db.Database;
import com.budget.infrastructure.EventBus;
import com.budget.model.Transaction;
import com.budget.modules.finance.events.TransactionAddedEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SettingsController {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * Eksportuje wszystkie transakcje do pliku .csv (Excel)
     */
    @FXML
    public void exportData(javafx.event.ActionEvent event) {
        // Pobieramy Stage (okno) z przycisku, który wywołał akcję
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuj Dane");
        fileChooser.setInitialFileName("lifeos_transakcje_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            AsyncRunner.run(() -> {
                List<Transaction> all = transactionDAO.getAllTransactions();
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Nagłówek CSV (separator ;)
                    writer.println("ID;Data;Typ;Kategoria;Kwota;Opis");

                    // Dane
                    for (Transaction t : all) {
                        writer.printf("%d;%s;%s;%s;%.2f;%s%n",
                                t.getId(),
                                t.getDate(),
                                t.getType(),
                                t.getCategory(),
                                t.getAmount(),
                                t.getDescription().replace(";", ",") // Zabezpieczenie przed psuciem CSV
                        );
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, success -> {
                if (success) showInfo("Sukces", "Dane wyeksportowane pomyślnie!");
                else showError("Błąd", "Nie udało się zapisać pliku.");
            });
        }
    }

    /**
     * Tworzy kopię zapasową pliku bazy danych
     */
    @FXML
    public void createBackup(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Lokalizacja oryginalnej bazy (zgodnie z Database.java)
        String dbPath = System.getProperty("user.home") + "/lifeos_db.mv.db";
        File originalDb = new File(dbPath);

        if (!originalDb.exists()) {
            showError("Błąd", "Nie znaleziono pliku bazy danych: " + dbPath);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz Kopię Zapasową");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        fileChooser.setInitialFileName("lifeos_backup_" + timestamp + ".db");

        File destFile = fileChooser.showSaveDialog(stage);

        if (destFile != null) {
            AsyncRunner.run(() -> {
                try {
                    Files.copy(originalDb.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, success -> {
                if (success) showInfo("Backup", "Kopia zapasowa utworzona!");
                else showError("Błąd", "Nie udało się skopiować bazy danych.");
            });
        }
    }

    /**
     * Czyści całą bazę danych (niebezpieczne!)
     */
    @FXML
    public void clearDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Systemu");
        alert.setHeaderText("CZY NA PEWNO CHCESZ USUNĄĆ WSZYSTKIE DANE?");
        alert.setContentText("Tej operacji nie można cofnąć. Zostaną usunięte wszystkie transakcje, zadania i cele.");

        // Stylizacja alertu (opcjonalnie, żeby pasował do Dark Mode)
        alert.getDialogPane().setStyle("-fx-background-color: #262933;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
        alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #1f2229;");
        alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            AsyncRunner.run(() -> {
                try (Connection conn = Database.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Usuwamy dane z tabel
                    stmt.execute("DELETE FROM transactions");
                    stmt.execute("DELETE FROM tasks");
                    stmt.execute("DELETE FROM goals");
                    // Opcjonalnie reset sekwencji ID
                    // stmt.execute("ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1");

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, success -> {
                if (success) {
                    showInfo("Reset", "System został wyczyszczony.");
                    // Ważne: Informujemy inne moduły, że dane zniknęły, żeby odświeżyły widoki
                    EventBus.publish(new TransactionAddedEvent(null)); // Hack: wymusza odświeżenie finansów
                    // Możesz tu dodać więcej eventów
                } else {
                    showError("Błąd", "Nie udało się wyczyścić bazy.");
                }
            });
        }
    }

    // --- Helpery ---

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Wystąpił błąd");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}