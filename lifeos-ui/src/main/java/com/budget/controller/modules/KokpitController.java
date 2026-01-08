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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KokpitController {

    @FXML private Label lblDate;
    @FXML private Label dashBalanceLabel;
    @FXML private Label dashTasksLabel;
    @FXML private Label lblBudgetSummary;

    @FXML private HBox purseContainer;
    @FXML private ListView<Transaction> recentTransactionsList;
    @FXML private ListView<Task> urgentTasksList;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final PurseDAO purseDAO = new PurseDAO(); // Zakładam, że masz DAO do portfeli/kont

    @FXML
    public void initialize() {
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        refresh();
    }

    public void refresh() {
        AsyncRunner.run(() -> {
            // 1. Pobieranie danych w tle
            double balance = transactionDAO.getCurrentBalance();
            List<Task> urgentTasks = taskDAO.getUrgentTasks();
            List<Transaction> recentTrans = transactionDAO.getRecentTransactions(10);

            return new DashboardData(balance, urgentTasks, recentTrans);
        }, data -> {
            // 2. Aktualizacja UI
            dashBalanceLabel.setText(String.format("%.2f PLN", data.balance));
            dashTasksLabel.setText(data.tasks.size() + " zadań");

            recentTransactionsList.setItems(FXCollections.observableArrayList(data.transactions));
            urgentTasksList.setItems(FXCollections.observableArrayList(data.tasks));

            // Logika prostych portfeli (karty na środku)
            refreshPurses(data.balance);
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

    // Akcje przekierowania (obsługiwane przez DashboardController, tu tylko zaślepki jeśli potrzebne)
    @FXML public void goToFinanse() { /* Logika w DashboardController */ }
    @FXML public void goToZadania() { /* Logika w DashboardController */ }

    private record DashboardData(double balance, List<Task> tasks, List<Transaction> transactions) {}
}