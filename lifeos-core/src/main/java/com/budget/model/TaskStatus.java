package com.budget.model;

public enum TaskStatus {
    PENDING("Oczekujące"),
    IN_PROGRESS("W trakcie"),
    COMPLETED("Zakończone"),
    CANCELLED("Anulowane"),
    ON_HOLD("Wstrzymane");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}