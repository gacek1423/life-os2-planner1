package com.budget.controller.modules;

import com.budget.dao.BudgetDAO;
import com.budget.dao.PurseDAO;
import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Transaction;
import com.budget.modules.finance.domain.Purse;
import com.budget.modules.finance.events.TransactionAddedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceController {

    // --- UI: Tab 1 (Operacje) ---
    @FXML private Label balanceLabel, incomeLabel, expenseLabel, monthLabel;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colCategory, colDesc, colType;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private PieChart expenseChart;
    @FXML private LineChart<String, Number> trendChart;

    // Inputs
    @FXML private TextField amountField, descField;
    @FXML private ComboBox<String> typeBox, categoryBox;
    @FXML private DatePicker datePicker;

    // --- UI: Tab 2 (Budżet) ---
    @FXML private VBox budgetListContainer;
    @FXML private ComboBox<String> newBudgetCategory;
    @FXML private TextField newBudgetLimit;

    // --- UI: Command Palette ---
    @FXML private StackPane commandPane;
    @FXML private TextField commandField;
    @FXML private ListView<String> commandResult;

    // --- DATA ---
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final PurseDAO purseDAO = new PurseDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    private YearMonth currentMonth = YearMonth.now();
    private final ObservableList<String> commandHistory = FXCollections.observableArrayList();
    private int selectedCommandIndex = -1;

    @FXML
    public void initialize() {
        setupTable();
        setupInputs();
        setupCommandPalette();

        // Bezpieczne ustawianie skrótów klawiszowych (dopiero gdy scena istnieje)
        Platform.runLater(this::setupKeyboardShortcuts);

        refreshFinances();
        newBudgetCategory.setOnAction(e -> suggestBudget(newBudgetCategory.getValue()));
    }

    public void refreshFinances() {
        if (monthLabel != null) {
            monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pl"))).toUpperCase());
        }

        AsyncRunner.run(() -> {
            List<Transaction> transactions = transactionDAO.getTransactionsForMonth(currentMonth.getYear(), currentMonth.getMonthValue());
            List<Purse> purses = purseDAO.getAllPurses();
            Map<String, Double> budgets = budgetDAO.getAllBudgets();
            return new FinanceData(transactions, purses, budgets);
        }, data -> {
            if (transactionTable != null) transactionTable.getItems().setAll(data.transactions);
            updateChart(data.transactions);
            updateTrendChart(data.transactions);
            updateSummary(data.transactions, data.purses);
            renderBudgetList(data.transactions, data.budgets);
        });
    }

    // ==================== COMMAND PALETTE (NAPRAWIONE) ====================

    private void setupCommandPalette() {
        // Obsługa klawiszy wewnątrz palety
        commandPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hideCommandPalette();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                executeCommand();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                navigateCommands(1);
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                navigateCommands(-1);
                event.consume();
            }
        });

        // Sugestie
        commandField.textProperty().addListener((obs, old, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) updateCommandSuggestions(newText);
            else showDefaultCommands();
        });
    }

    // TO BYŁ BŁĄD: Scaliłem dwie metody w jedną publiczną @FXML
    @FXML
    public void showCommandPalette() {
        commandPane.setVisible(true);
        commandPane.setMouseTransparent(false); // Odblokuj myszkę dla palety
        commandField.requestFocus();
        commandField.clear();
        showDefaultCommands();
    }

    private void hideCommandPalette() {
        commandPane.setVisible(false);
        commandPane.setMouseTransparent(true); // Przepuść kliknięcia do tła
        commandField.clear();
        commandResult.getItems().clear();
        selectedCommandIndex = -1;
    }

    // --- Reszta metod Command Palette (skrócona dla czytelności, bez zmian logiki) ---
    private void showDefaultCommands() {
        ObservableList<String> suggestions = FXCollections.observableArrayList(
                "📊 dodaj wydatek [kwota] [kategoria] [opis]", "➕ dodaj przychód [kwota] [kategoria] [opis]",
                "📈 budżet", "📅 miesiąc [+/-]", "🗑️ usuń ostatni", "❌ anuluj"
        );
        commandResult.setItems(suggestions);
    }

    private void updateCommandSuggestions(String input) {
        String[] parts = input.toLowerCase().split(" ");
        String cmd = parts[0];
        List<String> s = new ArrayList<>();
        if (cmd.startsWith("dodaj")) s.add("📊 " + input + " [opis]...");
        else if (cmd.startsWith("budżet")) s.add("📈 Przełącz na budżet");
        else s.add("Szukaj: " + input);
        commandResult.setItems(FXCollections.observableArrayList(s));
    }

    private void navigateCommands(int dir) {
        int size = commandResult.getItems().size();
        if (size == 0) return;
        selectedCommandIndex = (selectedCommandIndex + dir + size) % size;
        commandResult.getSelectionModel().select(selectedCommandIndex);
        commandResult.scrollTo(selectedCommandIndex);
    }

    private void executeCommand() {
        String cmd = commandField.getText().trim();
        if (cmd.isEmpty()) return;
        // Prosta obsługa komend
        if (cmd.startsWith("dodaj wydatek")) {
            try {
                String[] p = cmd.split(" ", 4);
                addTransactionDirect("WYDATEK", p.length > 3 ? p[3] : "Szybki wydatek", Double.parseDouble(p[2]), "Inne");
            } catch (Exception e) { showAlert("Błąd", "Format: dodaj wydatek KWOTA OPIS"); }
        } else if (cmd.contains("anuluj") || cmd.contains("esc")) {
            hideCommandPalette();
        }
        hideCommandPalette();
    }

    private void addTransactionDirect(String type, String desc, double amount, String cat) {
        Transaction t = new Transaction(0, type, cat, amount, LocalDate.now(), desc);
        AsyncRunner.run(() -> {
            transactionDAO.addTransaction(t);
            EventBus.publish(new TransactionAddedEvent(t));
        }, this::refreshFinances);
    }

    @FXML public void handleOverlayClick(javafx.scene.input.MouseEvent event) {
        if (event.getTarget() == commandPane) hideCommandPalette();
    }

    // ==================== POZOSTAŁE METODY UI ====================

    private void setupKeyboardShortcuts() {
        if (transactionTable.getScene() != null) {
            transactionTable.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.K) {
                    showCommandPalette();
                    event.consume();
                }
            });
        }
    }

    @FXML public void addTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText().replace(",", "."));
            addTransactionDirect(typeBox.getValue(), descField.getText(), amount, categoryBox.getValue());
            amountField.clear(); descField.clear();
        } catch (Exception e) { showAlert("Błąd", "Sprawdź dane transakcji."); }
    }

    // Reszta metod (metody pomocnicze, wykresy, budżet) - bez zmian w logice, tylko czystość kodu
    private void renderBudgetList(List<Transaction> transactions, Map<String, Double> budgets) {
        if (budgetListContainer == null) return;
        budgetListContainer.getChildren().clear();
        Map<String, Double> spentMap = transactions.stream().filter(t -> "WYDATEK".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        budgets.forEach((cat, limit) -> {
            if (limit <= 0) return;
            double spent = spentMap.getOrDefault(cat, 0.0);
            double progress = spent / limit;
            VBox card = new VBox(5);
            card.getStyleClass().add("budget-card");
            if (progress >= 1.0) card.getStyleClass().add("budget-card-alert");
            card.setStyle("-fx-background-color: #262a35; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #323642; -fx-border-radius: 8;");

            Label l = new Label(cat.toUpperCase() + " (" + String.format("%.0f%%", progress * 100) + ")");
            l.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            ProgressBar pb = new ProgressBar(progress);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setStyle("-fx-accent: " + (progress > 1.0 ? "#ff6b6b" : "#51cf66") + ";");

            card.getChildren().addAll(l, pb);
            budgetListContainer.getChildren().add(card);
        });
    }

    private void updateChart(List<Transaction> transactions) {
        if (expenseChart == null) return;
        Map<String, Double> data = transactions.stream().filter(t -> "WYDATEK".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        data.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
        expenseChart.setData(pieData);
    }

    private void updateTrendChart(List<Transaction> transactions) {
        if (trendChart == null) return;
        trendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        transactions.stream().filter(t -> "WYDATEK".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getDate, Collectors.summingDouble(Transaction::getAmount)))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey().format(DateTimeFormatter.ofPattern("dd")), e.getValue())));
        trendChart.getData().add(series);
    }

    private void updateSummary(List<Transaction> t, List<Purse> p) {
        double total = p.stream().mapToDouble(Purse::getAllocatedAmount).sum();
        if (balanceLabel != null) balanceLabel.setText(String.format("%.2f PLN", total));
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colDate.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean e) { super.updateItem(d, e); if (!e && d != null) setText(d.format(DateTimeFormatter.ofPattern("dd.MM"))); }
        });
        colAmount.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double a, boolean e) { super.updateItem(a, e); if (!e && a != null) { setText(String.format("%.2f zł", a)); setStyle("-fx-text-fill: " + (a > 0 ? "#51cf66" : "#ff6b6b")); } }
        });
    }

    private void setupInputs() {
        typeBox.getItems().addAll("PRZYCHÓD", "WYDATEK"); typeBox.getSelectionModel().selectFirst();
        categoryBox.getItems().addAll("Jedzenie", "Dom", "Paliwo", "Rozrywka", "Pensja", "Inne");
        newBudgetCategory.getItems().addAll(categoryBox.getItems());
        datePicker.setValue(LocalDate.now());
    }

    @FXML public void handleAddNewBudget() {
        try {
            String c = newBudgetCategory.getValue();
            double l = Double.parseDouble(newBudgetLimit.getText());
            AsyncRunner.run(() -> budgetDAO.setBudget(c, l), this::refreshFinances);
        } catch(Exception e) { showAlert("Błąd", "Błędne dane"); }
    }

    private void suggestBudget(String c) {
        // Mock suggestion
        if(c != null) newBudgetLimit.setText("1500");
    }

    @FXML public void prevMonth() { currentMonth = currentMonth.minusMonths(1); refreshFinances(); }
    @FXML public void nextMonth() { currentMonth = currentMonth.plusMonths(1); refreshFinances(); }
    @FXML public void refreshBudgets() { refreshFinances(); }

    private void showAlert(String t, String c) { new Alert(Alert.AlertType.INFORMATION, c).showAndWait(); }
    private record FinanceData(List<Transaction> transactions, List<Purse> purses, Map<String, Double> budgets) {}
}