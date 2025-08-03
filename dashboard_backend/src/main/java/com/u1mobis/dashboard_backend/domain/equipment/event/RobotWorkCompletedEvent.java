package com.u1mobis.dashboard_backend.domain.equipment.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class RobotWorkCompletedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String robotId;
    private final String companyId;
    private final String workType;
    
    public RobotWorkCompletedEvent(LocalDateTime occurredOn, String robotId, String companyId, String workType) {
        this.occurredOn = occurredOn;
        this.robotId = robotId;
        this.companyId = companyId;
        this.workType = workType;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId;
    }
    
    public String getRobotId() {
        return robotId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getWorkType() {
        return workType;
    }
}