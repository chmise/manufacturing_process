package com.u1mobis.dashboard_backend.domain.company.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class CompanySetupCompletedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String companyId;
    private final String industryCode;
    
    public CompanySetupCompletedEvent(LocalDateTime occurredOn, String companyId, String industryCode) {
        this.occurredOn = occurredOn;
        this.companyId = companyId;
        this.industryCode = industryCode;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId;
    }
    
    public String getIndustryCode() {
        return industryCode;
    }
}