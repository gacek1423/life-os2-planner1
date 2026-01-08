package com.budget.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Habit {
    private Long id;
    private String name;
    private String description;
    private HabitCategory category;
    private HabitFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private int targetStreak;
    @Builder.Default
    private List<HabitRecord> records = new ArrayList<>();

    
    public int getCurrentStreak() {
        if (records == null || records.isEmpty()) return 0;
        
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        
        for (int i = records.size() - 1; i >= 0; i--) {
            HabitRecord record = records.get(i);
            if (record.isCompleted() && record.getDate().equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    public double getCompletionRate() {
        if (records == null || records.isEmpty()) return 0.0;
        
        long completed = records.stream()
            .filter(HabitRecord::isCompleted)
            .count();
        
        return (double) completed / records.size() * 100;
    }
    
    public void addRecord(HabitRecord record) {
        if (records == null) {
            records = new ArrayList<>();
        }
        records.add(record);
    }
}