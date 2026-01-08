package com.budget.model;

public enum HabitFrequency {
    DAILY("Codziennie"),
    WEEKLY("Co tydzień"),
    WEEKDAYS("Dni robocze"),
    WEEKENDS("Weekendy"),
    CUSTOM("Niestandardowe");
    
    private final String displayName;
    
    HabitFrequency(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}