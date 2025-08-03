package com.u1mobis.dashboard_backend.domain.iot.model;

public enum SensorStatus {
    NORMAL("Normal", "Sensor operating normally", "#00CC00"),
    WARNING("Warning", "Sensor value outside normal range", "#FFCC00"),
    CRITICAL("Critical", "Sensor value in critical range", "#FF6600"),
    ERROR("Error", "Sensor malfunction or communication error", "#FF0000"),
    OFFLINE("Offline", "Sensor not responding", "#CCCCCC"),
    MAINTENANCE("Maintenance", "Sensor under maintenance", "#0066CC"),
    OUT_OF_RANGE("Out of Range", "Sensor value outside acceptable range", "#FF9900");
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    SensorStatus(String displayName, String description, String colorCode) {
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
    
    public boolean isOperational() {
        return this == NORMAL || this == WARNING;
    }
    
    public boolean requiresAction() {
        return this == CRITICAL || this == ERROR || this == OFFLINE || this == OUT_OF_RANGE;
    }
    
    public boolean isHealthy() {
        return this == NORMAL;
    }
    
    public int getSeverityLevel() {
        return switch (this) {
            case NORMAL -> 0;
            case WARNING -> 1;
            case MAINTENANCE -> 2;
            case OUT_OF_RANGE -> 3;
            case OFFLINE -> 4;
            case CRITICAL -> 5;
            case ERROR -> 6;
        };
    }
}