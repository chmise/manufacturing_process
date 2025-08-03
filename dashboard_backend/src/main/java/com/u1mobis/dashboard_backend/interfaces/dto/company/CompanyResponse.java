package com.u1mobis.dashboard_backend.interfaces.dto.company;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CompanyResponse {
    private String id;
    private String name;
    private String code;
    private LocalDateTime createdAt;
    private CompanyProfileResponse profile;
    private boolean setupCompleted;
    
    @Getter
    @Setter
    @Builder
    public static class CompanyProfileResponse {
        private String industryTypeId;
        private String industryTypeName;
        private String companySize;
        private String productionType;
        private Integer dailyCapacity;
        private Integer automationLevel;
        private String qualityStandards;
        private String specialRequirements;
        private boolean setupCompleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}