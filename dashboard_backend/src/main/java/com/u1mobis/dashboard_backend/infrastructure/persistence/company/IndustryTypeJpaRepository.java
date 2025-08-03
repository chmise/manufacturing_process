package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IndustryTypeJpaRepository extends JpaRepository<IndustryTypeJpaEntity, Long> {
    
    @Query("SELECT i FROM IndustryTypeJpaEntity i WHERE i.isActive = true ORDER BY i.industryName")
    List<IndustryTypeJpaEntity> findAllActiveOrderByName();
    
    Optional<IndustryTypeJpaEntity> findByIndustryCode(String industryCode);
    
    List<IndustryTypeJpaEntity> findByIsActiveTrueOrderByIndustryName();
}