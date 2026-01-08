package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private LocalDate startDate;
    private LocalDate targetDate;
    private LocalDate completedDate;
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;
    private GoalCategory category;
    private Priority priority;
    @Builder.Default
    private List<GoalMilestone> milestones = new ArrayList<>();
    @Builder.Default
    private List<GoalProgress> progressHistory = new ArrayList<>();

    
    public void addProgress(BigDecimal amount) {
        if (this.currentAmount == null) {
            this.currentAmount = BigDecimal.ZERO;
        }
        this.currentAmount = this.currentAmount.add(amount);
        
        GoalProgress progress = new GoalProgress();
        progress.setAmount(amount);
        progress.setDate(LocalDate.now());
        progress.setTotalAmount(this.currentAmount);
        
        if (progressHistory == null) {
            progressHistory = new ArrayList<>();
        }
        progressHistory.add(progress);
        
        checkMilestones();
        checkCompletion();
    }
    
    private void checkMilestones() {
        if (milestones == null) return;
        
        for (GoalMilestone milestone : milestones) {
            if (!milestone.isAchieved() && currentAmount.compareTo(milestone.getAmount()) >= 0) {
                milestone.setAchieved(true);
                milestone.setAchievedDate(LocalDate.now());
            }
        }
    }
    
    private void checkCompletion() {
        if (currentAmount.compareTo(targetAmount) >= 0) {
            status = GoalStatus.COMPLETED;
            completedDate = LocalDate.now();
        }
    }
    
    public double getProgressPercentage() {
        if (targetAmount == null || currentAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return currentAmount.doubleValue() / targetAmount.doubleValue() * 100;
    }
    
    public long getDaysRemaining() {
        if (targetDate == null) return 0;
        return LocalDate.now().until(targetDate).getDays();
    }
    
    public BigDecimal getRemainingAmount() {
        if (targetAmount == null || currentAmount == null) return BigDecimal.ZERO;
        BigDecimal remaining = targetAmount.subtract(currentAmount);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }
}