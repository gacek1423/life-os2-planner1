package com.budget.controller;

import com.budget.dao.GoalDAO;
import com.budget.dao.TaskDAO;
import com.budget.dao.TransactionDAO;
import com.budget.model.Goal;
import com.budget.model.Task;
import com.budget.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.budget.service.DataExporter;
import javafx.stage.Stage;
import com.budget.db.DatabaseService;
import java.sql.Connection;
import java.sql.Statement;


import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DashboardController {

    // --- SEKCJA: FINANSE ---
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField categoryField, amountField, descField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colType, colCategory, colDesc;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private PieChart expenseChart;
    @FXML private Label balanceLabel, incomeLabel, expenseLabel;

    // --- SEKCJA: ZADANIA ---
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker taskDatePicker;
    @FXML private ListView<Task> taskListView;

    // --- SEKCJA: CELE ---
    @FXML private TextField goalNameField;
    @FXML private TextField goalTargetField;
    @FXML private TextField goalCurrentField;
    @FXML private DatePicker goalDatePicker;
    @FXML private ListView<Goal> goalListView;
    // --- RAPORTY (NOWOŚĆ) ---
    @FXML private LineChart<String, Number> trendChart;
    @FXML private BarChart<String, Number> categoryBarChart;

    // --- DAO (Dostęp do bazy) ---
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final GoalDAO goalDAO = new GoalDAO();

    @FXML
    public void initialize() {
        // 1. Inicjalizacja Finansów
        setupTable();
        typeBox.setItems(FXCollections.observableArrayList("WYDATEK", "PRZYCHÓD"));
        typeBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());

        // 2. Inicjalizacja Zadań
        setupTasks();
        priorityBox.setItems(FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        priorityBox.getSelectionModel().select("MEDIUM");
        taskDatePicker.setValue(LocalDate.now());

        // 3. Inicjalizacja Celów
        setupGoals();

        // 4. Załadowanie wszystkich danych
        refreshAll();
    }

    private void refreshAll() {
        refreshFinances();
        refreshTasks();
        refreshGoals();
    }

    // =========================================================
    // LOGIKA: FINANSE
    // =========================================================

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 1. Kolumna KATEGORIA z ikonami (Emoji)
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Dodajemy ikonę w zależności od słowa kluczowego
                    String icon = getIconForCategory(item);
                    setText(icon + " " + item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #e1e4e8;");
                }
            }
        });

        // 2. Kolumna KWOTA z kolorami
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f zł", amount));
                    Transaction t = getTableView().getItems().get(getIndex());
                    if ("WYDATEK".equals(t.getType())) setTextFill(Color.web("#8a3c3c")); // Czerwony
                    else setTextFill(Color.web("#4c9a6a")); // Zielony
                }
            }
        });
    }

    // Metoda pomocnicza dobierająca ikony
    private String getIconForCategory(String category) {
        String catLower = category.toLowerCase();

        if (catLower.contains("jedzenie") || catLower.contains("spożywcze") || catLower.contains("restauracja")) return "🍔";
        if (catLower.contains("dom") || catLower.contains("czynsz") || catLower.contains("mieszkanie")) return "🏠";
        if (catLower.contains("paliwo") || catLower.contains("auto") || catLower.contains("transport") || catLower.contains("uber")) return "🚗";
        if (catLower.contains("zakupy") || catLower.contains("ubrania") || catLower.contains("sklep")) return "🛍️";
        if (catLower.contains("rozrywka") || catLower.contains("kino") || catLower.contains("netflix") || catLower.contains("gry")) return "🎮";
        if (catLower.contains("zdrowie") || catLower.contains("leki") || catLower.contains("lekarz")) return "❤️";
        if (catLower.contains("wypłata") || catLower.contains("przelew") || catLower.contains("biznes")) return "💰";
        if (catLower.contains("edukacja") || catLower.contains("kurs") || catLower.contains("książki")) return "📚";
        if (catLower.contains("rachunki") || catLower.contains("prąd") || catLower.contains("internet")) return "⚡";

        return "📁"; // Domyślna ikona folderu
    }

    @FXML
    private void handleAddTransaction() {
        try {
            if (amountField.getText().isEmpty()) return;
            double amount = Double.parseDouble(amountField.getText().replace(",", "."));

            Transaction t = new Transaction(0, typeBox.getValue(), categoryField.getText(), amount, datePicker.getValue(), descField.getText());
            transactionDAO.addTransaction(t);

            amountField.clear();
            categoryField.clear();
            descField.clear();
            refreshFinances();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshFinances() {
        ObservableList<Transaction> list = FXCollections.observableArrayList(transactionDAO.getAllTransactions());
        transactionTable.setItems(list);

        double balance = 0, income = 0, expense = 0;
        Map<String, Double> expenseMap = new HashMap<>();

        for (Transaction t : list) {
            if ("PRZYCHÓD".equals(t.getType())) {
                balance += t.getAmount();
                income += t.getAmount();
            } else {
                balance -= t.getAmount();
                expense += t.getAmount();
                expenseMap.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        balanceLabel.setText(String.format("%.2f PLN", balance));
        incomeLabel.setText(String.format("+ %.2f", income));
        expenseLabel.setText(String.format("- %.2f", expense));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        expenseMap.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
        expenseChart.setData(pieData);
    }

    // =========================================================
    // LOGIKA: ZADANIA
    // =========================================================

    private void setupTasks() {
        taskListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.getStyleClass().add("task-row");
                    if (task.isDone()) container.getStyleClass().add("task-done");

                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(task.isDone());
                    checkBox.setOnAction(e -> {
                        taskDAO.toggleStatus(task.getId(), checkBox.isSelected());
                        refreshTasks();
                    });

                    VBox infoBox = new VBox(2);
                    Label titleLabel = new Label(task.getTitle());
                    titleLabel.setStyle("-fx-text-fill: #e1e4e8; -fx-font-weight: bold;");
                    Label dateLabel = new Label("Termin: " + task.getDueDate());
                    dateLabel.setStyle("-fx-text-fill: #8b92a1; -fx-font-size: 10px;");
                    infoBox.getChildren().addAll(titleLabel, dateLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label priorityLabel = new Label(task.getPriority());
                    priorityLabel.getStyleClass().add("priority-tag");
                    String color = switch (task.getPriority()) {
                        case "HIGH" -> "#8a3c3c";
                        case "MEDIUM" -> "#d4a76a";
                        default -> "#4fa3c7";
                    };
                    priorityLabel.setStyle("-fx-background-color: " + color + ";");

                    container.getChildren().addAll(checkBox, infoBox, spacer, priorityLabel);
                    setGraphic(container);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2;");
                }
            }
        });
    }

    @FXML
    private void handleAddTask() {
        if (taskTitleField.getText().isEmpty()) return;
        Task t = new Task(0, taskTitleField.getText(), priorityBox.getValue(), false, taskDatePicker.getValue());
        taskDAO.addTask(t);
        taskTitleField.clear();
        refreshTasks();
    }


    private void refreshTasks() {
        taskListView.setItems(FXCollections.observableArrayList(taskDAO.getAllTasks()));
    }

    // =========================================================
    // LOGIKA: CELE
    // =========================================================

    private void setupGoals() {
        goalListView.setCellFactory(param -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal goal, boolean empty) {
                super.updateItem(goal, empty);
                if (empty || goal == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox container = new VBox(8);
                    container.getStyleClass().add("goal-row");

                    HBox topRow = new HBox(10);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label(goal.getName());
                    nameLabel.setStyle("-fx-text-fill: #e1e4e8; -fx-font-weight: bold; -fx-font-size: 14px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String amounts = String.format("%.0f zł / %.0f zł (%.0f%%)",
                            goal.getCurrentAmount(),
                            goal.getTargetAmount(),
                            goal.getProgress() * 100);
                    Label amountsLabel = new Label(amounts);
                    amountsLabel.setStyle("-fx-text-fill: #8b92a1;");

                    topRow.getChildren().addAll(nameLabel, spacer, amountsLabel);

                    ProgressBar progressBar = new ProgressBar(goal.getProgress());
                    progressBar.setMaxWidth(Double.MAX_VALUE);
                    progressBar.setPrefHeight(20);

                    container.getChildren().addAll(topRow, progressBar);

                    if (goal.getDeadline() != null) {
                        Label dateLabel = new Label("Termin: " + goal.getDeadline());
                        dateLabel.setStyle("-fx-text-fill: #8b92a1; -fx-font-size: 10px;");
                        container.getChildren().add(dateLabel);
                    }

                    setGraphic(container);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }
    @FXML
    private void handleExport() {
        DataExporter exporter = new DataExporter();
        // Pobieramy aktualne okno aplikacji, aby wyświetlić dialog zapisu
        Stage stage = (Stage) typeBox.getScene().getWindow();

        // Pobieramy wszystkie transakcje
        ObservableList<Transaction> list = FXCollections.observableArrayList(transactionDAO.getAllTransactions());

        exporter.exportTransactionsToCSV(list, stage);
    }

    @FXML
    private void handleClearDatabase() {
        // Proste zabezpieczenie - Alert potwierdzenia
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Czy na pewno chcesz usunąć WSZYSTKIE dane?");
        alert.setContentText("Tej operacji nie można cofnąć.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            clearAllTables();
            refreshAll(); // Odśwież widoki (będą puste)
        }
    }

    private void clearAllTables() {
        String sql1 = "DELETE FROM transactions";
        String sql2 = "DELETE FROM tasks";
        String sql3 = "DELETE FROM goals";

        try (Connection conn = DatabaseService.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            System.out.println("Baza danych wyczyszczona.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddGoal() {
        try {
            if (goalNameField.getText().isEmpty() || goalTargetField.getText().isEmpty()) return;

            String name = goalNameField.getText();
            double target = Double.parseDouble(goalTargetField.getText().replace(",", "."));
            double current = goalCurrentField.getText().isEmpty() ? 0.0 : Double.parseDouble(goalCurrentField.getText().replace(",", "."));
            LocalDate deadline = goalDatePicker.getValue();

            Goal g = new Goal(0, name, target, current, deadline);
            goalDAO.addGoal(g);

            goalNameField.clear();
            goalTargetField.clear();
            goalCurrentField.clear();
            goalDatePicker.setValue(null);

            refreshGoals();

        } catch (NumberFormatException e) {
            System.err.println("Błąd formatu liczby w formularzu celów!");
        }
    }
    // ================== RAPORTY (NOWA LOGIKA) ==================

    private void refreshReports(ObservableList<Transaction> transactions) {
        // 1. Przygotowanie danych do Wykresu Liniowego (Trend miesięczny)
        // Używamy TreeMap, aby miesiące były posortowane automatycznie
        Map<YearMonth, Double> incomeByMonth = new TreeMap<>();
        Map<YearMonth, Double> expenseByMonth = new TreeMap<>();

        // 2. Przygotowanie danych do Wykresu Słupkowego (Kategorie)
        Map<String, Double> categoryExpenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            YearMonth ym = YearMonth.from(t.getDate());

            if ("PRZYCHÓD".equals(t.getType())) {
                incomeByMonth.merge(ym, t.getAmount(), Double::sum);
            } else {
                expenseByMonth.merge(ym, t.getAmount(), Double::sum);
                categoryExpenseMap.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        // --- KONFIGURACJA WYKRESU LINIOWEGO ---
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Przychody");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Wydatki");

        // Łączymy wszystkie miesiące z obu map, żeby oś X była spójna
        incomeByMonth.forEach((ym, amount) ->
                incomeSeries.getData().add(new XYChart.Data<>(ym.toString(), amount)));

        expenseByMonth.forEach((ym, amount) ->
                expenseSeries.getData().add(new XYChart.Data<>(ym.toString(), amount)));

        trendChart.getData().clear();
        trendChart.getData().addAll(incomeSeries, expenseSeries);

        // --- KONFIGURACJA WYKRESU SŁUPKOWEGO ---
        XYChart.Series<String, Number> categorySeries = new XYChart.Series<>();
        categorySeries.setName("Kategorie");

        categoryExpenseMap.forEach((cat, amount) ->
                categorySeries.getData().add(new XYChart.Data<>(cat, amount)));

        categoryBarChart.getData().clear();
        categoryBarChart.getData().add(categorySeries);
    }



    private void refreshGoals() {
        goalListView.setItems(FXCollections.observableArrayList(goalDAO.getAllGoals()));
    }
}