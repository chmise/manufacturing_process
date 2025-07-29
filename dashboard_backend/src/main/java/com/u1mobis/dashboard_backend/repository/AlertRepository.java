package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.Alert;
import com.u1mobis.dashboard_backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    // 회사별 알림 조회 (최신순)
    List<Alert> findByCompanyOrderByTimestampDesc(Company company);
    
    // 회사별 읽지 않은 알림 조회
    List<Alert> findByCompanyAndIsReadFalseOrderByTimestampDesc(Company company);
    
    // 회사별 알림 개수
    long countByCompany(Company company);
    
    // 회사별 읽지 않은 알림 개수
    long countByCompanyAndIsReadFalse(Company company);
    
    // 회사별 알림 전체 삭제
    void deleteByCompany(Company company);
    
    // 특정 알림 ID로 해당 회사의 알림 조회 (보안을 위해)
    @Query("SELECT a FROM Alert a WHERE a.alertId = :alertId AND a.company = :company")
    Alert findByAlertIdAndCompany(@Param("alertId") Long alertId, @Param("company") Company company);
}