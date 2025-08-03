package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfileJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private CompanyJpaEntity company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_type_id", nullable = false)
    private IndustryTypeJpaEntity industryType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "company_size", nullable = false)
    private CompanySizeEnum companySize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "production_type", nullable = false)
    private ProductionTypeEnum productionType;
    
    @Column(name = "daily_production_capacity")
    private Integer dailyProductionCapacity;
    
    @Column(name = "automation_level")
    private Integer automationLevel;
    
    @Column(name = "quality_standards", columnDefinition = "TEXT")
    private String qualityStandards;
    
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
    
    @Column(name = "setup_completed", nullable = false)
    @Builder.Default
    private Boolean setupCompleted = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CompanySizeEnum {
        STARTUP, SMALL, MEDIUM, LARGE
    }
    
    public enum ProductionTypeEnum {
        MASS_PRODUCTION, VARIETY_SMALL_LOT, CUSTOM_ORDER, BATCH_PRODUCTION
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}