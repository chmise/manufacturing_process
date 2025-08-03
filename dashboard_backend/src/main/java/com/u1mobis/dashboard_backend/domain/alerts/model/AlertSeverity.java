package com.u1mobis.dashboard_backend.domain.alerts.model;

public enum AlertSeverity {
    INFO("Info", "Informational message", "#0066CC", 1),
    WARNING("Warning", "Warning condition", "#FFCC00", 2),
    CRITICAL("Critical", "Critical condition requiring immediate attention", "#FF6600", 3),
    EMERGENCY("Emergency", "Emergency situation", "#FF0000", 4);
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    private final int priority;
    
    AlertSeverity(String displayName, String description, String colorCode, int priority) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
        this.priority = priority;
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
    
    public int getPriority() {
        return priority;
    }
    
    public boolean isHighPriority() {
        return priority >= 3;
    }
    
    public boolean requiresNotification() {
        return priority >= 2;
    }
    
    public boolean requiresEscalation() {
        return priority >= 3;
    }
}