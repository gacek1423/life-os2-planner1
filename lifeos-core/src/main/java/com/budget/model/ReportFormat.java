package com.budget.model;

public enum ReportFormat {
    PDF("PDF"),
    CSV("CSV"),
    JSON("JSON"),
    HTML("HTML"),
    EXCEL("Excel");
    
    private final String displayName;
    
    ReportFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}