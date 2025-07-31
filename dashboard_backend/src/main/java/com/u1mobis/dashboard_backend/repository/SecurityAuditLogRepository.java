package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    
    // 기본 조회 메서드들
    Page<SecurityAuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<SecurityAuditLog> findByCompanyIdOrderByTimestampDesc(Long companyId, Pageable pageable);
    
    Page<SecurityAuditLog> findByEventTypeOrderByTimestampDesc(
        SecurityAuditLog.EventType eventType, Pageable pageable);
    
    Page<SecurityAuditLog> findByRiskLevelOrderByTimestampDesc(
        SecurityAuditLog.RiskLevel riskLevel, Pageable pageable);
    
    // 시간 범위 조회
    Page<SecurityAuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 사용자별 시간 범위 조회
    Page<SecurityAuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
        Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 회사별 시간 범위 조회
    Page<SecurityAuditLog> findByCompanyIdAndTimestampBetweenOrderByTimestampDesc(
        Long companyId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // 복합 조건 조회
    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "(:userId IS NULL OR s.userId = :userId) AND " +
           "(:companyId IS NULL OR s.companyId = :companyId) AND " +
           "(:eventType IS NULL OR s.eventType = :eventType) AND " +
           "(:riskLevel IS NULL OR s.riskLevel = :riskLevel) AND " +
           "(:clientIp IS NULL OR s.clientIp = :clientIp) AND " +
           "(:startTime IS NULL OR s.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR s.timestamp <= :endTime) " +
           "ORDER BY s.timestamp DESC")
    Page<SecurityAuditLog> findByMultipleCriteria(
        @Param("userId") Long userId,
        @Param("companyId") Long companyId,
        @Param("eventType") SecurityAuditLog.EventType eventType,
        @Param("riskLevel") SecurityAuditLog.RiskLevel riskLevel,
        @Param("clientIp") String clientIp,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    // 통계 조회 메서드들
    @Query("SELECT s.eventType, COUNT(s) FROM SecurityAuditLog s WHERE " +
           "s.timestamp >= :startTime AND s.timestamp <= :endTime " +
           "GROUP BY s.eventType ORDER BY COUNT(s) DESC")
    List<Object[]> getEventTypeStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT s.riskLevel, COUNT(s) FROM SecurityAuditLog s WHERE " +
           "s.timestamp >= :startTime AND s.timestamp <= :endTime " +
           "GROUP BY s.riskLevel ORDER BY s.riskLevel DESC")
    List<Object[]> getRiskLevelStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT DATE(s.timestamp), COUNT(s) FROM SecurityAuditLog s WHERE " +
           "s.timestamp >= :startTime AND s.timestamp <= :endTime " +
           "GROUP BY DATE(s.timestamp) ORDER BY DATE(s.timestamp)")
    List<Object[]> getDailyEventCounts(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 실패한 로그인 시도 조회
    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "s.eventType = 'LOGIN_FAILURE' AND " +
           "s.clientIp = :clientIp AND " +
           "s.timestamp >= :since " +
           "ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findFailedLoginAttempts(
        @Param("clientIp") String clientIp,
        @Param("since") LocalDateTime since
    );
    
    // 특정 사용자의 의심스러운 활동 조회
    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "s.userId = :userId AND " +
           "s.riskLevel IN ('HIGH', 'CRITICAL') AND " +
           "s.timestamp >= :since " +
           "ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findSuspiciousActivitiesByUser(
        @Param("userId") Long userId,
        @Param("since") LocalDateTime since
    );
    
    // 회사별 보안 이벤트 요약
    @Query("SELECT " +
           "COUNT(s) as totalEvents, " +
           "SUM(CASE WHEN s.success = true THEN 1 ELSE 0 END) as successfulEvents, " +
           "SUM(CASE WHEN s.success = false THEN 1 ELSE 0 END) as failedEvents, " +
           "SUM(CASE WHEN s.riskLevel = 'HIGH' OR s.riskLevel = 'CRITICAL' THEN 1 ELSE 0 END) as highRiskEvents " +
           "FROM SecurityAuditLog s WHERE " +
           "s.companyId = :companyId AND " +
           "s.timestamp >= :startTime AND s.timestamp <= :endTime")
    Object[] getCompanySecuritySummary(
        @Param("companyId") Long companyId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 최근 보안 이벤트 조회 (실시간 모니터링용)
    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "s.riskLevel IN ('HIGH', 'CRITICAL') AND " +
           "s.timestamp >= :since " +
           "ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findRecentHighRiskEvents(@Param("since") LocalDateTime since);
    
    // IP별 이벤트 통계
    @Query("SELECT s.clientIp, COUNT(s), " +
           "SUM(CASE WHEN s.success = false THEN 1 ELSE 0 END) as failures " +
           "FROM SecurityAuditLog s WHERE " +
           "s.timestamp >= :startTime AND s.timestamp <= :endTime " +
           "GROUP BY s.clientIp " +
           "HAVING COUNT(s) > :minCount " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> getIpStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("minCount") Long minCount
    );
    
    // 중복 이벤트 감지 (같은 사용자, 같은 IP, 짧은 시간 내 여러 시도)
    @Query("SELECT s FROM SecurityAuditLog s WHERE " +
           "s.userId = :userId AND " +
           "s.clientIp = :clientIp AND " +
           "s.eventType = :eventType AND " +
           "s.timestamp >= :since " +
           "ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findDuplicateEvents(
        @Param("userId") Long userId,
        @Param("clientIp") String clientIp,
        @Param("eventType") SecurityAuditLog.EventType eventType,
        @Param("since") LocalDateTime since
    );
}