package com.u1mobis.dashboard_backend.domain.manufacturing.model;

public enum ProductionState {
    SCHEDULED("예정", "생산이 예정되어 있음"),
    IN_PROGRESS("진행중", "현재 생산 진행 중"),
    MOVING_TO_ROBOT("로봇 이동중", "로봇 작업 구역으로 이동 중"),
    ROBOT_WORKING("로봇 작업중", "로봇이 작업을 수행 중"),
    MOVING_TO_INSPECTION("검사 이동중", "품질 검사 구역으로 이동 중"),
    INSPECTING("검사중", "품질 검사를 수행 중"),
    REWORK_REQUIRED("재작업 필요", "품질 불량으로 재작업 필요"),
    COMPLETED("완료", "생산이 성공적으로 완료됨"),
    FAILED("실패", "생산이 실패함"),
    CANCELLED("취소", "생산이 취소됨");
    
    private final String displayName;
    private final String description;
    
    ProductionState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isInProgress() {
        return this == IN_PROGRESS || this == MOVING_TO_ROBOT || 
               this == ROBOT_WORKING || this == MOVING_TO_INSPECTION || 
               this == INSPECTING;
    }
    
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
    
    public boolean canTransitionTo(ProductionState newState) {
        switch (this) {
            case SCHEDULED:
                return newState == IN_PROGRESS || newState == CANCELLED;
            case IN_PROGRESS:
                return newState == MOVING_TO_ROBOT || newState == FAILED || newState == CANCELLED;
            case MOVING_TO_ROBOT:
                return newState == ROBOT_WORKING || newState == FAILED || newState == CANCELLED;
            case ROBOT_WORKING:
                return newState == MOVING_TO_INSPECTION || newState == REWORK_REQUIRED || 
                       newState == FAILED || newState == CANCELLED;
            case MOVING_TO_INSPECTION:
                return newState == INSPECTING || newState == FAILED || newState == CANCELLED;
            case INSPECTING:
                return newState == COMPLETED || newState == REWORK_REQUIRED || 
                       newState == FAILED || newState == CANCELLED;
            case REWORK_REQUIRED:
                return newState == IN_PROGRESS || newState == CANCELLED;
            case COMPLETED:
            case FAILED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}