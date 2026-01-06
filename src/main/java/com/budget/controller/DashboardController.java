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
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Kolorowanie kwot
        colAmount.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", amount));
                    Transaction t = getTableView().getItems().get(getIndex());
                    if ("WYDATEK".equals(t.getType())) setTextFill(Color.web("#8a3c3c")); // Czerwony (Burgundy)
                    else setTextFill(Color.web("#4c9a6a")); // Zielony (Emerald)
                }
            }
        });
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

    private void refreshGoals() {
        goalListView.setItems(FXCollections.observableArrayList(goalDAO.getAllGoals()));
    }
}