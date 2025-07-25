package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_lines")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductionLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    @EqualsAndHashCode.Include
    private Long lineId;
    
    @Column(name = "line_name", nullable = false)
    private String lineName; // "Line A", "Line B"
    
    @Column(name = "line_code", nullable = false, unique = true)
    private String lineCode; // "LA", "LB"
    
    @Column(name = "description")
    private String description; // 라인 설명
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드: companyId 조회
    public Long getCompanyId() {
        return company != null ? company.getCompanyId() : null;
    }
    
    // 생성자
    public ProductionLine(Company company, String lineName, String lineCode, String description) {
        this.company = company;
        this.lineName = lineName;
        this.lineCode = lineCode;
        this.description = description;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}