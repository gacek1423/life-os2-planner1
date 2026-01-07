package com.budget.controller;

import com.budget.controller.managers.*; // Import wszystkich Managerów
import com.budget.infrastructure.EventBus;
import com.budget.modules.finance.events.TransactionAddedEvent;
import com.budget.modules.goals.events.GoalAddedEvent;
import com.budget.modules.tasks.events.TaskUpdatedEvent;
import com.budget.model.Goal;
import com.budget.model.Task;
import com.budget.model.Transaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class DashboardController {

    // --- FXML UI ELEMENTS ---
    @FXML private Button btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia;
    @FXML private StackPane rootPane;
    @FXML private VBox viewKokpit, viewZadania, viewCele, viewRaporty, viewUstawienia;
    @FXML private BorderPane viewFinanse;

    // Kokpit Widgets
    @FXML private Label dashBalanceLabel, dashTasksLabel, lblDate, lblBudgetSummary;
    @FXML private HBox purseContainer;
    @FXML private ListView<Task> urgentTasksList;
    @FXML private ListView<Transaction> recentTransactionsList;

    // Finanse Widgets
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField categoryField, amountField, descField, searchField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private PieChart expenseChart;
    @FXML private ListView<HBox> budgetListView;
    @FXML private Label balanceLabel, incomeLabel, expenseLabel, monthLabel;

    // Zadania Widgets
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker taskDatePicker;
    @FXML private ListView<Task> taskListView;

    // Cele Widgets
    @FXML private TextField goalNameField, goalTargetField, goalCurrentField;
    @FXML private DatePicker goalDatePicker;
    @FXML private ListView<Goal> goalListView;

    // Raporty Widgets
    @FXML private Label repAvgIncome, repAvgExpense, repTotalSavings;
    @FXML private AreaChart<String, Number> reportTrendChart;
    @FXML private BarChart<String, Number> reportSavingsChart, reportCategoryChart;
    @FXML private Button btnGenerateReport;
    // Navigation
    @FXML private Button btnKalendarz;
    // Views
    @FXML private VBox viewKalendarz;
    // Calendar Widgets
    @FXML private GridPane calendarGrid;
    @FXML private Label calMonthLabel;



    // --- MANAGERS (DELEGACJA) ---
    private FinanceManager financeManager;
    private TaskManager taskManager;
    private GoalManager goalManager;
    private PurseManager purseManager;
    private ReportManager reportManager;
    private SettingsManager settingsManager;
    private CalendarManager calendarManager;

    @FXML
    public void initialize() {
        // 1. Inicjalizacja Managerów
        financeManager = new FinanceManager(transactionTable, expenseChart, budgetListView,
                balanceLabel, incomeLabel, expenseLabel, monthLabel,
                amountField, categoryField, descField, searchField, typeBox, datePicker);

        taskManager = new TaskManager(taskListView, urgentTasksList, taskTitleField, priorityBox, taskDatePicker, dashTasksLabel);

        goalManager = new GoalManager(goalListView, goalNameField, goalTargetField, goalCurrentField, goalDatePicker);

        purseManager = new PurseManager(purseContainer);

        reportManager = new ReportManager(reportTrendChart, reportSavingsChart, reportCategoryChart, repAvgIncome, repAvgExpense, repTotalSavings);
        calendarManager = new com.budget.controller.managers.CalendarManager(calendarGrid, calMonthLabel,this::handleCalendarDateSelect /* <-- Nowy callback*/);
        // 2. Setup (Konfiguracja wstępna)
        financeManager.setup();
        taskManager.setup();
        goalManager.setup();
        reportManager.setup();
        purseManager.refreshPurses();
        settingsManager = new SettingsManager();

        // 3. EventBus (Reaktywne odświeżanie)
        EventBus.subscribe(TransactionAddedEvent.class, e -> Platform.runLater(() -> {
            financeManager.refreshFinances();
            purseManager.refreshPurses(); // Finanse wpływają na portfele
            reportManager.refreshReports(); // I na raporty
            calendarManager.refreshCalendar();
        }));

        EventBus.subscribe(TaskUpdatedEvent.class, e -> Platform.runLater(() -> taskManager.refreshTasks()));
        EventBus.subscribe(GoalAddedEvent.class, e -> Platform.runLater(() -> goalManager.refreshGoals()));
        EventBus.subscribe(TaskUpdatedEvent.class, e -> Platform.runLater(() -> calendarManager.refreshCalendar()));


        // Inne
        showKokpit();
    }

    // --- ACTIONS (Delegowanie do managerów) ---
    // Metoda wywoływana, gdy klikniesz dzień w kalendarzu
    // Metoda wywoływana, gdy klikniesz dzień w kalendarzu
    private void handleCalendarDateSelect(java.time.LocalDate date) {
        // Tworzymy dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Transakcja", "Transakcja", "Zadanie");
        dialog.setTitle("Planer");
        dialog.setHeaderText("Planowanie dla: " + date);
        dialog.setContentText("Co chcesz dodać?");

        // 1. Podpinamy nasz styl CSS do dialogu
        // Fix: Używamy pełnej ścieżki do zasobu
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/budget/style.css").toExternalForm());

        // 2. Usuwamy standardową, brzydką ikonę "?" (grafika null)
        dialog.setGraphic(null);

        // 3. Opcjonalnie: Ustawiamy ikonę okna (Stage) na logo aplikacji (jeśli masz ikonę)
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        // stage.getIcons().add(new Image(...)); // Odkomentuj jeśli masz ikonę

        dialog.showAndWait().ifPresent(type -> {
            if ("Transakcja".equals(type)) {
                showFinanse();
                datePicker.setValue(date);
                // Mały trick: uruchamiamy focus z opóźnieniem, żeby zadziałał po przełączeniu widoku
                Platform.runLater(() -> amountField.requestFocus());
            } else {
                showZadania();
                taskDatePicker.setValue(date);
                Platform.runLater(() -> taskTitleField.requestFocus());
            }
        });
    }


    @FXML private void handleAddTransaction() { financeManager.addTransaction(); }
    @FXML private void prevMonth() { financeManager.prevMonth(); }
    @FXML private void nextMonth() { financeManager.nextMonth(); }

    @FXML private void handleAddTask() { taskManager.addTask(); }
    @FXML private void handleAddGoal() { goalManager.addGoal(); }

    @FXML private void handleGenerateReport() {
        reportManager.generateReport((Stage) rootPane.getScene().getWindow());
    }

    // --- NAVIGATION ---
    @FXML public void showKokpit() { switchView(viewKokpit, btnKokpit); purseManager.refreshPurses(); }
    @FXML public void showFinanse() { switchView(viewFinanse, btnFinanse); financeManager.refreshFinances(); }
    @FXML public void showZadania() { switchView(viewZadania, btnZadania); taskManager.refreshTasks(); }
    @FXML public void showCele() { switchView(viewCele, btnCele); goalManager.refreshGoals(); }
    @FXML public void showRaporty() { switchView(viewRaporty, btnRaporty); reportManager.refreshReports(); }
    @FXML public void showUstawienia() { switchView(viewUstawienia, btnUstawienia); }

    private void switchView(javafx.scene.Node view, Button activeButton) {
        viewKokpit.setVisible(false);
        viewFinanse.setVisible(false);
        viewZadania.setVisible(false);
        viewCele.setVisible(false);
        viewRaporty.setVisible(false);
        viewUstawienia.setVisible(false);
        viewKalendarz.setVisible(false);

        List<Button> btns = Arrays.asList(btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia);
        btns.forEach(b -> b.getStyleClass().remove("sidebar-button-active"));

        view.setVisible(true);
        activeButton.getStyleClass().add("sidebar-button-active");
    }

    // Puste metody dla przycisków, których jeszcze nie obsłużyliśmy w pełni
    @FXML private void handleExport() {settingsManager.exportData((Stage) rootPane.getScene().getWindow());}
    @FXML private void handleBackup() {settingsManager.createDatabaseBackup((Stage) rootPane.getScene().getWindow());}
    @FXML private void handleClearDatabase() {settingsManager.clearDatabase();}
    @FXML public void showKalendarz() { switchView(viewKalendarz, btnKalendarz); calendarManager.refreshCalendar(); }
    @FXML private void prevCalMonth() { calendarManager.prevMonth(); }
    @FXML private void nextCalMonth() { calendarManager.nextMonth(); }
}