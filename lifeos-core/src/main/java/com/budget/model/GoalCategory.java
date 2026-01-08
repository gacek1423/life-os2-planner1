package com.budget.model;

public enum GoalCategory {
    FINANCIAL("Finansowy"),
    HEALTH("Zdrowotny"),
    EDUCATION("Edukacyjny"),
    CAREER("Zawodowy"),
    PERSONAL("Osobisty"),
    TRAVEL("Podróże"),
    HOBBY("Hobby"),
    FAMILY("Rodzinny");
    
    private final String displayName;
    
    GoalCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}