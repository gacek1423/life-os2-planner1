package com.budget.modules.habits;

import com.budget.model.Habit;
import com.budget.model.HabitRecord;
import com.budget.model.HabitCategory;
import com.budget.model.HabitFrequency;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HabitServiceImpl implements HabitService {
    
    private final Map<Long, Habit> habits = new HashMap<>();
    private final Map<Long, HabitRecord> records = new HashMap<>();
    private long habitIdCounter = 1;
    private long recordIdCounter = 1;
    
    @Override
    public Habit createHabit(Habit habit) {
        habit.setId(habitIdCounter++);
        habit.setRecords(new ArrayList<>());
        habits.put(habit.getId(), habit);
        return habit;
    }
    
    @Override
    public Habit getHabitById(Long id) {
        return habits.get(id);
    }
    
    @Override
    public List<Habit> getAllHabits() {
        return new ArrayList<>(habits.values());
    }
    
    @Override
    public List<Habit> getHabitsByCategory(HabitCategory category) {
        return habits.values().stream()
            .filter(habit -> habit.getCategory() == category)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Habit> getActiveHabits() {
        return habits.values().stream()
            .filter(Habit::isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public Habit updateHabit(Habit habit) {
        if (habits.containsKey(habit.getId())) {
            habits.put(habit.getId(), habit);
            return habit;
        }
        return null;
    }
    
    @Override
    public void deleteHabit(Long id) {
        habits.remove(id);
        // Usuń również powiązane rekordy
        records.entrySet().removeIf(entry -> entry.getValue().getHabitId().equals(id));
    }
    
    @Override
    public HabitRecord addRecord(HabitRecord record) {
        record.setId(recordIdCounter++);
        records.put(record.getId(), record);
        
        // Dodaj rekord do listy w nawyku
        Habit habit = habits.get(record.getHabitId());
        if (habit != null) {
            habit.addRecord(record);
        }
        
        return record;
    }
    
    @Override
    public List<HabitRecord> getRecordsForHabit(Long habitId) {
        return records.values().stream()
            .filter(record -> record.getHabitId().equals(habitId))
            .sorted(Comparator.comparing(HabitRecord::getDate))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<HabitRecord> getRecordsForDate(LocalDate date) {
        return records.values().stream()
            .filter(record -> record.getDate().equals(date))
            .collect(Collectors.toList());
    }
    
    @Override
    public HabitRecord getRecordForHabitAndDate(Long habitId, LocalDate date) {
        return records.values().stream()
            .filter(record -> record.getHabitId().equals(habitId) && record.getDate().equals(date))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public Map<HabitCategory, Double> getCompletionRateByCategory() {
        Map<HabitCategory, List<Habit>> habitsByCategory = getAllHabits().stream()
            .collect(Collectors.groupingBy(Habit::getCategory));
        
        Map<HabitCategory, Double> completionRates = new HashMap<>();
        
        for (Map.Entry<HabitCategory, List<Habit>> entry : habitsByCategory.entrySet()) {
            List<Habit> categoryHabits = entry.getValue();
            if (categoryHabits.isEmpty()) {
                completionRates.put(entry.getKey(), 0.0);
                continue;
            }
            
            double averageRate = categoryHabits.stream()
                .mapToDouble(Habit::getCompletionRate)
                .average()
                .orElse(0.0);
            
            completionRates.put(entry.getKey(), averageRate);
        }
        
        return completionRates;
    }
    
    @Override
    public int getCurrentStreak(Long habitId) {
        Habit habit = getHabitById(habitId);
        return habit != null ? habit.getCurrentStreak() : 0;
    }
    
    @Override
    public int getBestStreak(Long habitId) {
        List<HabitRecord> habitRecords = getRecordsForHabit(habitId);
        if (habitRecords.isEmpty()) return 0;
        
        int bestStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;
        
        for (HabitRecord record : habitRecords) {
            if (record.isCompleted()) {
                if (lastDate != null && lastDate.plusDays(1).equals(record.getDate())) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
                bestStreak = Math.max(bestStreak, currentStreak);
                lastDate = record.getDate();
            } else {
                currentStreak = 0;
            }
        }
        
        return bestStreak;
    }
    
    @Override
    public Map<LocalDate, Boolean> getCompletionCalendar(Long habitId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Boolean> calendar = new HashMap<>();
        List<HabitRecord> habitRecords = getRecordsForHabit(habitId);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            boolean completed = habitRecords.stream()
                .anyMatch(record -> record.getDate().equals(finalDate) && record.isCompleted());
            calendar.put(date, completed);
        }
        
        return calendar;
    }
    
    @Override
    public List<Habit> getHabitsForToday() {
        LocalDate today = LocalDate.now();
        return getActiveHabits().stream()
            .filter(habit -> shouldHabitBeDoneToday(habit, today))
            .collect(Collectors.toList());
    }
    
    private boolean shouldHabitBeDoneToday(Habit habit, LocalDate date) {
        if (habit.getFrequency() == HabitFrequency.DAILY) {
            return true;
        } else if (habit.getFrequency() == HabitFrequency.WEEKDAYS) {
            return !date.getDayOfWeek().toString().matches("SATURDAY|SUNDAY");
        } else if (habit.getFrequency() == HabitFrequency.WEEKENDS) {
            return date.getDayOfWeek().toString().matches("SATURDAY|SUNDAY");
        } else if (habit.getFrequency() == HabitFrequency.WEEKLY) {
            // Zakładamy, że weekly habits są robione w dniu startu
            long daysSinceStart = habit.getStartDate().until(date).getDays();
            return daysSinceStart % 7 == 0;
        }
        return false;
    }
    
    @Override
    public void completeHabitForToday(Long habitId, String notes, int difficulty) {
        LocalDate today = LocalDate.now();
        HabitRecord existingRecord = getRecordForHabitAndDate(habitId, today);
        
        if (existingRecord != null) {
            existingRecord.setCompleted(true);
            existingRecord.setNotes(notes);
            existingRecord.setDifficulty(difficulty);
        } else {
            HabitRecord newRecord = new HabitRecord();
            newRecord.setHabitId(habitId);
            newRecord.setDate(today);
            newRecord.setCompleted(true);
            newRecord.setNotes(notes);
            newRecord.setDifficulty(difficulty);
            addRecord(newRecord);
        }
    }
    
    @Override
    public void uncompleteHabitForToday(Long habitId) {
        LocalDate today = LocalDate.now();
        HabitRecord existingRecord = getRecordForHabitAndDate(habitId, today);
        if (existingRecord != null) {
            existingRecord.setCompleted(false);
        }
    }
    
    @Override
    public Map<String, Object> getHabitAnalytics(Long habitId) {
        Map<String, Object> analytics = new HashMap<>();
        Habit habit = getHabitById(habitId);
        List<HabitRecord> habitRecords = getRecordsForHabit(habitId);
        
        analytics.put("habit", habit);
        analytics.put("totalRecords", habitRecords.size());
        analytics.put("completionRate", habit.getCompletionRate());
        analytics.put("currentStreak", getCurrentStreak(habitId));
        analytics.put("bestStreak", getBestStreak(habitId));
        analytics.put("averageDifficulty", habitRecords.stream()
            .filter(HabitRecord::isCompleted)
            .mapToInt(HabitRecord::getDifficulty)
            .average()
            .orElse(0.0));
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getGlobalHabitStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Habit> allHabits = getAllHabits();
        
        stats.put("totalHabits", allHabits.size());
        stats.put("activeHabits", getActiveHabits().size());
        stats.put("habitsCompletedToday", getRecordsForDate(LocalDate.now()).stream()
            .filter(HabitRecord::isCompleted)
            .count());
        stats.put("averageCompletionRate", allHabits.stream()
            .mapToDouble(Habit::getCompletionRate)
            .average()
            .orElse(0.0));
        stats.put("completionByCategory", getCompletionRateByCategory());
        
        return stats;
    }
}