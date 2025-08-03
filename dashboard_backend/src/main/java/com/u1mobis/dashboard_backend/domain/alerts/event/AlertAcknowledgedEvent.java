package com.u1mobis.dashboard_backend.domain.alerts.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class AlertAcknowledgedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String alertId;
    private final String companyId;
    private final String acknowledgedBy;
    private final String comment;
    
    public AlertAcknowledgedEvent(LocalDateTime occurredOn, String alertId, String companyId,
                                String acknowledgedBy, String comment) {
        this.occurredOn = occurredOn;
        this.alertId = alertId;
        this.companyId = companyId;
        this.acknowledgedBy = acknowledgedBy;
        this.comment = comment;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return alertId;
    }
    
    public String getAlertId() {
        return alertId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }
    
    public String getComment() {
        return comment;
    }
}