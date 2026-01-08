package com.budget.model;

public enum HabitCategory {
    HEALTH("Zdrowie"),
    FITNESS("Fitness"),
    MENTAL_WELLBEING("Zdrowie psychiczne"),
    PRODUCTIVITY("Produktywność"),
    LEARNING("Nauka"),
    SOCIAL("Relacje społeczne"),
    HOBBY("Hobby"),
    FINANCIAL("Finanse osobiste");
    
    private final String displayName;
    
    HabitCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}