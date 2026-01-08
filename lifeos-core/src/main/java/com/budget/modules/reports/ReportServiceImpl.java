package com.budget.modules.reports;

import com.budget.model.*;
import com.budget.model.ReportPeriod;
import com.budget.modules.habits.HabitService;
import com.budget.modules.dashboard.DashboardService;
import com.budget.modules.goals.GoalService;
import com.budget.modules.tasks.TaskService;
import com.budget.modules.finance.domain.PurseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportServiceImpl implements ReportService {
    
    private final HabitService habitService;
    private final DashboardService dashboardService;
    private final GoalService goalService;
    private final TaskService taskService;
    private final PurseService purseService;
    
    private final Map<Long, Report> reports = new HashMap<>();
    private final Map<Long, Report> templates = new HashMap<>();
    private long reportIdCounter = 1;
    private long templateIdCounter = 1;
    
    public ReportServiceImpl(HabitService habitService, DashboardService dashboardService,
                           GoalService goalService, TaskService taskService, PurseService purseService) {
        this.habitService = habitService;
        this.dashboardService = dashboardService;
        this.goalService = goalService;
        this.taskService = taskService;
        this.purseService = purseService;
    }
    
    @Override
    public Report generateReport(ReportType type, ReportPeriod period) {
        LocalDate[] dates = calculatePeriodDates(period);
        return generateReport(type, dates[0], dates[1]);
    }
    
    @Override
    public Report generateReport(ReportType type, LocalDate startDate, LocalDate endDate) {
        Report report = new Report();
        report.setId(reportIdCounter++);
        report.setType(type);
        report.setPeriod(ReportPeriod.CUSTOM);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy("System");
        report.setFormat(ReportFormat.HTML);
        
        switch (type) {
            case FINANCIAL_SUMMARY:
                return generateFinancialReportData(report);
            case HABIT_ANALYSIS:
                return generateHabitAnalysisReportData(report);
            case GOAL_PROGRESS:
                return generateGoalProgressReportData(report);
            case TASK_PRODUCTIVITY:
                return generateTaskProductivityReportData(report);
            case DASHBOARD_OVERVIEW:
                return generateDashboardReportData(report);
            case WEEKLY_SUMMARY:
                return generateWeeklySummaryReportData(report);
            case MONTHLY_SUMMARY:
                return generateMonthlySummaryReportData(report);
            default:
                return generateCustomReportData(report);
        }
    }
    
    @Override
    public Report generateCustomReport(Map<String, Object> parameters) {
        Report report = new Report();
        report.setId(reportIdCounter++);
        report.setType(ReportType.CUSTOM);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy("User");
        report.setData(parameters);
        
        return generateCustomReportData(report);
    }
    
    private Report generateFinancialReportData(Report report) {
        report.setTitle("Raport finansowy");
        report.setDescription("Podsumowanie sytuacji finansowej za okres " + 
                            report.getStartDate() + " - " + report.getEndDate());
        
        Map<String, Object> data = new HashMap<>();
        Dashboard dashboard = dashboardService.getDashboardForDate(report.getEndDate());
        
        // Sekcja 1: Podsumowanie
        ReportSection summarySection = new ReportSection();
        summarySection.setTitle("Podsumowanie finansowe");
        summarySection.setType("summary");
        summarySection.setOrder(1);
        
        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("totalBalance", dashboard.getTotalBalance());
        summaryData.put("monthlyIncome", dashboard.getMonthlyIncome());
        summaryData.put("monthlyExpenses", dashboard.getMonthlyExpenses());
        summaryData.put("savingsThisMonth", dashboard.getSavingsThisMonth());
        summaryData.put("savingsRate", dashboard.getSavingsRate());
        summarySection.setData(summaryData);
        
        // Sekcja 2: Wykres
        ReportSection chartSection = new ReportSection();
        chartSection.setTitle("Struktura wydatków");
        chartSection.setType("chart");
        chartSection.setOrder(2);
        
        ReportChart pieChart = new ReportChart();
        pieChart.setId("expenses-chart");
        pieChart.setTitle("Wydatki według kategorii");
        pieChart.setType("pie");
        pieChart.setData(createSamplePieChartData());
        
        chartSection.setCharts(List.of(pieChart));
        
        // Sekcja 3: Tabela transakcji
        ReportSection tableSection = new ReportSection();
        tableSection.setTitle("Transakcje");
        tableSection.setType("table");
        tableSection.setOrder(3);
        
        ReportTable transactionTable = new ReportTable();
        transactionTable.setId("transactions-table");
        transactionTable.setTitle("Lista transakcji");
        transactionTable.setHeaders(List.of("Data", "Opis", "Kategoria", "Kwota"));
        transactionTable.setColumnTypes(new String[]{"date", "text", "text", "currency"});
        transactionTable.setRows(createSampleTransactionData());
        
        tableSection.setTables(List.of(transactionTable));
        
        report.setSections(List.of(summarySection, chartSection, tableSection));
        
        return report;
    }
    
    private Report generateHabitAnalysisReportData(Report report) {
        report.setTitle("Analiza nawyków");
        report.setDescription("Analiza realizacji nawyków za okres " + 
                            report.getStartDate() + " - " + report.getEndDate());
        
        Map<String, Object> data = new HashMap<>();
        List<Habit> habits = habitService.getAllHabits();
        
        // Statystyki ogólne
        data.put("totalHabits", habits.size());
        data.put("activeHabits", habits.stream().filter(Habit::isActive).count());
        data.put("averageCompletionRate", habits.stream()
            .mapToDouble(Habit::getCompletionRate)
            .average()
            .orElse(0.0));
        
        // Sekcja 1: Podsumowanie
        ReportSection summarySection = new ReportSection();
        summarySection.setTitle("Podsumowanie nawyków");
        summarySection.setType("summary");
        summarySection.setOrder(1);
        summarySection.setData(data);
        
        // Sekcja 2: Wykres realizacji według kategorii
        ReportSection categoryChartSection = new ReportSection();
        categoryChartSection.setTitle("Realizacja według kategorii");
        categoryChartSection.setType("chart");
        categoryChartSection.setOrder(2);
        
        ReportChart categoryChart = new ReportChart();
        categoryChart.setId("category-completion-chart");
        categoryChart.setTitle("Procent realizacji nawyków według kategorii");
        categoryChart.setType("bar");
        categoryChart.setXAxisLabel("Kategoria");
        categoryChart.setYAxisLabel("Realizacja %");
        categoryChart.setData(createCategoryCompletionData());
        
        categoryChartSection.setCharts(List.of(categoryChart));
        
        // Sekcja 3: Tabela nawyków
        ReportSection habitsTableSection = new ReportSection();
        habitsTableSection.setTitle("Szczegóły nawyków");
        habitsTableSection.setType("table");
        habitsTableSection.setOrder(3);
        
        ReportTable habitsTable = new ReportTable();
        habitsTable.setId("habits-table");
        habitsTable.setTitle("Lista nawyków");
        habitsTable.setHeaders(List.of("Nazwa", "Kategoria", "Częstotliwość", "Realizacja %", "Seria"));
        habitsTable.setColumnTypes(new String[]{"text", "text", "text", "percentage", "number"});
        habitsTable.setRows(createHabitsTableData(habits));
        
        habitsTableSection.setTables(List.of(habitsTable));
        
        report.setSections(List.of(summarySection, categoryChartSection, habitsTableSection));
        
        return report;
    }
    
    private Report generateGoalProgressReportData(Report report) {
        report.setTitle("Postęp celów");
        report.setDescription("Raport postępu realizacji celów");
        
        List<Goal> goals = goalService.getAllGoals();
        
        // Sekcja 1: Podsumowanie celów
        ReportSection summarySection = new ReportSection();
        summarySection.setTitle("Podsumowanie celów");
        summarySection.setType("summary");
        summarySection.setOrder(1);
        
        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("totalGoals", goals.size());
        summaryData.put("activeGoals", goals.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count());
        summaryData.put("completedGoals", goals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count());
        summaryData.put("totalTargetAmount", goals.stream().map(Goal::getTargetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        summaryData.put("totalCurrentAmount", goals.stream().map(Goal::getCurrentAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        summarySection.setData(summaryData);
        
        // Sekcja 2: Wykres kołowy statusów
        ReportSection statusChartSection = new ReportSection();
        statusChartSection.setTitle("Status celów");
        statusChartSection.setType("chart");
        statusChartSection.setOrder(2);
        
        ReportChart statusChart = new ReportChart();
        statusChart.setId("goal-status-chart");
        statusChart.setTitle("Rozkład celów według statusu");
        statusChart.setType("pie");
        statusChart.setData(createGoalStatusData(goals));
        
        statusChartSection.setCharts(List.of(statusChart));
        
        // Sekcja 3: Tabela celów
        ReportSection goalsTableSection = new ReportSection();
        goalsTableSection.setTitle("Szczegóły celów");
        goalsTableSection.setType("table");
        goalsTableSection.setOrder(3);
        
        ReportTable goalsTable = new ReportTable();
        goalsTable.setId("goals-table");
        goalsTable.setTitle("Lista celów");
        goalsTable.setHeaders(List.of("Nazwa", "Kategoria", "Status", "Cel", "Aktualnie", "Postęp %"));
        goalsTable.setColumnTypes(new String[]{"text", "text", "text", "currency", "currency", "percentage"});
        goalsTable.setRows(createGoalsTableData(goals));
        
        goalsTableSection.setTables(List.of(goalsTable));
        
        report.setSections(List.of(summarySection, statusChartSection, goalsTableSection));
        
        return report;
    }
    
    private Report generateTaskProductivityReportData(Report report) {
        report.setTitle("Produktywność zadań");
        report.setDescription("Analiza produktywności na podstawie zadań");
        
        List<Task> tasks = taskService.getAllTasks();
        
        // Sekcja 1: Podsumowanie
        ReportSection summarySection = new ReportSection();
        summarySection.setTitle("Podsumowanie zadań");
        summarySection.setType("summary");
        summarySection.setOrder(1);
        
        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("totalTasks", tasks.size());
        summaryData.put("completedTasks", tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
        summaryData.put("pendingTasks", tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count());
        summaryData.put("overdueTasks", tasks.stream().filter(Task::isOverdue).count());
        summaryData.put("completionRate", tasks.isEmpty() ? 0 : 
            (double) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count() / tasks.size() * 100);
        summarySection.setData(summaryData);
        
        // Sekcja 2: Wykres priorytetów
        ReportSection priorityChartSection = new ReportSection();
        priorityChartSection.setTitle("Zadania według priorytetu");
        priorityChartSection.setType("chart");
        priorityChartSection.setOrder(2);
        
        ReportChart priorityChart = new ReportChart();
        priorityChart.setId("priority-chart");
        priorityChart.setTitle("Rozkład zadań według priorytetu");
        priorityChart.setType("doughnut");
        priorityChart.setData(createTaskPriorityData(tasks));
        
        priorityChartSection.setCharts(List.of(priorityChart));
        
        report.setSections(List.of(summarySection, priorityChartSection));
        
        return report;
    }
    
    private Report generateDashboardReportData(Report report) {
        report.setTitle("Przegląd kokpitu");
        report.setDescription("Kompletny przegląd wszystkich obszarów życia");
        
        Dashboard dashboard = dashboardService.getDashboard();
        
        ReportSection section = new ReportSection();
        section.setTitle("Podsumowanie ogólne");
        section.setType("dashboard");
        section.setOrder(1);
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("financial", Map.of(
            "totalBalance", dashboard.getTotalBalance(),
            "monthlyIncome", dashboard.getMonthlyIncome(),
            "monthlyExpenses", dashboard.getMonthlyExpenses(),
            "savingsThisMonth", dashboard.getSavingsThisMonth()
        ));
        
        dashboardData.put("goals", Map.of(
            "totalGoals", dashboard.getTotalGoals(),
            "completedGoals", dashboard.getCompletedGoals(),
            "activeGoals", dashboard.getActiveGoals(),
            "completionRate", dashboard.getGoalCompletionPercentage()
        ));
        
        dashboardData.put("tasks", Map.of(
            "totalTasks", dashboard.getTotalTasks(),
            "completedToday", dashboard.getCompletedTasksToday(),
            "pendingTasks", dashboard.getPendingTasks(),
            "overdueTasks", dashboard.getOverdueTasks()
        ));
        
        dashboardData.put("habits", Map.of(
            "totalHabits", dashboard.getTotalHabits(),
            "completedToday", dashboard.getHabitsCompletedToday(),
            "averageCompletionRate", dashboard.getAverageHabitCompletionRate(),
            "longestStreak", dashboard.getCurrentLongestStreak()
        ));
        
        section.setData(dashboardData);
        report.setSections(List.of(section));
        
        return report;
    }
    
    private Report generateWeeklySummaryReportData(Report report) {
        report.setTitle("Podsumowanie tygodniowe");
        report.setDescription("Tygodniowy raport aktywności");
        
        Map<String, Object> weeklyData = dashboardService.getWeeklyProgress();
        
        ReportSection section = new ReportSection();
        section.setTitle("Postęp tygodniowy");
        section.setType("weekly");
        section.setOrder(1);
        section.setData(weeklyData);
        
        report.setSections(List.of(section));
        
        return report;
    }
    
    private Report generateMonthlySummaryReportData(Report report) {
        report.setTitle("Podsumowanie miesięczne");
        report.setDescription("Miesięczny raport podsumowujący");
        
        // Użyj danych z dashboard jako podstawy
        Dashboard dashboard = dashboardService.getDashboard();
        
        ReportSection section = new ReportSection();
        section.setTitle("Podsumowanie miesiąca");
        section.setType("monthly");
        section.setOrder(1);
        
        Map<String, Object> monthlyData = new HashMap<>();
        monthlyData.put("dashboard", dashboard);
        monthlyData.put("insights", List.of(
            "Świetna realizacja nawyków - utrzymuj tempo!",
            "Cel 'Wakacje w Hiszpanii' wymaga zwiększonej oszczędności",
            "3 zadania zaległe - uporaj się z nimi jak najszybciej"
        ));
        
        section.setData(monthlyData);
        report.setSections(List.of(section));
        
        return report;
    }
    
    private Report generateCustomReportData(Report report) {
        if (report.getData() == null) {
            report.setData(new HashMap<>());
        }
        
        ReportSection section = new ReportSection();
        section.setTitle("Raport niestandardowy");
        section.setType("custom");
        section.setOrder(1);
        section.setData(report.getData());
        
        report.setSections(List.of(section));
        return report;
    }
    
    // Metody pomocnicze do tworzenia danych
    private List<Map<String, Object>> createSamplePieChartData() {
        return List.of(
            Map.of("label", "Jedzenie", "value", 1200),
            Map.of("label", "Transport", "value", 800),
            Map.of("label", "Rozrywka", "value", 600),
            Map.of("label", "Zdrowie", "value", 400),
            Map.of("label", "Inne", "value", 800)
        );
    }
    
    private List<Map<String, Object>> createSampleTransactionData() {
        return List.of(
            Map.of("date", "2026-01-08", "description", "Zakupy spożywcze", "category", "Jedzenie", "amount", -150.50),
            Map.of("date", "2026-01-07", "description", "Wypłata", "category", "Praca", "amount", 3500.00),
            Map.of("date", "2026-01-06", "description", "Paliwo", "category", "Transport", "amount", -200.00),
            Map.of("date", "2026-01-05", "description", "Kino", "category", "Rozrywka", "amount", -45.00)
        );
    }
    
    private List<Map<String, Object>> createCategoryCompletionData() {
        return List.of(
            Map.of("label", "Zdrowie", "value", 85),
            Map.of("label", "Nauka", "value", 92),
            Map.of("label", "Produktywność", "value", 78),
            Map.of("label", "Finanse", "value", 95)
        );
    }
    
    private List<Map<String, Object>> createHabitsTableData(List<Habit> habits) {
        return habits.stream().map(habit -> Map.of(
            "Nazwa", (Object) habit.getName(),
            "Kategoria", habit.getCategory().getDisplayName(),
            "Częstotliwość", habit.getFrequency().getDisplayName(),
            "Realizacja %", Math.round(habit.getCompletionRate()),
            "Seria", habit.getCurrentStreak()
        )).collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> createGoalStatusData(List<Goal> goals) {
        Map<GoalStatus, Long> statusCount = goals.stream()
            .collect(Collectors.groupingBy(Goal::getStatus, Collectors.counting()));
        
        return statusCount.entrySet().stream()
            .map(entry -> Map.of("label", (Object) entry.getKey().getDisplayName(), "value", entry.getValue()))
            .collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> createGoalsTableData(List<Goal> goals) {
        return goals.stream().map(goal -> Map.of(
            "Nazwa", (Object) goal.getName(),
            "Kategoria", goal.getCategory().getDisplayName(),
            "Status", goal.getStatus().getDisplayName(),
            "Cel", goal.getTargetAmount(),
            "Aktualnie", goal.getCurrentAmount(),
            "Postęp %", Math.round(goal.getProgressPercentage())
        )).collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> createTaskPriorityData(List<Task> tasks) {
        Map<Priority, Long> priorityCount = tasks.stream()
            .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
        
        return priorityCount.entrySet().stream()
            .map(entry -> Map.of("label", (Object) entry.getKey().getDisplayName(), "value", entry.getValue()))
            .collect(Collectors.toList());
    }
    
    private LocalDate[] calculatePeriodDates(ReportPeriod period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (period) {
            case TODAY:
                startDate = endDate;
                break;
            case THIS_WEEK:
                startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
                break;
            case THIS_MONTH:
                startDate = endDate.withDayOfMonth(1);
                break;
            case THIS_QUARTER:
                int month = endDate.getMonthValue();
                int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                startDate = endDate.withMonth(quarterStartMonth).withDayOfMonth(1);
                break;
            case THIS_YEAR:
                startDate = endDate.withDayOfYear(1);
                break;
            case LAST_7_DAYS:
                startDate = endDate.minusDays(7);
                break;
            case LAST_30_DAYS:
                startDate = endDate.minusDays(30);
                break;
            case LAST_90_DAYS:
                startDate = endDate.minusDays(90);
                break;
            default:
                startDate = endDate.minusDays(30);
        }
        
        return new LocalDate[]{startDate, endDate};
    }
    
    // Implementacje podstawowych metod CRUD
    @Override
    public Report saveReport(Report report) {
        report.setId(reportIdCounter++);
        reports.put(report.getId(), report);
        return report;
    }
    
    @Override
    public Report getReportById(Long id) {
        return reports.get(id);
    }
    
    @Override
    public List<Report> getAllReports() {
        return new ArrayList<>(reports.values());
    }
    
    @Override
    public List<Report> getReportsByType(ReportType type) {
        return reports.values().stream()
            .filter(report -> report.getType() == type)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteReport(Long id) {
        reports.remove(id);
    }
    
    @Override
    public Report generateFinancialReport(ReportPeriod period) {
        return generateReport(ReportType.FINANCIAL_SUMMARY, period);
    }
    
    @Override
    public Report generateHabitReport(ReportPeriod period, Long habitId) {
        return generateReport(ReportType.HABIT_ANALYSIS, period);
    }
    
    @Override
    public Report generateGoalReport(Long goalId) {
        return generateReport(ReportType.GOAL_PROGRESS, ReportPeriod.ALL_TIME);
    }
    
    @Override
    public Report generateTaskProductivityReport(ReportPeriod period) {
        return generateReport(ReportType.TASK_PRODUCTIVITY, period);
    }
    
    @Override
    public Report generateDashboardReport() {
        return generateReport(ReportType.DASHBOARD_OVERVIEW, ReportPeriod.THIS_MONTH);
    }
    
    // Metody eksportu - uproszczone implementacje
    @Override
    public String exportToPDF(Report report) {
        return "Raport zapisany jako PDF: reports/report_" + report.getId() + ".pdf";
    }
    
    @Override
    public String exportToCSV(Report report) {
        return "Raport zapisany jako CSV: reports/report_" + report.getId() + ".csv";
    }
    
    @Override
    public String exportToExcel(Report report) {
        return "Raport zapisany jako Excel: reports/report_" + report.getId() + ".xlsx";
    }
    
    @Override
    public String exportToHTML(Report report) {
        return "Raport zapisany jako HTML: reports/report_" + report.getId() + ".html";
    }
    
    // Metody harmonogramowania - uproszczone
    @Override
    public void scheduleReport(Report report, String cronExpression) {
        report.setScheduled(true);
        report.setScheduleExpression(cronExpression);
    }
    
    @Override
    public void cancelScheduledReport(Long reportId) {
        Report report = reports.get(reportId);
        if (report != null) {
            report.setScheduled(false);
            report.setScheduleExpression(null);
        }
    }
    
    @Override
    public List<Report> getScheduledReports() {
        return reports.values().stream()
            .filter(Report::isScheduled)
            .collect(Collectors.toList());
    }
    
    // Metody szablonów
    @Override
    public Report createTemplate(ReportType type, String name, Map<String, Object> configuration) {
        Report template = new Report();
        template.setId(templateIdCounter++);
        template.setTitle(name);
        template.setType(type);
        template.setData(configuration);
        template.setGeneratedAt(LocalDateTime.now());
        
        templates.put(template.getId(), template);
        return template;
    }
    
    @Override
    public List<Report> getTemplates() {
        return new ArrayList<>(templates.values());
    }
    
    @Override
    public Report generateFromTemplate(Long templateId, ReportPeriod period) {
        Report template = templates.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Nie znaleziono szablonu o ID: " + templateId);
        }
        
        Report report = generateReport(template.getType(), period);
        report.setTitle(template.getTitle());
        report.setDescription("Wygenerowano z szablonu");
        
        // Zastosuj konfigurację z szablonu
        if (template.getData() != null) {
            report.getData().putAll(template.getData());
        }
        
        return report;
    }
}