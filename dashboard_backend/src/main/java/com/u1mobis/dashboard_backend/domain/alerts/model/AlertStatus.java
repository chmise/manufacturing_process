package com.u1mobis.dashboard_backend.domain.alerts.model;

public enum AlertStatus {
    ACTIVE("Active", "Alert is active and requires attention", "#FF4444"),
    ACKNOWLEDGED("Acknowledged", "Alert has been acknowledged by a user", "#FFAA00"),
    RESOLVED("Resolved", "Alert has been resolved", "#00AA00"),
    SUPPRESSED("Suppressed", "Alert has been suppressed", "#AAAAAA"),
    EXPIRED("Expired", "Alert has expired without resolution", "#666666");
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    AlertStatus(String displayName, String description, String colorCode) {
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
    
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    public boolean isResolved() {
        return this == RESOLVED;
    }
    
    public boolean canBeAcknowledged() {
        return this == ACTIVE;
    }
    
    public boolean canBeResolved() {
        return this == ACTIVE || this == ACKNOWLEDGED;
    }
    
    public boolean requiresAttention() {
        return this == ACTIVE;
    }
}