package com.u1mobis.dashboard_backend.domain.inventory.model;

public enum MaterialType {
    RAW_MATERIAL("Raw Material", "Base materials for production"),
    COMPONENT("Component", "Manufactured components for assembly"),
    CONSUMABLE("Consumable", "Items consumed during production"),
    TOOL("Tool", "Production tools and equipment"),
    CHEMICAL("Chemical", "Chemical substances for processing"),
    PACKAGING("Packaging", "Materials for product packaging"),
    SPARE_PART("Spare Part", "Replacement parts for equipment"),
    FINISHED_GOOD("Finished Good", "Completed products ready for sale");
    
    private final String displayName;
    private final String description;
    
    MaterialType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isProducedInternally() {
        return this == COMPONENT || this == FINISHED_GOOD;
    }
    
    public boolean isPurchased() {
        return this == RAW_MATERIAL || this == CONSUMABLE || 
               this == TOOL || this == CHEMICAL || 
               this == PACKAGING || this == SPARE_PART;
    }
    
    public boolean requiresQualityCheck() {
        return this == RAW_MATERIAL || this == COMPONENT || 
               this == CHEMICAL || this == FINISHED_GOOD;
    }
}