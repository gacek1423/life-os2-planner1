package com.budget.model;

public enum Priority {
    LOW("Niski", 1),
    MEDIUM("Średni", 2),
    HIGH("Wysoki", 3),
    CRITICAL("Krytyczny", 4);
    
    private final String displayName;
    private final int level;
    
    Priority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
}