package com.u1mobis.dashboard_backend.domain.user.model;

public enum UserStatus {
    ACTIVE("Active", "User account is active and can access the system"),
    INACTIVE("Inactive", "User account is temporarily disabled"),
    SUSPENDED("Suspended", "User account is suspended due to violations"),
    PENDING("Pending", "User account is pending activation"),
    EXPIRED("Expired", "User account has expired and needs renewal"),
    LOCKED("Locked", "User account is locked due to security reasons");
    
    private final String displayName;
    private final String description;
    
    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    public boolean isDisabled() {
        return this == INACTIVE || this == SUSPENDED || this == EXPIRED || this == LOCKED;
    }
    
    public boolean requiresAdminAction() {
        return this == SUSPENDED || this == LOCKED || this == PENDING;
    }
    
    public boolean canBeActivated() {
        return this == INACTIVE || this == PENDING || this == EXPIRED;
    }
    
    public boolean canBeSuspended() {
        return this == ACTIVE || this == INACTIVE;
    }
}