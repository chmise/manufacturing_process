package com.u1mobis.dashboard_backend.domain.user.event;

import com.u1mobis.dashboard_backend.domain.user.model.UserRole;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class UserRoleChangedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String userId;
    private final String companyId;
    private final String username;
    private final UserRole previousRole;
    private final UserRole newRole;
    private final String changedBy;
    
    public UserRoleChangedEvent(LocalDateTime occurredOn, String userId, String companyId,
                              String username, UserRole previousRole, UserRole newRole, String changedBy) {
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.previousRole = previousRole;
        this.newRole = newRole;
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
    
    public UserRole getPreviousRole() {
        return previousRole;
    }
    
    public UserRole getNewRole() {
        return newRole;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public boolean isAuthorityIncreased() {
        return newRole.hasHigherAuthorityThan(previousRole);
    }
    
    public boolean isAuthorityDecreased() {
        return previousRole.hasHigherAuthorityThan(newRole);
    }
}