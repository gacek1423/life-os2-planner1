package com.budget.controller;

import com.budget.dao.GoalDAO;
import com.budget.dao.TaskDAO;
import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Goal;
import com.budget.model.Task;
import com.budget.model.Transaction;
import com.budget.service.DataExporter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class DashboardController {

    // --- SIDEBAR NAVIGATION ---
    @FXML private Button btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia;
    @FXML private StackPane rootPane;

    // --- VIEWS (PANELS) ---
    @FXML private VBox viewKokpit;
    @FXML private BorderPane viewFinanse;
    @FXML private VBox viewZadania;
    @FXML private VBox viewCele;
    @FXML private VBox viewRaporty;
    @FXML private VBox viewUstawienia;

    // --- KOKPIT WIDGETS ---
    @FXML private Label dashBalanceLabel, dashTasksLabel, dashGoalsLabel;

    // --- FINANSE ---
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField categoryField, amountField, descField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colType, colCategory, colDesc;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private PieChart expenseChart;
    @FXML private Label balanceLabel, incomeLabel, expenseLabel;
    @FXML private Label monthLabel;
    @FXML private TextField searchField;
    private YearMonth currentMonth;

    // --- ZADANIA ---
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker taskDatePicker;
    @FXML private ListView<Task> taskListView;

    // --- CELE ---
    @FXML private TextField goalNameField, goalTargetField, goalCurrentField;
    @FXML private DatePicker goalDatePicker;
    @FXML private ListView<Goal> goalListView;

    // --- RAPORTY ---
    @FXML private LineChart<String, Number> trendChart;
    @FXML private BarChart<String, Number> categoryBarChart;

    // --- COMMAND PALETTE ---
    @FXML private StackPane commandPaletteOverlay;
    @FXML private TextField commandInput;
    @FXML private ListView<CommandItem> commandList;

    // --- BACKEND ---
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final GoalDAO goalDAO = new GoalDAO();
    private final ObservableList<CommandItem> allCommands = FXCollections.observableArrayList();

    private static class CommandItem {
        String name;
        Runnable action;
        public CommandItem(String name, Runnable action) { this.name = name; this.action = action; }
        @Override public String toString() { return name; }
    }

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();

        // 1. Setup Modules
        setupTable();
        setupTasks();
        setupGoals();
        setupForms();
        setupCommandPalette();

        // 2. Reactive Update
        EventBus.subscribe(EventBus.EventType.TRANSACTION_ADDED, o -> { refreshFinances(); refreshReports(); updateKokpit(); });
        EventBus.subscribe(EventBus.EventType.TASK_UPDATED, o -> { refreshTasks(); updateKokpit(); });
        EventBus.subscribe(EventBus.EventType.GOAL_ADDED, o -> { refreshGoals(); updateKokpit(); });
        EventBus.subscribe(EventBus.EventType.DATA_CLEARED, o -> refreshAll());

        searchField.textProperty().addListener((obs, oldV, newV) -> refreshFinances());

        // 3. Start
        refreshAll();
        showKokpit(); // Default view
    }

    private void setupForms() {
        typeBox.setItems(FXCollections.observableArrayList("WYDATEK", "PRZYCHÓD"));
        typeBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());
        priorityBox.setItems(FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        priorityBox.getSelectionModel().select("MEDIUM");
        taskDatePicker.setValue(LocalDate.now());
    }

    private void refreshAll() {
        refreshFinances();
        refreshTasks();
        refreshGoals();
        refreshReports();
        updateKokpit();
    }

    // ================== NAVIGATION LOGIC ==================

    private void switchView(Node view, Button activeButton) {
        // Hide all
        viewKokpit.setVisible(false);
        viewFinanse.setVisible(false);
        viewZadania.setVisible(false);
        viewCele.setVisible(false);
        viewRaporty.setVisible(false);
        viewUstawienia.setVisible(false);

        // Reset buttons
        List<Button> btns = Arrays.asList(btnKokpit, btnFinanse, btnZadania, btnCele, btnRaporty, btnUstawienia);
        btns.forEach(b -> b.getStyleClass().remove("sidebar-button-active"));

        // Show selected
        view.setVisible(true);
        activeButton.getStyleClass().add("sidebar-button-active");
    }

    @FXML public void showKokpit() { switchView(viewKokpit, btnKokpit); updateKokpit(); }
    @FXML public void showFinanse() { switchView(viewFinanse, btnFinanse); }
    @FXML public void showZadania() { switchView(viewZadania, btnZadania); }
    @FXML public void showCele() { switchView(viewCele, btnCele); }
    @FXML public void showRaporty() { switchView(viewRaporty, btnRaporty); }
    @FXML public void showUstawienia() { switchView(viewUstawienia, btnUstawienia); }

    private void updateKokpit() {
        // Szybkie podsumowanie na ekran startowy
        AsyncRunner.run(() -> transactionDAO.getAllTransactions(), list -> {
            double bal = list.stream().mapToDouble(t -> "PRZYCHÓD".equals(t.getType()) ? t.getAmount() : -t.getAmount()).sum();
            dashBalanceLabel.setText(String.format("%.2f PLN", bal));
        });
        AsyncRunner.run(() -> taskDAO.getAllTasks(), list -> {
            long count = list.stream().filter(t -> !t.isDone()).count();
            dashTasksLabel.setText(count + " do zrobienia");
        });
        AsyncRunner.run(() -> goalDAO.getAllGoals(), list -> {
            long count = list.stream().filter(g -> g.getProgress() < 1.0).count();
            dashGoalsLabel.setText(count + " w toku");
        });
    }

    // ================== COMMAND PALETTE ==================

    private void setupCommandPalette() {
        allCommands.addAll(
                new CommandItem("🏠 Idź do: Kokpit", this::showKokpit),
                new CommandItem("💰 Idź do: Finanse", this::showFinanse),
                new CommandItem("✅ Idź do: Zadania", this::showZadania),
                new CommandItem("🏆 Idź do: Cele", this::showCele),
                new CommandItem("📊 Idź do: Raporty", this::showRaporty),
                new CommandItem("➕ Nowa Transakcja", () -> { showFinanse(); amountField.requestFocus(); }),
                new CommandItem("➕ Nowe Zadanie", () -> { showZadania(); taskTitleField.requestFocus(); }),
                new CommandItem("💾 Eksportuj CSV", this::handleExport)
        );

        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                rootPane.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                        this::toggleCommandPalette
                );
            }
        });

        commandInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) toggleCommandPalette();
            if (e.getCode() == KeyCode.ENTER) executeCommand();
            if (e.getCode() == KeyCode.DOWN) { commandList.requestFocus(); commandList.getSelectionModel().selectFirst(); }
        });
        commandList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) executeCommand();
            if (e.getCode() == KeyCode.ESCAPE) toggleCommandPalette();
        });
        commandList.setOnMouseClicked(e -> { if (e.getClickCount() == 2) executeCommand(); });

        commandInput.textProperty().addListener((obs, oldV, newVal) -> {
            if (newVal == null || newVal.isEmpty()) commandList.setItems(allCommands);
            else {
                String lower = newVal.toLowerCase();
                commandList.setItems(allCommands.filtered(item -> item.name.toLowerCase().contains(lower)));
                if (!commandList.getItems().isEmpty()) commandList.getSelectionModel().selectFirst();
            }
        });
    }

    @FXML public void toggleCommandPalette() {
        boolean visible = !commandPaletteOverlay.isVisible();
        commandPaletteOverlay.setVisible(visible);
        if (visible) { commandInput.clear(); commandList.setItems(allCommands); commandInput.requestFocus(); }
    }

    private void executeCommand() {
        CommandItem item = commandList.getSelectionModel().getSelectedItem();
        if (item != null) { toggleCommandPalette(); item.action.run(); }
    }

    // ================== FINANSE LOGIC ==================
    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(getIcon(item) + " " + item); setStyle("-fx-font-weight: bold; -fx-text-fill: #e1e4e8;"); }
            }
        });
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) setText(null);
                else {
                    setText(String.format("%.2f zł", amount));
                    Transaction t = getTableView().getItems().get(getIndex());
                    if ("WYDATEK".equals(t.getType())) setTextFill(Color.web("#8a3c3c")); else setTextFill(Color.web("#4c9a6a"));
                }
            }
        });

        ContextMenu cm = new ContextMenu();
        MenuItem del = new MenuItem("Usuń");
        del.setOnAction(e -> {
            Transaction t = transactionTable.getSelectionModel().getSelectedItem();
            if(t!=null) AsyncRunner.run(() -> transactionDAO.deleteTransaction(t.getId()), () -> EventBus.publish(EventBus.EventType.TRANSACTION_ADDED));
        });
        transactionTable.setContextMenu(cm);
    }

    @FXML private void handleAddTransaction() {
        if (amountField.getText().isEmpty()) return;
        try {
            double amt = Double.parseDouble(amountField.getText().replace(",", "."));
            Transaction t = new Transaction(0, typeBox.getValue(), categoryField.getText(), amt, datePicker.getValue(), descField.getText());
            AsyncRunner.run(() -> transactionDAO.addTransaction(t), () -> {
                amountField.clear(); categoryField.clear(); descField.clear();
                EventBus.publish(EventBus.EventType.TRANSACTION_ADDED);
            });
        } catch (Exception e) {}
    }

    private void refreshFinances() {
        String mName = currentMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl"));
        monthLabel.setText((mName + " " + currentMonth.getYear()).toUpperCase());
        AsyncRunner.run(() -> transactionDAO.getTransactionsForMonth(currentMonth.getYear(), currentMonth.getMonthValue()), data -> {
            ObservableList<Transaction> list = FXCollections.observableArrayList(data);
            FilteredList<Transaction> filtered = new FilteredList<>(list, p -> true);
            String filter = searchField.getText();
            if (filter != null && !filter.isEmpty()) {
                String l = filter.toLowerCase();
                filtered.setPredicate(t -> (t.getCategory()!=null && t.getCategory().toLowerCase().contains(l)) || (t.getDescription()!=null && t.getDescription().toLowerCase().contains(l)));
            }
            transactionTable.setItems(filtered);

            // Summaries
            double bal = 0, inc = 0, exp = 0;
            Map<String, Double> expMap = new HashMap<>();
            for (Transaction t : filtered) {
                if ("PRZYCHÓD".equals(t.getType())) { bal += t.getAmount(); inc += t.getAmount(); }
                else { bal -= t.getAmount(); exp += t.getAmount(); expMap.merge(t.getCategory(), t.getAmount(), Double::sum); }
            }
            balanceLabel.setText(String.format("%.2f", bal));
            incomeLabel.setText(String.format("+ %.2f", inc));
            expenseLabel.setText(String.format("- %.2f", exp));
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            expMap.forEach((k, v) -> pie.add(new PieChart.Data(k, v)));
            expenseChart.setData(pie);
        });
    }

    @FXML private void prevMonth() { currentMonth = currentMonth.minusMonths(1); refreshFinances(); }
    @FXML private void nextMonth() { currentMonth = currentMonth.plusMonths(1); refreshFinances(); }
    private String getIcon(String c) { if(c==null)return"📁"; String l=c.toLowerCase(); if(l.contains("jedz"))return"🍔"; if(l.contains("dom"))return"🏠"; if(l.contains("auto"))return"🚗"; return "📁"; }

    // ================== ZADANIA & CELE & RAPORTS ==================
    // (Kod skrócony - logika identyczna jak wcześniej, tylko podpięta pod nowe kontenery)

    private void setupTasks() {
        taskListView.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    HBox c = new HBox(10); c.setAlignment(Pos.CENTER_LEFT);
                    CheckBox cb = new CheckBox(); cb.setSelected(t.isDone());
                    cb.setOnAction(e -> AsyncRunner.run(() -> taskDAO.toggleStatus(t.getId(), cb.isSelected()), () -> EventBus.publish(EventBus.EventType.TASK_UPDATED)));
                    Label l = new Label(t.getTitle()); l.setStyle("-fx-text-fill: #e1e4e8;");
                    if (t.isDone()) l.setStyle("-fx-text-fill: #8b92a1; -fx-strikethrough: true;");
                    c.getChildren().addAll(cb, l);
                    setGraphic(c);
                }
            }
        });
    }
    @FXML private void handleAddTask() {
        if(taskTitleField.getText().isEmpty()) return;
        Task t = new Task(0, taskTitleField.getText(), priorityBox.getValue(), false, taskDatePicker.getValue());
        AsyncRunner.run(() -> taskDAO.addTask(t), () -> { taskTitleField.clear(); EventBus.publish(EventBus.EventType.TASK_UPDATED); });
    }
    private void refreshTasks() { AsyncRunner.run(taskDAO::getAllTasks, l -> taskListView.setItems(FXCollections.observableArrayList(l))); }

    private void setupGoals() {
        goalListView.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Goal g, boolean empty) {
                super.updateItem(g, empty);
                if(empty||g==null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); }
                else {
                    VBox c = new VBox(5); Label n = new Label(g.getName() + " " + String.format("(%.0f%%)", g.getProgress()*100));
                    n.setStyle("-fx-text-fill: white;");
                    ProgressBar pb = new ProgressBar(g.getProgress()); pb.setMaxWidth(Double.MAX_VALUE);
                    c.getChildren().addAll(n, pb); setGraphic(c);
                }
            }
        });
    }
    @FXML private void handleAddGoal() {
        try {
            Goal g = new Goal(0, goalNameField.getText(), Double.parseDouble(goalTargetField.getText()), 0, goalDatePicker.getValue());
            AsyncRunner.run(() -> goalDAO.addGoal(g), () -> { goalNameField.clear(); goalTargetField.clear(); EventBus.publish(EventBus.EventType.GOAL_ADDED); });
        } catch(Exception e){}
    }
    private void refreshGoals() { AsyncRunner.run(goalDAO::getAllGoals, l -> goalListView.setItems(FXCollections.observableArrayList(l))); }

    private void refreshReports() {
        AsyncRunner.run(transactionDAO::getAllTransactions, l -> {
            Map<String, Double> cat = new HashMap<>();
            for(Transaction t : l) if("WYDATEK".equals(t.getType())) cat.merge(t.getCategory(), t.getAmount(), Double::sum);
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            cat.forEach((k,v) -> s.getData().add(new XYChart.Data<>(k,v)));
            categoryBarChart.getData().clear(); categoryBarChart.getData().add(s);
            // Trend chart logic omitted for brevity, same as before
        });
    }

    @FXML private void handleExport() { AsyncRunner.run(transactionDAO::getAllTransactions, l -> new DataExporter().exportTransactionsToCSV(l, (Stage) rootPane.getScene().getWindow())); }
    @FXML private void handleClearDatabase() { AsyncRunner.run(() -> {/*delete sql*/}, () -> EventBus.publish(EventBus.EventType.DATA_CLEARED)); }
}