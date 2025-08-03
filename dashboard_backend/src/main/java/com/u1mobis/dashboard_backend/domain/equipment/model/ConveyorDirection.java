package com.u1mobis.dashboard_backend.domain.equipment.model;

public enum ConveyorDirection {
    FORWARD("Forward"),
    REVERSE("Reverse"),
    STOPPED("Stopped");
    
    private final String displayName;
    
    ConveyorDirection(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isMoving() {
        return this != STOPPED;
    }
    
    public ConveyorDirection reverse() {
        return switch (this) {
            case FORWARD -> REVERSE;
            case REVERSE -> FORWARD;
            case STOPPED -> STOPPED;
        };
    }
}