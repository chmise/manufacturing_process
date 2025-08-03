package com.u1mobis.dashboard_backend.domain.user.model;

public enum UserRole {
    ADMIN("Administrator", "Full system access with all privileges", 100),
    MANAGER("Manager", "Management access with operational controls", 80),
    SUPERVISOR("Supervisor", "Supervisory access with monitoring capabilities", 60),
    OPERATOR("Operator", "Operational access with production controls", 40),
    VIEWER("Viewer", "Read-only access to dashboards and reports", 20),
    MAINTENANCE("Maintenance", "Maintenance access with equipment controls", 50),
    QUALITY("Quality", "Quality assurance access with inspection tools", 45);
    
    private final String displayName;
    private final String description;
    private final int authorityLevel;
    
    UserRole(String displayName, String description, int authorityLevel) {
        this.displayName = displayName;
        this.description = description;
        this.authorityLevel = authorityLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getAuthorityLevel() {
        return authorityLevel;
    }
    
    public boolean hasHigherAuthorityThan(UserRole other) {
        return this.authorityLevel > other.authorityLevel;
    }
    
    public boolean hasEqualOrHigherAuthorityThan(UserRole other) {
        return this.authorityLevel >= other.authorityLevel;
    }
    
    public boolean canAccess(UserRole requiredRole) {
        return hasEqualOrHigherAuthorityThan(requiredRole);
    }
    
    public boolean isAdministrative() {
        return this == ADMIN || this == MANAGER;
    }
    
    public boolean isOperational() {
        return this == SUPERVISOR || this == OPERATOR || this == MAINTENANCE;
    }
    
    public boolean isReadOnly() {
        return this == VIEWER;
    }
    
    public boolean canManageUsers() {
        return this == ADMIN;
    }
    
    public boolean canManageProduction() {
        return this == ADMIN || this == MANAGER || this == SUPERVISOR;
    }
    
    public boolean canOperateEquipment() {
        return this == ADMIN || this == MANAGER || this == SUPERVISOR || 
               this == OPERATOR || this == MAINTENANCE;
    }
    
    public boolean canPerformMaintenance() {
        return this == ADMIN || this == MANAGER || this == MAINTENANCE;
    }
    
    public boolean canAccessQualityTools() {
        return this == ADMIN || this == MANAGER || this == SUPERVISOR || this == QUALITY;
    }
}