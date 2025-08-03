package com.u1mobis.dashboard_backend.domain.analytics.model;

public enum KPIType {
    PRODUCTION_EFFICIENCY("Production Efficiency", "Overall equipment effectiveness", "%"),
    QUALITY_RATE("Quality Rate", "First-time yield rate", "%"),
    THROUGHPUT("Throughput", "Production output rate", "units/hour"),
    CYCLE_TIME("Cycle Time", "Average cycle time per unit", "minutes"),
    DOWNTIME_RATE("Downtime Rate", "Equipment downtime percentage", "%"),
    DEFECT_RATE("Defect Rate", "Percentage of defective products", "%"),
    ON_TIME_DELIVERY("On-Time Delivery", "Delivery performance rate", "%"),
    INVENTORY_TURNOVER("Inventory Turnover", "Inventory turnover ratio", "ratio"),
    ENERGY_EFFICIENCY("Energy Efficiency", "Energy consumption per unit", "kWh/unit"),
    LABOR_PRODUCTIVITY("Labor Productivity", "Output per labor hour", "units/hour"),
    COST_PER_UNIT("Cost Per Unit", "Manufacturing cost per unit", "currency/unit"),
    CUSTOMER_SATISFACTION("Customer Satisfaction", "Customer satisfaction score", "score");
    
    private final String displayName;
    private final String description;
    private final String unit;
    
    KPIType(String displayName, String description, String unit) {
        this.displayName = displayName;
        this.description = description;
        this.unit = unit;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public boolean isPercentage() {
        return unit.equals("%");
    }
    
    public boolean isRate() {
        return unit.contains("/");
    }
    
    public boolean isProductionRelated() {
        return this == PRODUCTION_EFFICIENCY || this == THROUGHPUT || 
               this == CYCLE_TIME || this == DOWNTIME_RATE;
    }
    
    public boolean isQualityRelated() {
        return this == QUALITY_RATE || this == DEFECT_RATE;
    }
    
    public boolean isFinancialRelated() {
        return this == COST_PER_UNIT || this == INVENTORY_TURNOVER;
    }
}