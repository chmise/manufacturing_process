package com.u1mobis.dashboard_backend.interfaces.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyProfileSetupRequest {
    private Long industryTypeId;
    private String companySize;
    private String productionType;
    private Integer dailyCapacity;
    private Integer automationLevel;
    private String qualityStandards;
    private String specialRequirements;
}