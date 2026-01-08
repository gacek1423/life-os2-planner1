package com.budget.controller.modules;

import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportController {
    @FXML private Label repAvgIncome, repAvgExpense, repTotalSavings;
    @FXML private AreaChart<String, Number> reportTrendChart;
    @FXML private BarChart<String, Number> reportSavingsChart, reportCategoryChart;

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {
        // Domyślnie ładujemy raport przy starcie
        refreshReports();
    }

    @FXML
    public void generateReport() {
        // Logika generowania HTML/PDF
        System.out.println("Generowanie raportu...");
    }

    public void refreshReports() {
        AsyncRunner.run(() -> transactionDAO.getAllTransactions(), transactions -> {
            if (transactions.isEmpty()) return;

            // 1. Wykres Kategorii
            Map<String, Double> byCat = transactions.stream()
                    .filter(t -> "WYDATEK".equals(t.getType()))
                    .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

            XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
            catSeries.setName("Wydatki");
            byCat.forEach((k, v) -> catSeries.getData().add(new XYChart.Data<>(k, v)));

            reportCategoryChart.getData().clear();
            reportCategoryChart.getData().add(catSeries);

            // 2. Proste KPI (średnie)
            double totalInc = transactions.stream().filter(t -> "PRZYCHÓD".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
            double totalExp = transactions.stream().filter(t -> "WYDATEK".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();

            repAvgIncome.setText(String.format("%.0f PLN", totalInc / 12)); // Uproszczenie
            repTotalSavings.setText(String.format("%.0f PLN", totalInc - totalExp));
        });
    }
}