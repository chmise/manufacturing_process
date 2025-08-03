package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.company.event.CompanyCreatedEvent;

import java.time.LocalDateTime;

public class Company extends AggregateRoot<CompanyId> {
    private CompanyId id;
    private CompanyName name;
    private CompanyCode code;
    private CompanyProfile profile;
    private LocalDateTime createdAt;
    
    protected Company() {
    }
    
    public Company(CompanyName name, CompanyCode code) {
        this.name = name;
        this.code = code;
        this.createdAt = LocalDateTime.now();
        
        raiseEvent(new CompanyCreatedEvent(
            LocalDateTime.now(),
            null, // ID will be set after persistence
            name.getValue(),
            code.getValue()
        ));
    }
    
    public void setId(CompanyId id) {
        this.id = id;
    }
    
    public void setupProfile(IndustryTypeId industryTypeId, CompanySize companySize, 
                            ProductionType productionType, CompanyCapacity capacity,
                            String qualityStandards, String specialRequirements) {
        this.profile = new CompanyProfile(
            industryTypeId, companySize, productionType, capacity,
            qualityStandards, specialRequirements
        );
    }
    
    public void completeSetup() {
        if (profile == null) {
            throw new IllegalStateException("Cannot complete setup without profile");
        }
        profile.markAsCompleted();
    }
    
    public boolean isSetupCompleted() {
        return profile != null && profile.isSetupCompleted();
    }
    
    public void updateProfile(IndustryTypeId industryTypeId, CompanySize companySize,
                             ProductionType productionType, CompanyCapacity capacity,
                             String qualityStandards, String specialRequirements) {
        if (profile == null) {
            setupProfile(industryTypeId, companySize, productionType, capacity,
                        qualityStandards, specialRequirements);
        } else {
            profile.update(industryTypeId, companySize, productionType, capacity,
                          qualityStandards, specialRequirements);
        }
    }
    
    @Override
    public CompanyId getId() {
        return id;
    }
    
    public CompanyName getName() {
        return name;
    }
    
    public CompanyCode getCode() {
        return code;
    }
    
    public CompanyProfile getProfile() {
        return profile;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}