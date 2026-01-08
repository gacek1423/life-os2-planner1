package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    private String title;
    private String description;
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    private Priority priority;
    private LocalDate dueDate;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;
    private String category;
    private String tags;
    private int estimatedMinutes;
    private int actualMinutes;
    private Long userId;
    private boolean recurring;
    private RecurringPattern recurringPattern;
    private LocalDate recurringEndDate;
    private String notes;

    
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void start() {
        this.status = TaskStatus.IN_PROGRESS;
    }
    
    public boolean isOverdue() {
        if (dueDate == null || status == TaskStatus.COMPLETED) return false;
        return LocalDate.now().isAfter(dueDate);
    }
    
    public long getDaysUntilDue() {
        if (dueDate == null) return -1;
        return LocalDate.now().until(dueDate).getDays();
    }
    
    public double getTimeEfficiency() {
        if (estimatedMinutes == 0 || actualMinutes == 0) return 0.0;
        return (double) estimatedMinutes / actualMinutes * 100;
    }
}