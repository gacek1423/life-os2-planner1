package com.budget.controller.managers;

import com.budget.dao.TransactionDAO;
import com.budget.db.DatabaseService;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.infrastructure.events.DataClearedEvent;
import com.budget.service.DataExporter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SettingsManager {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DataExporter dataExporter = new DataExporter();

    public void exportData(Stage stage) {
        AsyncRunner.run(transactionDAO::getAllTransactions, list ->
                dataExporter.exportTransactionsToCSV(list, stage));
    }

    public void createDatabaseBackup(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz Kopię Zapasową (Backup)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki Bazy Danych (*.mv.db)", "*.mv.db"));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        fileChooser.setInitialFileName("LifeOS_Backup_" + timestamp + ".mv.db");

        File destFile = fileChooser.showSaveDialog(stage);
        if (destFile != null) {
            AsyncRunner.run(() -> {
                // H2 Database zapisuje dane w pliku z rozszerzeniem .mv.db
                // Domyślnie w katalogu domowym użytkownika lub ./data
                // Tutaj zakładamy prostą kopię pliku (działa najlepiej gdy aplikacja jest zamknięta,
                // ale w H2 można użyć komendy BACKUP TO 'filename')

                try {
                    // Wykonujemy SQL BACKUP
                    DatabaseService.backupDatabase(destFile.getAbsolutePath());
                    Platform.runLater(() -> showAlert("Sukces", "Kopia zapasowa została utworzona pomyślnie."));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Błąd", "Nie udało się utworzyć kopii: " + e.getMessage()));
                }
            }, () -> {});
        }
    }

    public void clearDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Strefa Niebezpieczna");
        alert.setHeaderText("Czy na pewno chcesz usunąć WSZYSTKIE dane?");
        alert.setContentText("Tej operacji nie można cofnąć. Wszystkie transakcje, cele i zadania zostaną utracone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            AsyncRunner.run(() -> {
                DatabaseService.clearAllData();
                EventBus.publish(new DataClearedEvent());
            }, () -> showAlert("Wyczyszczono", "System został przywrócony do ustawień fabrycznych."));
        }
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}