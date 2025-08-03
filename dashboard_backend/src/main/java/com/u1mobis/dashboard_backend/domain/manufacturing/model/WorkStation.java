package com.u1mobis.dashboard_backend.domain.manufacturing.model;

public class WorkStation {
    private final String name;
    private final Position position;
    private final int progressPercentage;
    private final WorkStationType type;
    
    public WorkStation(String name, Position position, int progressPercentage, WorkStationType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Work station name cannot be empty");
        }
        if (position == null) {
            throw new IllegalArgumentException("Work station position cannot be null");
        }
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        
        this.name = name.trim();
        this.position = position;
        this.progressPercentage = progressPercentage;
        this.type = type != null ? type : WorkStationType.GENERAL;
    }
    
    public String getName() {
        return name;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public int getProgressPercentage() {
        return progressPercentage;
    }
    
    public WorkStationType getType() {
        return type;
    }
    
    public boolean isRobotStation() {
        return type == WorkStationType.ROBOT_WORK;
    }
    
    public boolean isInspectionStation() {
        return type == WorkStationType.INSPECTION;
    }
    
    public boolean isAssemblyStation() {
        return type == WorkStationType.ASSEMBLY;
    }
    
    public enum WorkStationType {
        ROBOT_WORK("로봇 작업"),
        INSPECTION("품질 검사"),
        ASSEMBLY("조립"),
        PAINTING("도장"),
        PACKAGING("포장"),
        GENERAL("일반 작업");
        
        private final String displayName;
        
        WorkStationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}