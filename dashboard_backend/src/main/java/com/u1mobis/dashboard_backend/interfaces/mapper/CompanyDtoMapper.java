package com.u1mobis.dashboard_backend.interfaces.mapper;

import com.u1mobis.dashboard_backend.domain.company.model.Company;
import com.u1mobis.dashboard_backend.domain.company.model.IndustryType;
import com.u1mobis.dashboard_backend.interfaces.dto.company.CompanyResponse;
import com.u1mobis.dashboard_backend.interfaces.dto.company.IndustryTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanyDtoMapper {
    
    public CompanyResponse toResponse(Company company) {
        if (company == null) return null;
        
        CompanyResponse.CompanyResponseBuilder builder = CompanyResponse.builder()
                .id(company.getId() != null ? company.getId().getValue() : null)
                .name(company.getName().getValue())
                .code(company.getCode().getValue())
                .createdAt(company.getCreatedAt())
                .setupCompleted(company.isSetupCompleted());
        
        if (company.getProfile() != null) {
            builder.profile(CompanyResponse.CompanyProfileResponse.builder()
                    .industryTypeId(company.getProfile().getIndustryTypeId().getValue())
                    .companySize(company.getProfile().getCompanySize().name())
                    .productionType(company.getProfile().getProductionType().name())
                    .dailyCapacity(company.getProfile().getCapacity() != null ? 
                            company.getProfile().getCapacity().getDailyProductionCapacity() : null)
                    .automationLevel(company.getProfile().getCapacity() != null ? 
                            company.getProfile().getCapacity().getAutomationLevel() : null)
                    .qualityStandards(company.getProfile().getQualityStandards())
                    .specialRequirements(company.getProfile().getSpecialRequirements())
                    .setupCompleted(company.getProfile().isSetupCompleted())
                    .createdAt(company.getProfile().getCreatedAt())
                    .updatedAt(company.getProfile().getUpdatedAt())
                    .build());
        }
        
        return builder.build();
    }
    
    public IndustryTypeResponse toResponse(IndustryType industryType) {
        if (industryType == null) return null;
        
        return IndustryTypeResponse.builder()
                .id(industryType.getId().getValue())
                .name(industryType.getName())
                .code(industryType.getCode())
                .description(industryType.getDescription())
                .iconName(industryType.getIconName())
                .primaryColor(industryType.getPrimaryColor())
                .active(industryType.isActive())
                .build();
    }
}