package com.u1mobis.dashboard_backend.domain.user.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class UserPasswordChangedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String userId;
    private final String companyId;
    private final String username;
    private final String changedBy;
    
    public UserPasswordChangedEvent(LocalDateTime occurredOn, String userId, String companyId,
                                  String username, String changedBy) {
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.changedBy = changedBy;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return userId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public boolean isSelfChange() {
        return username.equals(changedBy);
    }
    
    public boolean isAdminChange() {
        return !isSelfChange();
    }
}