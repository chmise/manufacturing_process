package com.u1mobis.dashboard_backend.domain.alerts.model;

public enum AlertType {
    // Production Alerts
    PRODUCTION_DELAY("Production Delay", "Production is behind schedule", AlertSeverity.WARNING),
    QUALITY_ISSUE("Quality Issue", "Quality problems detected", AlertSeverity.CRITICAL),
    EQUIPMENT_FAILURE("Equipment Failure", "Equipment malfunction or failure", AlertSeverity.CRITICAL),
    MAINTENANCE_DUE("Maintenance Due", "Equipment maintenance required", AlertSeverity.INFO),
    
    // Environmental Alerts
    TEMPERATURE_HIGH("High Temperature", "Temperature above threshold", AlertSeverity.WARNING),
    TEMPERATURE_CRITICAL("Critical Temperature", "Temperature in critical range", AlertSeverity.CRITICAL),
    HUMIDITY_HIGH("High Humidity", "Humidity above threshold", AlertSeverity.WARNING),
    AIR_QUALITY_POOR("Poor Air Quality", "Air quality below standards", AlertSeverity.WARNING),
    
    // Resource Alerts
    INVENTORY_LOW("Low Inventory", "Inventory level below minimum", AlertSeverity.WARNING),
    INVENTORY_CRITICAL("Critical Inventory", "Inventory critically low", AlertSeverity.CRITICAL),
    MATERIAL_SHORTAGE("Material Shortage", "Key material shortage detected", AlertSeverity.CRITICAL),
    
    // Performance Alerts
    KPI_BELOW_TARGET("KPI Below Target", "Performance indicator below target", AlertSeverity.WARNING),
    OEE_LOW("Low OEE", "Overall Equipment Effectiveness below threshold", AlertSeverity.WARNING),
    CYCLE_TIME_HIGH("High Cycle Time", "Cycle time above normal range", AlertSeverity.INFO),
    
    // Security Alerts
    UNAUTHORIZED_ACCESS("Unauthorized Access", "Security breach detected", AlertSeverity.CRITICAL),
    SYSTEM_ANOMALY("System Anomaly", "Unusual system behavior detected", AlertSeverity.WARNING),
    
    // Communication Alerts
    SENSOR_OFFLINE("Sensor Offline", "Sensor communication lost", AlertSeverity.WARNING),
    DEVICE_DISCONNECTED("Device Disconnected", "Device connection lost", AlertSeverity.INFO),
    
    // General System Alerts
    SYSTEM_ERROR("System Error", "System error occurred", AlertSeverity.CRITICAL),
    DATA_VALIDATION_ERROR("Data Validation Error", "Invalid data detected", AlertSeverity.WARNING),
    CONFIGURATION_CHANGE("Configuration Change", "System configuration modified", AlertSeverity.INFO);
    
    private final String displayName;
    private final String description;
    private final AlertSeverity defaultSeverity;
    
    AlertType(String displayName, String description, AlertSeverity defaultSeverity) {
        this.displayName = displayName;
        this.description = description;
        this.defaultSeverity = defaultSeverity;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public AlertSeverity getDefaultSeverity() {
        return defaultSeverity;
    }
    
    public boolean isProductionRelated() {
        return this == PRODUCTION_DELAY || this == QUALITY_ISSUE || 
               this == EQUIPMENT_FAILURE || this == MAINTENANCE_DUE;
    }
    
    public boolean isEnvironmentalRelated() {
        return this == TEMPERATURE_HIGH || this == TEMPERATURE_CRITICAL || 
               this == HUMIDITY_HIGH || this == AIR_QUALITY_POOR;
    }
    
    public boolean isResourceRelated() {
        return this == INVENTORY_LOW || this == INVENTORY_CRITICAL || 
               this == MATERIAL_SHORTAGE;
    }
    
    public boolean requiresImmediateAction() {
        return defaultSeverity == AlertSeverity.CRITICAL;
    }
}