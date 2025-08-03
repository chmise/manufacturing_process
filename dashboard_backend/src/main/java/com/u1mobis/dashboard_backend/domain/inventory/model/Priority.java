package com.u1mobis.dashboard_backend.domain.inventory.model;

public enum Priority {
    LOW(1, "Low Priority", "Standard materials with no time constraints"),
    MEDIUM(2, "Medium Priority", "Important materials with moderate constraints"),
    HIGH(3, "High Priority", "Critical materials requiring attention"),
    CRITICAL(4, "Critical Priority", "Mission-critical materials requiring immediate action");
    
    private final int level;
    private final String displayName;
    private final String description;
    
    Priority(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
    
    public boolean isLowerThan(Priority other) {
        return this.level < other.level;
    }
    
    public boolean isCriticalLevel() {
        return this == CRITICAL || this == HIGH;
    }
}