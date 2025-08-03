package com.u1mobis.dashboard_backend.domain.equipment.model;

public enum RobotType {
    ASSEMBLY_ROBOT("조립 로봇", "부품 조립 작업"),
    WELDING_ROBOT("용접 로봇", "용접 작업 수행"),
    PAINTING_ROBOT("도장 로봇", "도장 작업 수행"),
    PACKAGING_ROBOT("포장 로봇", "제품 포장 작업"),
    INSPECTION_ROBOT("검사 로봇", "품질 검사 수행"),
    MATERIAL_HANDLING_ROBOT("운반 로봇", "자재 운반 작업"),
    GENERAL_PURPOSE_ROBOT("범용 로봇", "다목적 작업 수행");
    
    private final String displayName;
    private final String description;
    
    RobotType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAssemblyType() {
        return this == ASSEMBLY_ROBOT || this == GENERAL_PURPOSE_ROBOT;
    }
    
    public boolean isQualityType() {
        return this == INSPECTION_ROBOT;
    }
    
    public boolean isProcessingType() {
        return this == WELDING_ROBOT || this == PAINTING_ROBOT;
    }
    
    public boolean isLogisticsType() {
        return this == MATERIAL_HANDLING_ROBOT || this == PACKAGING_ROBOT;
    }
}