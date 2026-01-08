package com.budget.model;

public enum RecurringPattern {
    DAILY("Codziennie"),
    WEEKLY("Co tydzień"),
    BIWEEKLY("Co dwa tygodnie"),
    MONTHLY("Co miesiąc"),
    QUARTERLY("Co kwartał"),
    YEARLY("Co rok");
    
    private final String displayName;
    
    RecurringPattern(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}