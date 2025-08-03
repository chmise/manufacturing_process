package com.u1mobis.dashboard_backend.domain.analytics.model;

public enum KPIStatus {
    NOT_MEASURED("Not Measured", "No data available", "#CCCCCC"),
    ON_TARGET("On Target", "Performance is on target", "#00CC00"),
    WITHIN_RANGE("Within Range", "Performance is within acceptable range", "#FFCC00"),
    BELOW_THRESHOLD("Below Threshold", "Performance is below minimum threshold", "#FF6600"),
    ABOVE_THRESHOLD("Above Threshold", "Performance is above maximum threshold", "#FF0000");
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    KPIStatus(String displayName, String description, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public boolean isGood() {
        return this == ON_TARGET || this == WITHIN_RANGE;
    }
    
    public boolean requiresAttention() {
        return this == BELOW_THRESHOLD || this == ABOVE_THRESHOLD;
    }
    
    public boolean hasData() {
        return this != NOT_MEASURED;
    }
    
    public int getSeverityLevel() {
        return switch (this) {
            case ON_TARGET -> 0;
            case WITHIN_RANGE -> 1;
            case NOT_MEASURED -> 2;
            case BELOW_THRESHOLD, ABOVE_THRESHOLD -> 3;
        };
    }
}