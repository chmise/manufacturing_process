package com.u1mobis.dashboard_backend.domain.equipment.model;

public enum EquipmentStatus {
    OPERATIONAL("가동", "정상 작동 중"),
    STOPPED("정지", "작동이 중단됨"),
    PAUSED("일시정지", "일시적으로 중단됨"),
    MAINTENANCE("정비중", "정비 작업 중"),
    ERROR("오류", "오류 발생으로 중단"),
    EMERGENCY_STOP("비상정지", "비상 상황으로 인한 정지");
    
    private final String displayName;
    private final String description;
    
    EquipmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isOperational() {
        return this == OPERATIONAL;
    }
    
    public boolean isStopped() {
        return this == STOPPED || this == PAUSED || this == MAINTENANCE || 
               this == ERROR || this == EMERGENCY_STOP;
    }
    
    public boolean requiresAttention() {
        return this == ERROR || this == EMERGENCY_STOP || this == MAINTENANCE;
    }
    
    public boolean canStart() {
        return this == STOPPED || this == PAUSED;
    }
    
    public boolean canStop() {
        return this == OPERATIONAL;
    }
    
    public boolean canTransitionTo(EquipmentStatus newStatus) {
        switch (this) {
            case OPERATIONAL:
                return newStatus == STOPPED || newStatus == PAUSED || 
                       newStatus == MAINTENANCE || newStatus == ERROR || 
                       newStatus == EMERGENCY_STOP;
            case STOPPED:
                return newStatus == OPERATIONAL || newStatus == MAINTENANCE || 
                       newStatus == ERROR || newStatus == EMERGENCY_STOP;
            case PAUSED:
                return newStatus == OPERATIONAL || newStatus == STOPPED || 
                       newStatus == MAINTENANCE || newStatus == ERROR || 
                       newStatus == EMERGENCY_STOP;
            case MAINTENANCE:
                return newStatus == STOPPED || newStatus == ERROR;
            case ERROR:
                return newStatus == STOPPED || newStatus == MAINTENANCE;
            case EMERGENCY_STOP:
                return newStatus == STOPPED || newStatus == MAINTENANCE;
            default:
                return false;
        }
    }
}