package com.u1mobis.dashboard_backend.domain.alerts.event;

import com.u1mobis.dashboard_backend.domain.alerts.model.AlertSeverity;
import com.u1mobis.dashboard_backend.domain.alerts.model.AlertType;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class AlertCreatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String alertId;
    private final String companyId;
    private final AlertType type;
    private final AlertSeverity severity;
    private final String title;
    private final String message;
    private final String source;
    
    public AlertCreatedEvent(LocalDateTime occurredOn, String alertId, String companyId, 
                           AlertType type, AlertSeverity severity, String title, 
                           String message, String source) {
        this.occurredOn = occurredOn;
        this.alertId = alertId;
        this.companyId = companyId;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.source = source;
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
    
    public AlertType getType() {
        return type;
    }
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSource() {
        return source;
    }
}