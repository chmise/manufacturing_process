package com.u1mobis.dashboard_backend.domain.analytics.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.analytics.model.KPIType;

import java.time.LocalDateTime;

public class KPICalculatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String kpiId;
    private final String companyId;
    private final KPIType kpiType;
    private final double currentValue;
    private final double targetValue;
    
    public KPICalculatedEvent(LocalDateTime occurredOn, String kpiId, String companyId,
                            KPIType kpiType, double currentValue, double targetValue) {
        this.occurredOn = occurredOn;
        this.kpiId = kpiId;
        this.companyId = companyId;
        this.kpiType = kpiType;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return kpiId;
    }
    
    public String getKpiId() {
        return kpiId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public KPIType getKpiType() {
        return kpiType;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public double getTargetValue() {
        return targetValue;
    }
    
    public double getPerformancePercentage() {
        return targetValue != 0 ? (currentValue / targetValue) * 100.0 : 0.0;
    }
}