package com.u1mobis.dashboard_backend.domain.quality.model;

public enum InspectionType {
    INCOMING_INSPECTION("Incoming Inspection", "Quality check of received materials"),
    IN_PROCESS_INSPECTION("In-Process Inspection", "Quality check during production"),
    FINAL_INSPECTION("Final Inspection", "Quality check of finished products"),
    FIRST_PIECE_INSPECTION("First Piece Inspection", "Initial quality verification"),
    RANDOM_INSPECTION("Random Inspection", "Random sampling quality check"),
    CUSTOMER_COMPLAINT("Customer Complaint", "Quality investigation from customer feedback"),
    AUDIT_INSPECTION("Audit Inspection", "Systematic quality audit"),
    CALIBRATION_CHECK("Calibration Check", "Equipment calibration verification");
    
    private final String displayName;
    private final String description;
    
    InspectionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isPreProduction() {
        return this == INCOMING_INSPECTION || this == FIRST_PIECE_INSPECTION;
    }
    
    public boolean isDuringProduction() {
        return this == IN_PROCESS_INSPECTION || this == RANDOM_INSPECTION;
    }
    
    public boolean isPostProduction() {
        return this == FINAL_INSPECTION;
    }
    
    public boolean isReactive() {
        return this == CUSTOMER_COMPLAINT || this == AUDIT_INSPECTION;
    }
}