package com.u1mobis.dashboard_backend.domain.equipment.model;

public enum StationWorkMode {
    MANUAL("Manual", "Operated manually by workers"),
    SEMI_AUTOMATIC("Semi-Automatic", "Partially automated with manual intervention"),
    AUTOMATIC("Automatic", "Fully automated operation"),
    MAINTENANCE("Maintenance", "Station is in maintenance mode");
    
    private final String displayName;
    private final String description;
    
    StationWorkMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean requiresOperator() {
        return this == MANUAL || this == SEMI_AUTOMATIC;
    }
    
    public boolean isAutomated() {
        return this == SEMI_AUTOMATIC || this == AUTOMATIC;
    }
    
    public boolean isOperational() {
        return this != MAINTENANCE;
    }
}