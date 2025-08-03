package com.u1mobis.dashboard_backend.domain.equipment.model;

public enum WorkStationType {
    ASSEMBLY("Assembly Station", "Assembles components"),
    INSPECTION("Inspection Station", "Quality inspection"),
    PACKAGING("Packaging Station", "Product packaging"),  
    TESTING("Testing Station", "Product testing"),
    QUALITY_CHECK("Quality Check Station", "Quality verification"),
    PREPARATION("Preparation Station", "Material preparation"),
    FINISHING("Finishing Station", "Final finishing operations");
    
    private final String displayName;
    private final String description;
    
    WorkStationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isQualityRelated() {
        return this == INSPECTION || this == QUALITY_CHECK || this == TESTING;
    }
    
    public boolean isProductionRelated() {
        return this == ASSEMBLY || this == PACKAGING || this == PREPARATION || this == FINISHING;
    }
}