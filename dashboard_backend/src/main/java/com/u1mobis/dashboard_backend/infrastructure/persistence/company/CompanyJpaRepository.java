package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {
    
    Optional<CompanyJpaEntity> findByCompanyCode(String companyCode);
    
    boolean existsByCompanyCode(String companyCode);
    
    @Query("SELECT c FROM CompanyJpaEntity c LEFT JOIN FETCH c.profile p LEFT JOIN FETCH p.industryType WHERE c.companyId = :companyId")
    Optional<CompanyJpaEntity> findByIdWithProfile(@Param("companyId") Long companyId);
    
    @Query("SELECT c FROM CompanyJpaEntity c LEFT JOIN FETCH c.profile p LEFT JOIN FETCH p.industryType WHERE c.companyCode = :companyCode")
    Optional<CompanyJpaEntity> findByCompanyCodeWithProfile(@Param("companyCode") String companyCode);
}