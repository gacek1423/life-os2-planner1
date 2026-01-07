package com.budget.controller.managers;

import com.budget.dao.TransactionDAO;
import com.budget.infrastructure.AsyncRunner;
import com.budget.model.Transaction;
import com.budget.service.DataExporter;
import com.budget.service.ReportService;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class ReportManager {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ReportService reportService = new ReportService();
    private final DataExporter dataExporter = new DataExporter();

    private final AreaChart<String, Number> trendChart;
    private final BarChart<String, Number> savingsChart;
    private final BarChart<String, Number> categoryChart;
    private final Label avgIncome, avgExpense, totalSavings;

    public ReportManager(AreaChart<String, Number> trendChart, BarChart<String, Number> savingsChart,
                         BarChart<String, Number> categoryChart, Label avgIncome, Label avgExpense, Label totalSavings) {
        this.trendChart = trendChart;
        this.savingsChart = savingsChart;
        this.categoryChart = categoryChart;
        this.avgIncome = avgIncome;
        this.avgExpense = avgExpense;
        this.totalSavings = totalSavings;
    }

    public void setup() {
        // Formatowanie osi (k, M)
        NumberAxis yAxisTrend = (NumberAxis) trendChart.getYAxis();
        yAxisTrend.setTickLabelFormatter(getCurrencyConverter());
        NumberAxis xAxisCat = (NumberAxis) categoryChart.getYAxis();
        xAxisCat.setTickLabelFormatter(getCurrencyConverter());

        refreshReports();
    }

    public void generateReport(Stage stage) {
        String html = reportService.generateMonthlyReportHtml(YearMonth.now());
        dataExporter.exportHtmlReport(html, "Raport_Finansowy.html", stage);
    }

    public void refreshReports() {
        AsyncRunner.run(transactionDAO::getAllTransactions, allData -> {
            // ... (TUTAJ WKLEJ CAŁĄ LOGIKĘ refreshReports() z DashboardController) ...
            // Ze względu na limit znaków, skopiuj ciało metody refreshReports()
            // z poprzedniej wersji DashboardController i wklej tutaj.
        });
    }

    private StringConverter<Number> getCurrencyConverter() {
        return new StringConverter<>() {
            @Override public String toString(Number object) {
                double val = object.doubleValue();
                if (Math.abs(val) >= 1_000_000) return String.format("%.1fM", val / 1_000_000);
                if (Math.abs(val) >= 1_000) return String.format("%.0fk", val / 1_000);
                return String.format("%.0f", val);
            }
            @Override public Number fromString(String string) { return 0; }
        };
    }
}