package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "industry_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryTypeJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_type_id")
    private Long industryTypeId;
    
    @Column(name = "industry_name", nullable = false, unique = true)
    private String industryName;
    
    @Column(name = "industry_code", nullable = false, unique = true, length = 10)
    private String industryCode;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "icon_name")
    private String iconName;
    
    @Column(name = "primary_color", length = 7)
    private String primaryColor;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}