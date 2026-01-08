package com.budget.modules.dashboard;

import com.budget.model.*;
import com.budget.model.GoalCategory;
import com.budget.modules.finance.domain.Purse;
import com.budget.modules.habits.HabitService;
import com.budget.modules.finance.domain.PurseService;
import com.budget.modules.goals.GoalService;
import com.budget.modules.tasks.TaskService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardServiceImpl implements DashboardService {

    private final HabitService habitService;
    private final PurseService purseService;
    private final GoalService goalService;
    private final TaskService taskService;

    public DashboardServiceImpl(HabitService habitService, PurseService purseService,
                                GoalService goalService, TaskService taskService) {
        this.habitService = habitService;
        this.purseService = purseService;
        this.goalService = goalService;
        this.taskService = taskService;
    }

    @Override
    public Dashboard getDashboard() {
        return getDashboardForDate(LocalDate.now());
    }

    @Override
    public Dashboard getDashboardForDate(LocalDate date) {
        Dashboard dashboard = new Dashboard();
        dashboard.setDate(date);

        populateFinancialData(dashboard);
        populateGoalsData(dashboard);
        populateTasksData(dashboard);
        populateHabitsData(dashboard);
        populateDailyData(dashboard);

        return dashboard;
    }

    private void populateFinancialData(Dashboard dashboard) {
        try {
            List<Purse> purses = purseService.getAllPurses();
            // Finanse używają double (zgodnie z DB)
            double totalBalance = purses.stream()
                    .mapToDouble(Purse::getAllocatedAmount)
                    .sum();
            dashboard.setTotalBalance(BigDecimal.valueOf(totalBalance));

            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            List<Transaction> thisMonthTransactions = getTransactionsForPeriod(startOfMonth, endOfMonth);

            double income = thisMonthTransactions.stream()
                    .filter(t -> t.getAmount() > 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double expenses = thisMonthTransactions.stream()
                    .filter(t -> t.getAmount() < 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            expenses = Math.abs(expenses);

            dashboard.setMonthlyIncome(BigDecimal.valueOf(income));
            dashboard.setMonthlyExpenses(BigDecimal.valueOf(expenses));
            dashboard.setSavingsThisMonth(BigDecimal.valueOf(income - expenses));

        } catch (Exception e) {
            dashboard.setTotalBalance(BigDecimal.ZERO);
            dashboard.setMonthlyIncome(BigDecimal.ZERO);
            dashboard.setMonthlyExpenses(BigDecimal.ZERO);
            dashboard.setSavingsThisMonth(BigDecimal.ZERO);
        }
    }

    private void populateGoalsData(Dashboard dashboard) {
        try {
            List<Goal> allGoals = goalService.getAllGoals();
            dashboard.setTotalGoals(allGoals.size());
            dashboard.setCompletedGoals((int) allGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                    .count());
            dashboard.setActiveGoals((int) allGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                    .count());

            // NAPRAWA: Konwersja BigDecimal (z Goal) na double do sumowania
            double totalGoalAmount = allGoals.stream()
                    .map(Goal::getTargetAmount)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue) // <-- KLUCZOWA ZMIANA
                    .sum();

            double achievedGoalAmount = allGoals.stream()
                    .map(Goal::getCurrentAmount)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue) // <-- KLUCZOWA ZMIANA
                    .sum();

            dashboard.setTotalGoalAmount(BigDecimal.valueOf(totalGoalAmount));
            dashboard.setAchievedGoalAmount(BigDecimal.valueOf(achievedGoalAmount));

        } catch (Exception e) {
            dashboard.setTotalGoals(0);
            dashboard.setCompletedGoals(0);
            dashboard.setActiveGoals(0);
            dashboard.setTotalGoalAmount(BigDecimal.ZERO);
            dashboard.setAchievedGoalAmount(BigDecimal.ZERO);
        }
    }

    private void populateTasksData(Dashboard dashboard) {
        try {
            List<Task> allTasks = taskService.getAllTasks();
            dashboard.setTotalTasks(allTasks.size());

            LocalDate today = LocalDate.now();
            dashboard.setCompletedTasksToday((int) allTasks.stream()
                    .filter(t -> t.getCompletedAt() != null &&
                            t.getCompletedAt().toLocalDate().equals(today))
                    .count());

            dashboard.setPendingTasks((int) allTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.IN_PROGRESS)
                    .count());

            dashboard.setOverdueTasks((int) allTasks.stream()
                    .filter(Task::isOverdue)
                    .count());

        } catch (Exception e) {
            dashboard.setTotalTasks(0);
            dashboard.setCompletedTasksToday(0);
            dashboard.setPendingTasks(0);
            dashboard.setOverdueTasks(0);
        }
    }

    private void populateHabitsData(Dashboard dashboard) {
        try {
            List<Habit> allHabits = habitService.getAllHabits();
            dashboard.setTotalHabits(allHabits.size());

            LocalDate today = LocalDate.now();
            dashboard.setHabitsCompletedToday((int) habitService.getRecordsForDate(today).stream()
                    .filter(HabitRecord::isCompleted)
                    .count());

            dashboard.setAverageHabitCompletionRate(allHabits.stream()
                    .mapToDouble(Habit::getCompletionRate)
                    .average()
                    .orElse(0.0));

            dashboard.setCurrentLongestStreak(allHabits.stream()
                    .mapToInt(habit -> habitService.getCurrentStreak(habit.getId()))
                    .max()
                    .orElse(0));

        } catch (Exception e) {
            dashboard.setTotalHabits(0);
            dashboard.setHabitsCompletedToday(0);
            dashboard.setAverageHabitCompletionRate(0.0);
            dashboard.setCurrentLongestStreak(0);
        }
    }

    private void populateDailyData(Dashboard dashboard) {
        try {
            dashboard.setTodayTasks(getTodayTasks());
            dashboard.setTodayHabits(getTodayHabits());
        } catch (Exception e) {
            dashboard.setTodayTasks(new ArrayList<>());
            dashboard.setTodayHabits(new ArrayList<>());
        }
    }

    @Override
    public Map<String, Object> getFinancialSummary() {
        Dashboard dashboard = getDashboard();
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalBalance", dashboard.getTotalBalance());
        summary.put("monthlyIncome", dashboard.getMonthlyIncome());
        summary.put("monthlyExpenses", dashboard.getMonthlyExpenses());
        summary.put("savingsThisMonth", dashboard.getSavingsThisMonth());
        summary.put("savingsRate", dashboard.getSavingsRate());

        return summary;
    }

    @Override
    public Map<String, BigDecimal> getMonthlyExpenseByCategory() {
        return new HashMap<>();
    }

    @Override
    public Map<String, BigDecimal> getMonthlyIncomeByCategory() {
        return new HashMap<>();
    }

    @Override
    public List<Goal> getActiveGoals() {
        try {
            return goalService.getAllGoals().stream()
                    .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Goal> getCompletedGoalsThisMonth() {
        try {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            return goalService.getAllGoals().stream()
                    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                    .filter(g -> g.getCompletedDate() != null &&
                            !g.getCompletedDate().isBefore(startOfMonth))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<GoalCategory, Long> getGoalsByCategory() {
        try {
            return goalService.getAllGoals().stream()
                    .collect(Collectors.groupingBy(Goal::getCategory, Collectors.counting()));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public List<Task> getTodayTasks() {
        try {
            LocalDate today = LocalDate.now();
            return taskService.getAllTasks().stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().equals(today))
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Task> getOverdueTasks() {
        try {
            return taskService.getAllTasks().stream()
                    .filter(Task::isOverdue)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<TaskStatus, Long> getTasksByStatus() {
        try {
            return taskService.getAllTasks().stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public List<Habit> getTodayHabits() {
        try {
            return habitService.getHabitsForToday();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<HabitCategory, Double> getHabitCompletionByCategory() {
        try {
            return habitService.getCompletionRateByCategory();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getProductivityAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        Dashboard dashboard = getDashboard();
        analysis.put("tasksCompletedToday", dashboard.getCompletedTasksToday());
        analysis.put("habitsCompletedToday", dashboard.getHabitsCompletedToday());
        analysis.put("taskCompletionRate", dashboard.getTaskCompletionPercentage());
        analysis.put("habitCompletionRate", dashboard.getAverageHabitCompletionRate());
        analysis.put("productivityTrend", "stable");
        return analysis;
    }

    @Override
    public Map<String, Object> getWeeklyProgress() {
        Map<String, Object> weekly = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        List<Task> weekTasks = getTasksForPeriod(weekStart, today);
        weekly.put("tasksThisWeek", weekTasks.size());
        weekly.put("tasksCompletedThisWeek", weekTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count());

        Map<String, Object> habitWeekly = new HashMap<>();
        for (LocalDate date = weekStart; !date.isAfter(today); date = date.plusDays(1)) {
            habitWeekly.put(date.toString(), habitService.getRecordsForDate(date).stream()
                    .filter(HabitRecord::isCompleted)
                    .count());
        }
        weekly.put("habitsByDay", habitWeekly);
        return weekly;
    }

    @Override
    public List<String> getNotifications() {
        List<String> notifications = new ArrayList<>();
        List<Task> overdueTasks = getOverdueTasks();
        if (!overdueTasks.isEmpty()) {
            notifications.add("Masz " + overdueTasks.size() + " zaległych zadań!");
        }
        return notifications;
    }

    @Override
    public List<String> getReminders() {
        List<String> reminders = new ArrayList<>();
        List<Task> todayTasks = getTodayTasks();
        for (Task task : todayTasks) {
            reminders.add("Dzisiaj: " + task.getTitle());
        }
        return reminders;
    }

    // Metody pomocnicze
    private List<Transaction> getTransactionsForPeriod(LocalDate startDate, LocalDate endDate) {
        // Implementacja powinna pobierać z TransactionDAO
        // Na razie placeholder, aby kod się kompilował
        return new ArrayList<>();
    }

    private List<Task> getTasksForPeriod(LocalDate startDate, LocalDate endDate) {
        try {
            return taskService.getAllTasks().stream()
                    .filter(t -> t.getCreatedAt() != null &&
                            !t.getCreatedAt().toLocalDate().isBefore(startDate) &&
                            !t.getCreatedAt().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}