package com.budget.model;

public enum ReportType {
    FINANCIAL_SUMMARY("Podsumowanie finansowe"),
    HABIT_ANALYSIS("Analiza nawyków"),
    GOAL_PROGRESS("Postęp celów"),
    TASK_PRODUCTIVITY("Produktywność zadań"),
    DASHBOARD_OVERVIEW("Przegląd kokpitu"),
    WEEKLY_SUMMARY("Podsumowanie tygodniowe"),
    MONTHLY_SUMMARY("Podsumowanie miesięczne"),
    YEARLY_SUMMARY("Podsumowanie roczne"),
    CUSTOM("Raport niestandardowy");
    
    private final String displayName;
    
    ReportType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}