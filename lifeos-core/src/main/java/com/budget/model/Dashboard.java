package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
    // Finanse
    private BigDecimal totalBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal savingsThisMonth;
    
    // Cele
    private int totalGoals;
    private int completedGoals;
    private int activeGoals;
    private BigDecimal totalGoalAmount;
    private BigDecimal achievedGoalAmount;
    
    // Zadania
    private int totalTasks;
    private int completedTasksToday;
    private int pendingTasks;
    private int overdueTasks;
    
    // Nawyki
    private int totalHabits;
    private int habitsCompletedToday;
    private double averageHabitCompletionRate;
    private int currentLongestStreak;
    
    // Statystyki dzienne
    private LocalDate date;
    private Map<String, BigDecimal> categoryExpenses;
    private List<Task> todayTasks;
    private List<Habit> todayHabits;
    
    // Metody pomocnicze
    public double getGoalCompletionPercentage() {
        if (totalGoals == 0) return 0.0;
        return (double) completedGoals / totalGoals * 100;
    }
    
    public double getTaskCompletionPercentage() {
        if (totalTasks == 0) return 0.0;
        return (double) (totalTasks - pendingTasks) / totalTasks * 100;
    }
    
    public BigDecimal getSavingsRate() {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return savingsThisMonth.divide(monthlyIncome, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}