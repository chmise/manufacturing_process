package com.u1mobis.dashboard_backend.domain.alerts.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class AlertResolvedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String alertId;
    private final String companyId;
    private final String resolvedBy;
    private final String resolution;
    private final long resolutionTimeMinutes;
    
    public AlertResolvedEvent(LocalDateTime occurredOn, String alertId, String companyId,
                            String resolvedBy, String resolution, long resolutionTimeMinutes) {
        this.occurredOn = occurredOn;
        this.alertId = alertId;
        this.companyId = companyId;
        this.resolvedBy = resolvedBy;
        this.resolution = resolution;
        this.resolutionTimeMinutes = resolutionTimeMinutes;
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
    
    public String getResolvedBy() {
        return resolvedBy;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public long getResolutionTimeMinutes() {
        return resolutionTimeMinutes;
    }
}