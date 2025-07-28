package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {

    // 회사별 생산라인 조회
    List<ProductionLine> findByCompanyCompanyId(Long companyId);
    
    // 활성화된 생산라인만 조회
    List<ProductionLine> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);
    
    // 라인 코드로 조회
    Optional<ProductionLine> findByLineCodeAndCompanyCompanyId(String lineCode, Long companyId);
    
    // 라인명으로 조회
    Optional<ProductionLine> findByLineNameAndCompanyCompanyId(String lineName, Long companyId);
    
    // 라인 코드 중복 확인
    boolean existsByLineCodeAndCompanyCompanyId(String lineCode, Long companyId);
    
    // 라인명 중복 확인
    boolean existsByLineNameAndCompanyCompanyId(String lineName, Long companyId);
    
    // 회사의 활성 라인 수 조회
    @Query("SELECT COUNT(pl) FROM ProductionLine pl WHERE pl.company.companyId = :companyId AND pl.isActive = true")
    long countActiveLinesByCompany(@Param("companyId") Long companyId);
    
    // 라인 코드로 단순 조회 (시뮬레이터용)
    Optional<ProductionLine> findByLineCode(String lineCode);
}