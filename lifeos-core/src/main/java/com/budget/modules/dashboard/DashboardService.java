package com.budget.modules.dashboard;

import com.budget.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    
    Dashboard getDashboard();
    
    Dashboard getDashboardForDate(LocalDate date);
    
    // Finanse
    Map<String, Object> getFinancialSummary();
    Map<String, BigDecimal> getMonthlyExpenseByCategory();
    Map<String, BigDecimal> getMonthlyIncomeByCategory();
    
    // Cele
    List<Goal> getActiveGoals();
    List<Goal> getCompletedGoalsThisMonth();
    Map<GoalCategory, Long> getGoalsByCategory();
    
    // Zadania
    List<Task> getTodayTasks();
    List<Task> getOverdueTasks();
    Map<TaskStatus, Long> getTasksByStatus();
    
    // Nawyki
    List<Habit> getTodayHabits();
    Map<HabitCategory, Double> getHabitCompletionByCategory();
    
    // Analiza czasowa
    Map<String, Object> getProductivityAnalysis();
    Map<String, Object> getWeeklyProgress();
    
    // Powiadomienia i przypomnienia
    List<String> getNotifications();
    List<String> getReminders();
}