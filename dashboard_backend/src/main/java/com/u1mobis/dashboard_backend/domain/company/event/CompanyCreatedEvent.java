package com.u1mobis.dashboard_backend.domain.company.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class CompanyCreatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String companyId;
    private final String companyName;
    private final String companyCode;
    
    public CompanyCreatedEvent(LocalDateTime occurredOn, String companyId, String companyName, String companyCode) {
        this.occurredOn = occurredOn;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyCode = companyCode;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getCompanyCode() {
        return companyCode;
    }
}