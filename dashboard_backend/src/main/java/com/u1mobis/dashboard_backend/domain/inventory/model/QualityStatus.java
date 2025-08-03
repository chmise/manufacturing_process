package com.u1mobis.dashboard_backend.domain.inventory.model;

public enum QualityStatus {
    PENDING_INSPECTION("Pending Inspection", "Awaiting quality inspection"),
    UNDER_INSPECTION("Under Inspection", "Currently being inspected"),
    APPROVED("Approved", "Passed quality inspection"),
    REJECTED("Rejected", "Failed quality inspection"),
    QUARANTINED("Quarantined", "Isolated due to quality issues"),
    RETURNED("Returned", "Returned to supplier");
    
    private final String displayName;
    private final String description;
    
    QualityStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    public boolean isRejected() {
        return this == REJECTED || this == QUARANTINED || this == RETURNED;
    }
    
    public boolean isPending() {
        return this == PENDING_INSPECTION || this == UNDER_INSPECTION;
    }
    
    public boolean isUsable() {
        return this == APPROVED;
    }
}