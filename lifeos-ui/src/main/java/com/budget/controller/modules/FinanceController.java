package com.budget.controller.modules;

import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Transaction;
import com.budget.modules.finance.events.TransactionAddedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceController {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colCategory, colDesc, colType;
    @FXML private TableColumn<Transaction, Double> colAmount;

    @FXML private PieChart expenseChart;
    @FXML private ListView<HBox> budgetListView;
    @FXML private Label balanceLabel, incomeLabel, expenseLabel, monthLabel;

    @FXML private TextField amountField, descField, searchField;
    @FXML private ComboBox<String> typeBox, categoryBox;
    @FXML private DatePicker datePicker;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private YearMonth currentMonth = YearMonth.now();

    @FXML
    public void initialize() {
        setupTable();

        typeBox.setItems(FXCollections.observableArrayList("WYDATEK", "PRZYCHÓD"));
        typeBox.getSelectionModel().selectFirst();

        categoryBox.setItems(FXCollections.observableArrayList(
                "🏠 Mieszkanie", "🛒 Jedzenie", "🚗 Transport",
                "💊 Zdrowie", "🎉 Rozrywka", "📚 Edukacja", "Inne"
        ));

        datePicker.setValue(LocalDate.now());
        searchField.textProperty().addListener((obs, o, n) -> refreshFinances());

        refreshFinances();
    }

    @FXML
    public void addTransaction() {
        if (amountField.getText().isEmpty()) return;

        String category = categoryBox.getValue();
        if (category == null || category.trim().isEmpty()) category = categoryBox.getEditor().getText();
        if (category == null) category = "Inne";

        try {
            double amt = Double.parseDouble(amountField.getText().replace(",", "."));
            Transaction t = new Transaction(0, typeBox.getValue(), category, amt, datePicker.getValue(), descField.getText());

            AsyncRunner.run(() -> {
                transactionDAO.addTransaction(t);
                EventBus.publish(new TransactionAddedEvent(t));
            }, () -> {
                amountField.clear(); descField.clear();
                refreshFinances();
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void prevMonth() { currentMonth = currentMonth.minusMonths(1); refreshFinances(); }
    @FXML public void nextMonth() { currentMonth = currentMonth.plusMonths(1); refreshFinances(); }

    public void refreshFinances() {
        String mName = currentMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl"));
        monthLabel.setText((mName + " " + currentMonth.getYear()).toUpperCase());

        AsyncRunner.run(() -> transactionDAO.getTransactionsForMonth(currentMonth.getYear(), currentMonth.getMonthValue()),
                list -> {
                    // Filtrowanie
                    String filter = searchField.getText().toLowerCase();
                    List<Transaction> filtered = list.stream()
                            .filter(t -> filter.isEmpty() || t.getDescription().toLowerCase().contains(filter) || t.getCategory().toLowerCase().contains(filter))
                            .collect(Collectors.toList());

                    transactionTable.setItems(FXCollections.observableArrayList(filtered));
                    updateStats(filtered);
                });
    }

    private void updateStats(List<Transaction> list) {
        double income = list.stream().filter(t -> "PRZYCHÓD".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double expense = list.stream().filter(t -> "WYDATEK".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();

        incomeLabel.setText(String.format("+%.2f", income));
        expenseLabel.setText(String.format("-%.2f", expense));
        balanceLabel.setText(String.format("%.2f PLN", income - expense));

        // Wykres
        Map<String, Double> byCat = list.stream()
                .filter(t -> "WYDATEK".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        expenseChart.setData(FXCollections.observableArrayList(
                byCat.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()
        ));
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
    }
}