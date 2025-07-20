package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.QualityRecord;

@Repository
public interface QualityRecordRepository extends JpaRepository<QualityRecord, Long> {
    
    // 특정 제품의 품질 기록 조회
    List<QualityRecord> findByProductIdOrderByTimestampDesc(String productId);
    
    // 특정 로봇의 품질 기록 조회
    List<QualityRecord> findByRobotIdAndTimestampBetween(String robotId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 불량 기록만 조회
    List<QualityRecord> findByResult(String result);
    
    // 특정 기간 불량률 계산
    @Query("SELECT (COUNT(CASE WHEN qr.result = 'FAIL' THEN 1 END) * 100.0 / COUNT(*)) " +
           "FROM QualityRecord qr WHERE qr.timestamp BETWEEN :startTime AND :endTime")
    Double calculateDefectRateForPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 불량 유형별 통계
    @Query("SELECT qr.defectReason, COUNT(qr) FROM QualityRecord qr " +
           "WHERE qr.result = 'FAIL' AND qr.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY qr.defectReason ORDER BY COUNT(qr) DESC")
    List<Object[]> getDefectStatistics(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 로봇별 품질 성능
    @Query("SELECT qr.robotId, " +
           "COUNT(*) as totalChecks, " +
           "COUNT(CASE WHEN qr.result = 'PASS' THEN 1 END) as passCount, " +
           "(COUNT(CASE WHEN qr.result = 'PASS' THEN 1 END) * 100.0 / COUNT(*)) as passRate " +
           "FROM QualityRecord qr WHERE qr.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY qr.robotId")
    List<Object[]> getRobotQualityPerformance(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
