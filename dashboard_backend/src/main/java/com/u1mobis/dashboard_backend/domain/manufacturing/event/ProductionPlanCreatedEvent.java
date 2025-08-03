package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlanCreatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String planId;
    private final String lineId;
    private final LocalDate planDate;
    private final int dailyTarget;
    
    public ProductionPlanCreatedEvent(LocalDateTime occurredOn, String planId, String lineId, 
                                    LocalDate planDate, int dailyTarget) {
        this.occurredOn = occurredOn;
        this.planId = planId;
        this.lineId = lineId;
        this.planDate = planDate;
        this.dailyTarget = dailyTarget;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return planId;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public String getLineId() {
        return lineId;
    }
    
    public LocalDate getPlanDate() {
        return planDate;
    }
    
    public int getDailyTarget() {
        return dailyTarget;
    }
}