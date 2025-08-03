package com.u1mobis.dashboard_backend.domain.alerts.event;

import com.u1mobis.dashboard_backend.domain.alerts.model.AlertSeverity;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class AlertEscalatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String alertId;
    private final String companyId;
    private final AlertSeverity newSeverity;
    private final String reason;
    
    public AlertEscalatedEvent(LocalDateTime occurredOn, String alertId, String companyId,
                             AlertSeverity newSeverity, String reason) {
        this.occurredOn = occurredOn;
        this.alertId = alertId;
        this.companyId = companyId;
        this.newSeverity = newSeverity;
        this.reason = reason;
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
    
    public AlertSeverity getNewSeverity() {
        return newSeverity;
    }
    
    public String getReason() {
        return reason;
    }
}