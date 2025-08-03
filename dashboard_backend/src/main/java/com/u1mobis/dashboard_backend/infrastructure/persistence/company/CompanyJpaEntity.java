package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "company_code", nullable = false, unique = true, length = 8)
    private String companyCode;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CompanyProfileJpaEntity profile;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}