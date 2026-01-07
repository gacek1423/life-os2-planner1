package com.budget.controller.managers;

import com.budget.dao.BudgetDAO;
import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.infrastructure.EventBus;
import com.budget.model.Transaction;
import com.budget.modules.finance.events.TransactionAddedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class FinanceManager {

    // DAO
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    // UI Elements (wstrzykiwane z kontrolera)
    private final TableView<Transaction> transactionTable;
    private final PieChart expenseChart;
    private final ListView<HBox> budgetListView;
    private final Label balanceLabel, incomeLabel, expenseLabel, monthLabel;
    private final TextField amountField, categoryField, descField, searchField;
    private final ComboBox<String> typeBox;
    private final DatePicker datePicker;

    // State
    private YearMonth currentMonth = YearMonth.now();

    // Konstruktor - tu przekazujemy kontrolki z FXML
    public FinanceManager(TableView<Transaction> table, PieChart chart, ListView<HBox> budgetList,
                          Label balance, Label income, Label expense, Label month,
                          TextField amount, TextField category, TextField desc, TextField search,
                          ComboBox<String> type, DatePicker date) {
        this.transactionTable = table;
        this.expenseChart = chart;
        this.budgetListView = budgetList;
        this.balanceLabel = balance;
        this.incomeLabel = income;
        this.expenseLabel = expense;
        this.monthLabel = month;
        this.amountField = amount;
        this.categoryField = category;
        this.descField = desc;
        this.searchField = search;
        this.typeBox = type;
        this.datePicker = date;
    }

    public void setup() {
        setupTable();
        typeBox.setItems(FXCollections.observableArrayList("WYDATEK", "PRZYCHÓD"));
        typeBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());

        searchField.textProperty().addListener((obs, oldV, newV) -> refreshFinances());
        refreshFinances();
    }

    private void setupTable() {
        // ... (Tu przenieś logikę konfiguracji kolumn z DashboardController) ...
        // Skróciłem dla czytelności - skopiuj kod z metody setupTable() w DashboardController
    }

    public void refreshFinances() {
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

            // Obliczenia
            double bal = 0, inc = 0, exp = 0;
            Map<String, Double> currentSpending = new HashMap<>();

            for (Transaction t : filtered) {
                if ("PRZYCHÓD".equals(t.getType())) {
                    bal += t.getAmount();
                    inc += t.getAmount();
                } else {
                    bal -= t.getAmount();
                    exp += t.getAmount();
                    currentSpending.merge(t.getCategory(), t.getAmount(), Double::sum);
                }
            }

            balanceLabel.setText(String.format("%.2f", bal));
            incomeLabel.setText(String.format("+ %.2f", inc));
            expenseLabel.setText(String.format("- %.2f", exp));

            // Wykres
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            currentSpending.forEach((k, v) -> pie.add(new PieChart.Data(k, v)));
            expenseChart.setData(pie);

            updateBudgetList(currentSpending);
        });
    }

    public void addTransaction() {
        if (amountField.getText().isEmpty()) return;
        try {
            double amt = Double.parseDouble(amountField.getText().replace(",", "."));
            Transaction t = new Transaction(0, typeBox.getValue(), categoryField.getText(), amt, datePicker.getValue(), descField.getText());

            AsyncRunner.run(() -> {
                transactionDAO.addTransaction(t);
                EventBus.publish(new TransactionAddedEvent(t));
            }, () -> {
                amountField.clear(); categoryField.clear(); descField.clear();
                refreshFinances();
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void prevMonth() { currentMonth = currentMonth.minusMonths(1); refreshFinances(); }
    public void nextMonth() { currentMonth = currentMonth.plusMonths(1); refreshFinances(); }

    // Przenieś tu też metodę updateBudgetList() z DashboardController
    private void updateBudgetList(Map<String, Double> spending) {
        // ... SKOPIUJ KOD updateBudgetList Z DashboardController ...
        // Pamiętaj, żeby używać pola 'budgetDAO' i 'budgetListView' z tej klasy
    }
}