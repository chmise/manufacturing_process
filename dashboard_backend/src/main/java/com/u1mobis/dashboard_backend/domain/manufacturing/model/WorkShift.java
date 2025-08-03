package com.u1mobis.dashboard_backend.domain.manufacturing.model;

public enum WorkShift {
    DAY_SHIFT("주간", 8, 17),
    NIGHT_SHIFT("야간", 20, 5),
    FULL_DAY("24시간", 0, 24);
    
    private final String displayName;
    private final int startHour;
    private final int endHour;
    
    WorkShift(String displayName, int startHour, int endHour) {
        this.displayName = displayName;
        this.startHour = startHour;
        this.endHour = endHour;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getStartHour() {
        return startHour;
    }
    
    public int getEndHour() {
        return endHour;
    }
    
    public int getWorkingHours() {
        if (this == NIGHT_SHIFT) {
            return 24 - startHour + endHour; // 야간 근무 시간 계산
        } else if (this == FULL_DAY) {
            return 24;
        } else {
            return endHour - startHour;
        }
    }
    
    public boolean isWorkingTime(int hour) {
        if (this == FULL_DAY) {
            return true;
        } else if (this == NIGHT_SHIFT) {
            return hour >= startHour || hour < endHour;
        } else {
            return hour >= startHour && hour < endHour;
        }
    }
}