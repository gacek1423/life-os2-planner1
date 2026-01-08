package com.budget.modules.habits;

import com.budget.model.Habit;
import com.budget.model.HabitRecord;
import com.budget.model.HabitCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HabitService {
    
    // CRUD operacje
    Habit createHabit(Habit habit);
    Habit getHabitById(Long id);
    List<Habit> getAllHabits();
    List<Habit> getHabitsByCategory(HabitCategory category);
    List<Habit> getActiveHabits();
    Habit updateHabit(Habit habit);
    void deleteHabit(Long id);
    
    // Rekordy nawyków
    HabitRecord addRecord(HabitRecord record);
    List<HabitRecord> getRecordsForHabit(Long habitId);
    List<HabitRecord> getRecordsForDate(LocalDate date);
    HabitRecord getRecordForHabitAndDate(Long habitId, LocalDate date);
    
    // Statystyki
    Map<HabitCategory, Double> getCompletionRateByCategory();
    int getCurrentStreak(Long habitId);
    int getBestStreak(Long habitId);
    Map<LocalDate, Boolean> getCompletionCalendar(Long habitId, LocalDate startDate, LocalDate endDate);
    
    // Operacje dziennie
    List<Habit> getHabitsForToday();
    void completeHabitForToday(Long habitId, String notes, int difficulty);
    void uncompleteHabitForToday(Long habitId);
    
    // Analiza
    Map<String, Object> getHabitAnalytics(Long habitId);
    Map<String, Object> getGlobalHabitStats();
}