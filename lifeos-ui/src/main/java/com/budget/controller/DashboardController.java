package com.budget.controller;

import com.budget.model.Dashboard;
import com.budget.model.HabitCategory; // <--- NAPRAWIONO: Dodano brakujący import
import com.budget.modules.dashboard.DashboardService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label dateLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlyIncomeLabel;
    @FXML private Label monthlyExpensesLabel;
    @FXML private Label savingsLabel;
    @FXML private Label savingsRateLabel;

    @FXML private Label totalGoalsLabel;
    @FXML private Label completedGoalsLabel;
    @FXML private Label goalProgressLabel;

    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksTodayLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label overdueTasksLabel;

    @FXML private Label totalHabitsLabel;
    @FXML private Label habitsCompletedTodayLabel;
    @FXML private Label habitCompletionRateLabel;
    @FXML private Label longestStreakLabel;

    @FXML private ListView<String> todayTasksList;
    @FXML private ListView<String> todayHabitsList;
    @FXML private ListView<String> notificationsList;

    @FXML private VBox financialChartContainer;
    @FXML private VBox habitChartContainer;

    private DashboardService dashboardService;

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (dashboardService == null) return;

        Dashboard dashboard = dashboardService.getDashboard();

        // Ustaw datę
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (dateLabel != null) dateLabel.setText("Dashboard - " + LocalDate.now().format(formatter));

        // Dane finansowe
        updateFinancialSection(dashboard);

        // Cele
        updateGoalsSection(dashboard);

        // Zadania
        updateTasksSection(dashboard);

        // Nawyki
        updateHabitsSection(dashboard);

        // Listy dzienne
        updateDailyLists(dashboard);

        // Powiadomienia
        updateNotifications(dashboard);

        // Wykresy
        createFinancialChart(dashboard);
        createHabitChart(dashboard);
    }

    private void updateFinancialSection(Dashboard dashboard) {
        if (totalBalanceLabel != null) totalBalanceLabel.setText(formatCurrency(dashboard.getTotalBalance()));
        if (monthlyIncomeLabel != null) monthlyIncomeLabel.setText(formatCurrency(dashboard.getMonthlyIncome()));
        if (monthlyExpensesLabel != null) monthlyExpensesLabel.setText(formatCurrency(dashboard.getMonthlyExpenses()));
        if (savingsLabel != null) {
            savingsLabel.setText(formatCurrency(dashboard.getSavingsThisMonth()));
            // Zmień kolor oszczędności
            if (dashboard.getSavingsThisMonth().compareTo(BigDecimal.ZERO) >= 0) {
                savingsLabel.setTextFill(Color.GREEN);
            } else {
                savingsLabel.setTextFill(Color.RED);
            }
        }
        if (savingsRateLabel != null) savingsRateLabel.setText(formatPercentage(dashboard.getSavingsRate()));
    }

    private void updateGoalsSection(Dashboard dashboard) {
        if (totalGoalsLabel != null) totalGoalsLabel.setText(String.valueOf(dashboard.getTotalGoals()));
        if (completedGoalsLabel != null) completedGoalsLabel.setText(String.valueOf(dashboard.getCompletedGoals()));
        if (goalProgressLabel != null) goalProgressLabel.setText(String.format("%.1f%%", dashboard.getGoalCompletionPercentage()));
    }

    private void updateTasksSection(Dashboard dashboard) {
        if (totalTasksLabel != null) totalTasksLabel.setText(String.valueOf(dashboard.getTotalTasks()));
        if (completedTasksTodayLabel != null) completedTasksTodayLabel.setText(String.valueOf(dashboard.getCompletedTasksToday()));
        if (pendingTasksLabel != null) pendingTasksLabel.setText(String.valueOf(dashboard.getPendingTasks()));
        if (overdueTasksLabel != null) {
            overdueTasksLabel.setText(String.valueOf(dashboard.getOverdueTasks()));
            if (dashboard.getOverdueTasks() > 0) {
                overdueTasksLabel.setTextFill(Color.RED);
            }
        }
    }

    private void updateHabitsSection(Dashboard dashboard) {
        if (totalHabitsLabel != null) totalHabitsLabel.setText(String.valueOf(dashboard.getTotalHabits()));
        if (habitsCompletedTodayLabel != null) habitsCompletedTodayLabel.setText(String.valueOf(dashboard.getHabitsCompletedToday()));
        if (habitCompletionRateLabel != null) habitCompletionRateLabel.setText(String.format("%.1f%%", dashboard.getAverageHabitCompletionRate()));
        if (longestStreakLabel != null) longestStreakLabel.setText(String.valueOf(dashboard.getCurrentLongestStreak()));
    }

    private void updateDailyLists(Dashboard dashboard) {
        // Dzisiejsze zadania
        if (todayTasksList != null) {
            todayTasksList.getItems().clear();
            dashboard.getTodayTasks().forEach(task -> {
                String priorityName = task.getPriority() != null ? task.getPriority().name() : "NORMAL";
                String taskText = String.format("%s - %s", task.getTitle(), priorityName);
                todayTasksList.getItems().add(taskText);
            });
        }

        // Dzisiejsze nawyki
        if (todayHabitsList != null) {
            todayHabitsList.getItems().clear();
            dashboard.getTodayHabits().forEach(habit -> {
                todayHabitsList.getItems().add(habit.getName());
            });
        }
    }

    private void updateNotifications(Dashboard dashboard) {
        if (notificationsList != null) {
            notificationsList.getItems().clear();

            List<String> notifications = dashboardService.getNotifications();
            notifications.forEach(notificationsList.getItems()::add);

            // Dodaj przypomnienia
            List<String> reminders = dashboardService.getReminders();
            reminders.forEach(notificationsList.getItems()::add);
        }
    }

    private void createFinancialChart(Dashboard dashboard) {
        if (financialChartContainer == null) return;

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Finanse Miesięczne");

        series.getData().add(new XYChart.Data<>("Przychody", dashboard.getMonthlyIncome().doubleValue()));
        series.getData().add(new XYChart.Data<>("Wydatki", dashboard.getMonthlyExpenses().doubleValue()));
        series.getData().add(new XYChart.Data<>("Oszczędności", dashboard.getSavingsThisMonth().doubleValue()));

        barChart.getData().add(series);
        barChart.setLegendVisible(false);

        financialChartContainer.getChildren().clear();
        financialChartContainer.getChildren().add(barChart);
    }

    private void createHabitChart(Dashboard dashboard) {
        if (habitChartContainer == null) return;

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("% Realizacji");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Realizacja Nawyków");

        Map<HabitCategory, Double> completionByCategory = dashboardService.getHabitCompletionByCategory();
        for (Map.Entry<HabitCategory, Double> entry : completionByCategory.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().name(), // Używamy .name() zamiast .getDisplayName(), jeśli to Enum
                    entry.getValue()
            ));
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);

        habitChartContainer.getChildren().clear();
        habitChartContainer.getChildren().add(barChart);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0.00 zł";
        return String.format("%.2f zł", amount.doubleValue());
    }

    private String formatPercentage(BigDecimal percentage) {
        if (percentage == null) return "0.0%";
        return String.format("%.1f%%", percentage.doubleValue());
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardData();
    }

    @FXML
    private void showWeeklyReport() {
        if (dashboardService == null) return;
        Map<String, Object> weeklyProgress = dashboardService.getWeeklyProgress();
        System.out.println("Raport tygodniowy: " + weeklyProgress);
    }
}