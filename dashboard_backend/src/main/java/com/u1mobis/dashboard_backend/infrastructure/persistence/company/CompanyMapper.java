package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import com.u1mobis.dashboard_backend.domain.company.model.*;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
    
    public Company toDomain(CompanyJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        
        Company company = new Company(
                new CompanyName(jpaEntity.getCompanyName()),
                new CompanyCode(jpaEntity.getCompanyCode())
        );
        
        company.setId(new CompanyId(String.valueOf(jpaEntity.getCompanyId())));
        
        if (jpaEntity.getProfile() != null) {
            CompanyProfile profile = mapProfileToDomain(jpaEntity.getProfile());
            company.setupProfile(
                    profile.getIndustryTypeId(),
                    profile.getCompanySize(),
                    profile.getProductionType(),
                    profile.getCapacity(),
                    profile.getQualityStandards(),
                    profile.getSpecialRequirements()
            );
            
            if (profile.isSetupCompleted()) {
                company.completeSetup();
            }
        }
        
        return company;
    }
    
    public CompanyJpaEntity toJpaEntity(Company domain) {
        if (domain == null) return null;
        
        CompanyJpaEntity jpaEntity = CompanyJpaEntity.builder()
                .companyId(domain.getId() != null ? Long.valueOf(domain.getId().getValue()) : null)
                .companyName(domain.getName().getValue())
                .companyCode(domain.getCode().getValue())
                .createdAt(domain.getCreatedAt())
                .build();
        
        if (domain.getProfile() != null) {
            CompanyProfileJpaEntity profileEntity = mapProfileToJpaEntity(domain.getProfile(), jpaEntity);
            jpaEntity.setProfile(profileEntity);
        }
        
        return jpaEntity;
    }
    
    private CompanyProfile mapProfileToDomain(CompanyProfileJpaEntity profileEntity) {
        CompanySize size = CompanySize.valueOf(profileEntity.getCompanySize().name());
        ProductionType type = ProductionType.valueOf(profileEntity.getProductionType().name());
        CompanyCapacity capacity = new CompanyCapacity(
                profileEntity.getDailyProductionCapacity(),
                profileEntity.getAutomationLevel()
        );
        
        CompanyProfile profile = new CompanyProfile(
                new IndustryTypeId(String.valueOf(profileEntity.getIndustryType().getIndustryTypeId())),
                size,
                type,
                capacity,
                profileEntity.getQualityStandards(),
                profileEntity.getSpecialRequirements()
        );
        
        return profile;
    }
    
    private CompanyProfileJpaEntity mapProfileToJpaEntity(CompanyProfile profile, CompanyJpaEntity companyEntity) {
        return CompanyProfileJpaEntity.builder()
                .company(companyEntity)
                .industryType(IndustryTypeJpaEntity.builder()
                        .industryTypeId(Long.valueOf(profile.getIndustryTypeId().getValue()))
                        .build())
                .companySize(CompanyProfileJpaEntity.CompanySizeEnum.valueOf(profile.getCompanySize().name()))
                .productionType(CompanyProfileJpaEntity.ProductionTypeEnum.valueOf(profile.getProductionType().name()))
                .dailyProductionCapacity(profile.getCapacity() != null ? profile.getCapacity().getDailyProductionCapacity() : null)
                .automationLevel(profile.getCapacity() != null ? profile.getCapacity().getAutomationLevel() : null)
                .qualityStandards(profile.getQualityStandards())
                .specialRequirements(profile.getSpecialRequirements())
                .setupCompleted(profile.isSetupCompleted())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
    
    public IndustryType toDomain(IndustryTypeJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        
        IndustryType industryType = new IndustryType(
                jpaEntity.getIndustryName(),
                jpaEntity.getIndustryCode(),
                jpaEntity.getDescription()
        );
        
        industryType.setId(new IndustryTypeId(String.valueOf(jpaEntity.getIndustryTypeId())));
        industryType.updateDetails(
                jpaEntity.getIndustryName(),
                jpaEntity.getDescription(),
                jpaEntity.getIconName(),
                jpaEntity.getPrimaryColor()
        );
        
        if (!jpaEntity.getIsActive()) {
            industryType.deactivate();
        }
        
        return industryType;
    }
}