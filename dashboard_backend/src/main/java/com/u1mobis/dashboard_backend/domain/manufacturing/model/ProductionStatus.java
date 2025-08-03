package com.u1mobis.dashboard_backend.domain.manufacturing.model;

public enum ProductionStatus {
    SCHEDULED("예정", "생산이 예정되어 있음"),
    IN_PROGRESS("진행중", "현재 생산 진행 중"),
    PAUSED("일시정지", "생산이 일시적으로 중단됨"),
    COMPLETED("완료", "생산이 성공적으로 완료됨"),
    FAILED("실패", "생산이 실패함"),
    CANCELLED("취소", "생산이 취소됨");
    
    private final String displayName;
    private final String description;
    
    ProductionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == IN_PROGRESS;
    }
    
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    public boolean canTransitionTo(ProductionStatus newStatus) {
        switch (this) {
            case SCHEDULED:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == PAUSED || newStatus == COMPLETED || 
                       newStatus == FAILED || newStatus == CANCELLED;
            case PAUSED:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case COMPLETED:
            case FAILED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}