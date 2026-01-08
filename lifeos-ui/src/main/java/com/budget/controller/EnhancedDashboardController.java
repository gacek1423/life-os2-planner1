package com.budget.controller;

import com.budget.model.*;
import com.budget.modules.dashboard.DashboardService;
import com.budget.modules.reports.ReportService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class EnhancedDashboardController {
    
    // Nagłówek
    @FXML private Label dateLabel;
    @FXML private Button refreshButton;
    @FXML private Button generateReportButton;
    @FXML private Button exportDataButton;
    
    // Sekcja finansowa
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlyIncomeLabel;
    @FXML private Label monthlyExpensesLabel;
    @FXML private Label savingsLabel;
    @FXML private Label savingsRateLabel;
    @FXML private VBox financialChartContainer;
    
    // Sekcja celów
    @FXML private Label totalGoalsLabel;
    @FXML private Label completedGoalsLabel;
    @FXML private Label goalProgressLabel;
    @FXML private ProgressBar goalProgressBar;
    @FXML private VBox goalsChartContainer;
    
    // Sekcja zadań
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksTodayLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label overdueTasksLabel;
    @FXML private VBox tasksChartContainer;
    
    // Sekcja nawyków
    @FXML private Label totalHabitsLabel;
    @FXML private Label habitsCompletedTodayLabel;
    @FXML private Label habitCompletionRateLabel;
    @FXML private Label longestStreakLabel;
    @FXML private VBox habitChartContainer;
    
    // Listy dzienne
    @FXML private ListView<String> todayTasksList;
    @FXML private ListView<String> todayHabitsList;
    @FXML private ListView<String> notificationsList;
    @FXML private ListView<String> quickActionsList;
    
    // Sekcja raportów szybkich
    @FXML private VBox quickReportsContainer;
    @FXML private ComboBox<ReportType> quickReportTypeComboBox;
    @FXML private Button generateQuickReportButton;
    
    // Sekcja trendów
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private CategoryAxis trendsXAxis;
    @FXML private NumberAxis trendsYAxis;
    
    // Status
    @FXML private Label statusLabel;
    
    private DashboardService dashboardService;
    private ReportService reportService;
    
    public void setServices(DashboardService dashboardService, ReportService reportService) {
        this.dashboardService = dashboardService;
        this.reportService = reportService;
        initialize();
    }
    
    private void initialize() {
        setupQuickReportComboBox();
        loadDashboardData();
        setupQuickActions();
        createTrendsChart();
    }
    
    private void setupQuickReportComboBox() {
        quickReportTypeComboBox.getItems().setAll(
            ReportType.FINANCIAL_SUMMARY,
            ReportType.HABIT_ANALYSIS,
            ReportType.GOAL_PROGRESS,
            ReportType.TASK_PRODUCTIVITY,
            ReportType.WEEKLY_SUMMARY,
            ReportType.MONTHLY_SUMMARY
        );
        
        quickReportTypeComboBox.setConverter(new javafx.util.StringConverter<ReportType>() {
            @Override
            public String toString(ReportType type) {
                return type != null ? type.getDisplayName() : "";
            }
            @Override
            public ReportType fromString(String string) { return null; }
        });
        
        quickReportTypeComboBox.setValue(ReportType.WEEKLY_SUMMARY);
    }
    
    private void loadDashboardData() {
        Dashboard dashboard = dashboardService.getDashboard();
        
        // Ustaw datę
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateLabel.setText("Kokpit - " + LocalDate.now().format(formatter));
        
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
        createGoalsChart(dashboard);
        createTasksChart(dashboard);
        createHabitChart(dashboard);
        
        // Trendy
        updateTrendsChart();
    }
    
    private void updateFinancialSection(Dashboard dashboard) {
        totalBalanceLabel.setText(formatCurrency(dashboard.getTotalBalance()));
        monthlyIncomeLabel.setText(formatCurrency(dashboard.getMonthlyIncome()));
        monthlyExpensesLabel.setText(formatCurrency(dashboard.getMonthlyExpenses()));
        savingsLabel.setText(formatCurrency(dashboard.getSavingsThisMonth()));
        savingsRateLabel.setText(formatPercentage(dashboard.getSavingsRate()));
        
        // Zmień kolor oszczędności
        if (dashboard.getSavingsThisMonth().compareTo(BigDecimal.ZERO) >= 0) {
            savingsLabel.setTextFill(Color.GREEN);
        } else {
            savingsLabel.setTextFill(Color.RED);
        }
    }
    
    private void updateGoalsSection(Dashboard dashboard) {
        totalGoalsLabel.setText(String.valueOf(dashboard.getTotalGoals()));
        completedGoalsLabel.setText(String.valueOf(dashboard.getCompletedGoals()));
        goalProgressLabel.setText(String.format("%.1f%%", dashboard.getGoalCompletionPercentage()));
        goalProgressBar.setProgress(dashboard.getGoalCompletionPercentage() / 100.0);
        
        // Zmień kolor progress baru
        double progress = dashboard.getGoalCompletionPercentage();
        if (progress >= 75) {
            goalProgressBar.setStyle("-fx-accent: #4CAF50;");
        } else if (progress >= 50) {
            goalProgressBar.setStyle("-fx-accent: #FFC107;");
        } else {
            goalProgressBar.setStyle("-fx-accent: #F44336;");
        }
    }
    
    private void updateTasksSection(Dashboard dashboard) {
        totalTasksLabel.setText(String.valueOf(dashboard.getTotalTasks()));
        completedTasksTodayLabel.setText(String.valueOf(dashboard.getCompletedTasksToday()));
        pendingTasksLabel.setText(String.valueOf(dashboard.getPendingTasks()));
        overdueTasksLabel.setText(String.valueOf(dashboard.getOverdueTasks()));
        
        if (dashboard.getOverdueTasks() > 0) {
            overdueTasksLabel.setTextFill(Color.RED);
            overdueTasksLabel.setStyle("-fx-font-weight: bold;");
        }
    }
    
    private void updateHabitsSection(Dashboard dashboard) {
        totalHabitsLabel.setText(String.valueOf(dashboard.getTotalHabits()));
        habitsCompletedTodayLabel.setText(String.valueOf(dashboard.getHabitsCompletedToday()));
        habitCompletionRateLabel.setText(String.format("%.1f%%", dashboard.getAverageHabitCompletionRate()));
        longestStreakLabel.setText(String.valueOf(dashboard.getCurrentLongestStreak()));
    }
    
    private void updateDailyLists(Dashboard dashboard) {
        // Dzisiejsze zadania
        todayTasksList.getItems().clear();
        dashboard.getTodayTasks().forEach(task -> {
            String taskText = String.format("%s - %s", task.getTitle(), task.getPriority().getDisplayName());
            todayTasksList.getItems().add(taskText);
        });
        
        // Dzisiejsze nawyki
        todayHabitsList.getItems().clear();
        dashboard.getTodayHabits().forEach(habit -> {
            todayHabitsList.getItems().add(habit.getName());
        });
    }
    
    private void updateNotifications(Dashboard dashboard) {
        notificationsList.getItems().clear();
        
        List<String> notifications = dashboardService.getNotifications();
        notifications.forEach(notificationsList.getItems()::add);
        
        // Dodaj przypomnienia
        List<String> reminders = dashboardService.getReminders();
        reminders.forEach(notificationsList.getItems()::add);
    }
    
    private void setupQuickActions() {
        quickActionsList.getItems().clear();
        quickActionsList.getItems().addAll(
            "💰 Dodaj przychód",
            "💸 Dodaj wydatek",
            "🎯 Utwórz nowy cel",
            "📝 Dodaj zadanie",
            "🔄 Oznacz nawyk jako wykonany",
            "📊 Generuj raport tygodniowy",
            "📈 Przeglądaj statystyki",
            "⚙️ Ustawienia aplikacji"
        );
        
        quickActionsList.setOnMouseClicked(event -> {
            String selectedAction = quickActionsList.getSelectionModel().getSelectedItem();
            if (selectedAction != null) {
                handleQuickAction(selectedAction);
            }
        });
    }
    
    private void handleQuickAction(String action) {
        switch (action) {
            case "💰 Dodaj przychód":
                showAlert("Dodaj przychód", "Funkcja w przygotowaniu", Alert.AlertType.INFORMATION);
                break;
            case "💸 Dodaj wydatek":
                showAlert("Dodaj wydatek", "Funkcja w przygotowaniu", Alert.AlertType.INFORMATION);
                break;
            case "🎯 Utwórz nowy cel":
                showAlert("Utwórz cel", "Przejdź do zakładki Cele", Alert.AlertType.INFORMATION);
                break;
            case "📝 Dodaj zadanie":
                showAlert("Dodaj zadanie", "Przejdź do zakładki Zadania", Alert.AlertType.INFORMATION);
                break;
            case "🔄 Oznacz nawyk jako wykonany":
                showAlert("Nawyki", "Przejdź do zakładki Nawyki", Alert.AlertType.INFORMATION);
                break;
            case "📊 Generuj raport tygodniowy":
                generateQuickReport(ReportType.WEEKLY_SUMMARY);
                break;
            case "📈 Przeglądaj statystyki":
                generateQuickReport(ReportType.DASHBOARD_OVERVIEW);
                break;
            case "⚙️ Ustawienia aplikacji":
                showAlert("Ustawienia", "Funkcja w przygotowaniu", Alert.AlertType.INFORMATION);
                break;
        }
    }
    
    private void createFinancialChart(Dashboard dashboard) {
        financialChartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Finanse miesięczne");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Kwota (zł)");
        
        series.getData().add(new XYChart.Data<>("Przychody", dashboard.getMonthlyIncome().doubleValue()));
        series.getData().add(new XYChart.Data<>("Wydatki", dashboard.getMonthlyExpenses().doubleValue()));
        series.getData().add(new XYChart.Data<>("Oszczędności", dashboard.getSavingsThisMonth().doubleValue()));
        
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(200);
        
        financialChartContainer.getChildren().add(barChart);
    }
    
    private void createGoalsChart(Dashboard dashboard) {
        goalsChartContainer.getChildren().clear();
        
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Status celów");
        
        PieChart.Data activeSlice = new PieChart.Data("Aktywne", dashboard.getActiveGoals());
        PieChart.Data completedSlice = new PieChart.Data("Zakończone", dashboard.getCompletedGoals());
        PieChart.Data otherSlice = new PieChart.Data("Pozostałe", 
            dashboard.getTotalGoals() - dashboard.getActiveGoals() - dashboard.getCompletedGoals());
        
        pieChart.getData().addAll(activeSlice, completedSlice, otherSlice);
        pieChart.setPrefHeight(200);
        
        goalsChartContainer.getChildren().add(pieChart);
    }
    
    private void createTasksChart(Dashboard dashboard) {
        tasksChartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Zadania");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        series.getData().add(new XYChart.Data<>("Wszystkie", dashboard.getTotalTasks()));
        series.getData().add(new XYChart.Data<>("Dzisiaj", dashboard.getCompletedTasksToday()));
        series.getData().add(new XYChart.Data<>("Oczekujące", dashboard.getPendingTasks()));
        series.getData().add(new XYChart.Data<>("Zaległe", dashboard.getOverdueTasks()));
        
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(200);
        
        tasksChartContainer.getChildren().add(barChart);
    }
    
    private void createHabitChart(Dashboard dashboard) {
        habitChartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("% Realizacji");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Nawyki");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        series.getData().add(new XYChart.Data<>("Ogółem", dashboard.getAverageHabitCompletionRate()));
        series.getData().add(new XYChart.Data<>("Dzisiaj", 
            dashboard.getTotalHabits() > 0 ? 
            (double) dashboard.getHabitsCompletedToday() / dashboard.getTotalHabits() * 100 : 0));
        
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(200);
        
        habitChartContainer.getChildren().add(barChart);
    }
    
    private void createTrendsChart() {
        trendsChart.setTitle("Trendy produktywności (ostatnie 7 dni)");
        
        trendsXAxis.setLabel("Dzień");
        trendsYAxis.setLabel("Wartość");
        
        XYChart.Series<String, Number> habitSeries = new XYChart.Series<>();
        habitSeries.setName("Nawyki %");
        
        XYChart.Series<String, Number> taskSeries = new XYChart.Series<>();
        taskSeries.setName("Zadania %");
        
        // Dane przykładowe - w rzeczywistej aplikacji pobierane z serwisu
        String[] days = {"Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nie"};
        double[] habitData = {85, 90, 78, 92, 88, 95, 87};
        double[] taskData = {75, 82, 70, 85, 80, 90, 78};
        
        for (int i = 0; i < days.length; i++) {
            habitSeries.getData().add(new XYChart.Data<>(days[i], habitData[i]));
            taskSeries.getData().add(new XYChart.Data<>(days[i], taskData[i]));
        }
        
        trendsChart.getData().clear();
        trendsChart.getData().addAll(habitSeries, taskSeries);
        trendsChart.setPrefHeight(250);
    }
    
    private void updateTrendsChart() {
        // W rzeczywistej aplikacji - odśwież dane z serwisu
        createTrendsChart();
    }
    
    @FXML
    private void refreshDashboard() {
        loadDashboardData();
        statusLabel.setText("Dashboard odświeżony");
    }
    
    @FXML
    private void generateReport() {
        ReportType selectedType = quickReportTypeComboBox.getValue();
        if (selectedType != null) {
            generateQuickReport(selectedType);
        }
    }
    
    private void generateQuickReport(ReportType type) {
        try {
            Report report = reportService.generateReport(type, ReportPeriod.LAST_7_DAYS);
            statusLabel.setText("Raport wygenerowany: " + report.getTitle());
            
            // Tutaj można otworzyć nowe okno z raportem
            showAlert("Raport wygenerowany", 
                "Raport '" + report.getTitle() + "' został wygenerowany pomyślnie!", 
                Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            statusLabel.setText("Błąd generowania raportu: " + e.getMessage());
            showAlert("Błąd", "Nie udało się wygenerować raportu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void exportData() {
        try {
            Dashboard dashboard = dashboardService.getDashboard();
            
            // Generuj raport kokpitowy i eksportuj
            Report report = reportService.generateDashboardReport();
            String filePath = reportService.exportToCSV(report);
            
            statusLabel.setText("Dane wyeksportowane do: " + filePath);
            showAlert("Eksport zakończony", "Dane zostały wyeksportowane do pliku CSV!", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            statusLabel.setText("Błąd eksportu: " + e.getMessage());
            showAlert("Błąd eksportu", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%.2f zł", amount.doubleValue());
    }
    
    private String formatPercentage(BigDecimal percentage) {
        return String.format("%.1f%%", percentage.doubleValue());
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}