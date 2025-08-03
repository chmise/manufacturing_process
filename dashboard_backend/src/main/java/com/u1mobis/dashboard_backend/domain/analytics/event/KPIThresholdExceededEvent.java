package com.u1mobis.dashboard_backend.domain.analytics.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.analytics.model.KPIType;

import java.time.LocalDateTime;

public class KPIThresholdExceededEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String kpiId;
    private final String companyId;
    private final KPIType kpiType;
    private final double currentValue;
    private final double minimumThreshold;
    private final double maximumThreshold;
    private final String violationType; // "BELOW" or "ABOVE"
    
    public KPIThresholdExceededEvent(LocalDateTime occurredOn, String kpiId, String companyId,
                                   KPIType kpiType, double currentValue, double minimumThreshold,
                                   double maximumThreshold, String violationType) {
        this.occurredOn = occurredOn;
        this.kpiId = kpiId;
        this.companyId = companyId;
        this.kpiType = kpiType;
        this.currentValue = currentValue;
        this.minimumThreshold = minimumThreshold;
        this.maximumThreshold = maximumThreshold;
        this.violationType = violationType;
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
    
    public double getMinimumThreshold() {
        return minimumThreshold;
    }
    
    public double getMaximumThreshold() {
        return maximumThreshold;
    }
    
    public String getViolationType() {
        return violationType;
    }
    
    public boolean isBelowThreshold() {
        return "BELOW".equals(violationType);
    }
    
    public boolean isAboveThreshold() {
        return "ABOVE".equals(violationType);
    }
    
    public double getDeviationAmount() {
        if (isBelowThreshold()) {
            return minimumThreshold - currentValue;
        } else {
            return currentValue - maximumThreshold;
        }
    }
}