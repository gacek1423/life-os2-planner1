package com.budget.controller.modules;

import com.budget.controller.DashboardController;
import com.budget.dao.PurseDAO;
import com.budget.dao.TaskDAO;
import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Task;
import com.budget.model.Transaction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KokpitController {

    @FXML private Label lblDate;
    @FXML private Label dashBalanceLabel;
    @FXML private Label dashTasksLabel;
    @FXML private Label lblBudgetSummary; // To było zacięte na "Analiza..."

    @FXML private HBox purseContainer;
    @FXML private ListView<Transaction> recentTransactionsList;
    @FXML private ListView<Task> urgentTasksList;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final PurseDAO purseDAO = new PurseDAO();

    @FXML
    public void initialize() {
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));

        // 1. NAPRAWA LIST: Ustawiamy ładny wygląd wierszy
        setupListViews();

        refresh();
    }

    public void refresh() {
        lblBudgetSummary.setText("Analiza..."); // Resetujemy tekst na czas ładowania

        AsyncRunner.run(() -> {
            // 1. Pobieranie danych w tle
            double balance = transactionDAO.getCurrentBalance(); // To wymaga metody w TransactionDAO (dałem ją wcześniej)
            List<Task> urgentTasks = taskDAO.getUrgentTasks();
            List<Transaction> recentTrans = transactionDAO.getRecentTransactions(5); // Pobierz 5 ostatnich

            return new DashboardData(balance, urgentTasks, recentTrans);
        }, data -> {
            // 2. Aktualizacja UI
            dashBalanceLabel.setText(String.format("%.2f PLN", data.balance));
            dashTasksLabel.setText(data.tasks.size() + " zadań");

            recentTransactionsList.setItems(FXCollections.observableArrayList(data.transactions));
            urgentTasksList.setItems(FXCollections.observableArrayList(data.tasks));

            // 3. NAPRAWA "ANALIZA...": Obliczamy kondycję finansową
            updateFinancialHealth(data.balance);

            // Logika prostych portfeli
            refreshPurses(data.balance);
        });
    }

    // --- NOWA METODA: Logika "Płynności Finansowej" ---
    private void updateFinancialHealth(double balance) {
        if (balance > 2000) {
            lblBudgetSummary.setText("Stabilna");
            lblBudgetSummary.setStyle("-fx-text-fill: #51cf66; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else if (balance > 0) {
            lblBudgetSummary.setText("Niska płynność");
            lblBudgetSummary.setStyle("-fx-text-fill: #fcc419; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else {
            lblBudgetSummary.setText("ZAGROŻONA");
            lblBudgetSummary.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 15px; -fx-font-weight: bold;");
        }
    }

    // --- NOWA METODA: Formatowanie list (żeby nie było brzydkiego toString) ---
    private void setupListViews() {
        // Formatowanie listy Transakcji
        recentTransactionsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    // Ikona typu (Przychód/Wydatek)
                    Label icon = new Label("WYDATEK".equals(item.getType()) ? "📉" : "📈");

                    // Kategoria i Opis
                    VBox details = new VBox(2);
                    Label cat = new Label(item.getCategory());
                    cat.setStyle("-fx-text-fill: #8b92a1; -fx-font-size: 10px; -fx-font-weight: bold;");
                    Label desc = new Label(item.getDescription());
                    desc.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                    details.getChildren().addAll(cat, desc);

                    Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);

                    // Kwota z kolorem
                    Label amount = new Label(String.format("%.2f zł", item.getAmount()));
                    if ("PRZYCHÓD".equals(item.getType())) {
                        amount.setStyle("-fx-text-fill: #51cf66; -fx-font-weight: bold;");
                    } else {
                        amount.setStyle("-fx-text-fill: white;");
                    }

                    box.getChildren().addAll(icon, details, r, amount);
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0; -fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent;");
                }
            }
        });

        // Formatowanie listy Zadań (Pilne)
        urgentTasksList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label icon = new Label("🔥"); // Ikonka ognia dla pilnych
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-text-fill: white;");

                    Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);

                    // Data na czerwono
                    String dateStr = item.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM"));
                    Label date = new Label(dateStr);
                    date.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 11px;");

                    box.getChildren().addAll(icon, title, r, date);
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
                }
            }
        });
    }

    private void refreshPurses(double totalBalance) {
        purseContainer.getChildren().clear();
        // Przykładowe statyczne portfele (docelowo z Bazy Danych)
        addPurseCard("GŁÓWNE KONTO", totalBalance * 0.6, "blue");
        addPurseCard("OSZCZĘDNOŚCI", totalBalance * 0.3, "green");
        addPurseCard("GOTÓWKA", totalBalance * 0.1, "orange");
    }

    private void addPurseCard(String name, double amount, String colorClass) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefSize(200, 120);
        card.setStyle("-fx-padding: 20; -fx-min-width: 200;");

        Label title = new Label(name);
        title.setStyle("-fx-text-fill: #8b92a1; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label value = new Label(String.format("%.2f PLN", amount));
        value.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        card.getChildren().addAll(title, value);
        purseContainer.getChildren().add(card);
    }

    // Akcje przekierowania
    @FXML public void goToFinanse() { /* DashboardController obsłuży przełączenie */ }
    @FXML public void goToZadania() { /* DashboardController obsłuży przełączenie */ }

    private record DashboardData(double balance, List<Task> tasks, List<Transaction> transactions) {}
}