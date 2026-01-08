package com.budget.model;

public enum ReportPeriod {
    TODAY("Dziś"),
    THIS_WEEK("Ten tydzień"),
    THIS_MONTH("Ten miesiąc"),
    THIS_QUARTER("Ten kwartał"),
    THIS_YEAR("Ten rok"),
    LAST_7_DAYS("Ostatnie 7 dni"),
    LAST_30_DAYS("Ostatnie 30 dni"),
    LAST_90_DAYS("Ostatnie 90 dni"),
    CUSTOM("Niestandardowy"),
    ALL_TIME("Cały okres");
    
    private final String displayName;
    
    ReportPeriod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}