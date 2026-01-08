package com.budget.model;

public enum GoalStatus {
    ACTIVE("Aktywny"),
    PAUSED("Wstrzymany"),
    COMPLETED("Zakończony"),
    CANCELLED("Anulowany");
    
    private final String displayName;
    
    GoalStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}