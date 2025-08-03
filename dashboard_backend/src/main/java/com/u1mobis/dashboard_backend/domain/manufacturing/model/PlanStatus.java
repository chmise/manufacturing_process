package com.u1mobis.dashboard_backend.domain.manufacturing.model;

public enum PlanStatus {
    PLANNING("계획중", "생산 계획 수립 중"),
    IN_PROGRESS("진행중", "생산 계획 실행 중"),
    COMPLETED("완료", "생산 계획 완료"),
    CANCELLED("취소", "생산 계획 취소됨"),
    ON_HOLD("보류", "생산 계획 일시 보류");
    
    private final String displayName;
    private final String description;
    
    PlanStatus(String displayName, String description) {
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
    
    public boolean canTransitionTo(PlanStatus newStatus) {
        switch (this) {
            case PLANNING:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED || newStatus == ON_HOLD;
            case IN_PROGRESS:
                return newStatus == COMPLETED || newStatus == CANCELLED || newStatus == ON_HOLD;
            case ON_HOLD:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}