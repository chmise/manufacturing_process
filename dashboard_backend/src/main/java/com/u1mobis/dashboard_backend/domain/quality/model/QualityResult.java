package com.u1mobis.dashboard_backend.domain.quality.model;

public enum QualityResult {
    PASS("Pass", "Meets quality standards", true),
    FAIL("Fail", "Does not meet quality standards", false),
    CONDITIONAL_PASS("Conditional Pass", "Passes with conditions or minor issues", true),
    REWORK_REQUIRED("Rework Required", "Requires rework to meet standards", false),
    REJECTED("Rejected", "Completely rejected, cannot be fixed", false),
    PENDING("Pending", "Inspection in progress", false);
    
    private final String displayName;
    private final String description;
    private final boolean acceptable;
    
    QualityResult(String displayName, String description, boolean acceptable) {
        this.displayName = displayName;
        this.description = description;
        this.acceptable = acceptable;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAcceptable() {
        return acceptable;
    }
    
    public boolean isRejected() {
        return this == FAIL || this == REJECTED || this == REWORK_REQUIRED;
    }
    
    public boolean isPending() {
        return this == PENDING;
    }
    
    public boolean requiresAction() {
        return this == REWORK_REQUIRED || this == CONDITIONAL_PASS;
    }
}