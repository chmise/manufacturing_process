package com.u1mobis.dashboard_backend.domain.company.model;

import java.time.LocalDateTime;

public class CompanyProfile {
    private IndustryTypeId industryTypeId;
    private CompanySize companySize;
    private ProductionType productionType;
    private CompanyCapacity capacity;
    private String qualityStandards;
    private String specialRequirements;
    private boolean setupCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected CompanyProfile() {
    }
    
    public CompanyProfile(IndustryTypeId industryTypeId, CompanySize companySize,
                         ProductionType productionType, CompanyCapacity capacity,
                         String qualityStandards, String specialRequirements) {
        this.industryTypeId = industryTypeId;
        this.companySize = companySize;
        this.productionType = productionType;
        this.capacity = capacity;
        this.qualityStandards = qualityStandards;
        this.specialRequirements = specialRequirements;
        this.setupCompleted = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public void update(IndustryTypeId industryTypeId, CompanySize companySize,
                      ProductionType productionType, CompanyCapacity capacity,
                      String qualityStandards, String specialRequirements) {
        this.industryTypeId = industryTypeId;
        this.companySize = companySize;
        this.productionType = productionType;
        this.capacity = capacity;
        this.qualityStandards = qualityStandards;
        this.specialRequirements = specialRequirements;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        if (industryTypeId == null || companySize == null || productionType == null) {
            throw new IllegalStateException("Cannot complete setup with incomplete profile");
        }
        this.setupCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isSetupCompleted() {
        return setupCompleted;
    }
    
    public boolean requiresHighAutomation() {
        return productionType.isSuitableForAutomation() && 
               companySize != CompanySize.STARTUP;
    }
    
    public boolean needsFlexibleScheduling() {
        return productionType.requiresFlexibleScheduling();
    }
    
    public IndustryTypeId getIndustryTypeId() {
        return industryTypeId;
    }
    
    public CompanySize getCompanySize() {
        return companySize;
    }
    
    public ProductionType getProductionType() {
        return productionType;
    }
    
    public CompanyCapacity getCapacity() {
        return capacity;
    }
    
    public String getQualityStandards() {
        return qualityStandards;
    }
    
    public String getSpecialRequirements() {
        return specialRequirements;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}