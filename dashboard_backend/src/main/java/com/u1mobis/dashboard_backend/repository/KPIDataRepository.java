package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.KPIData;

@Repository
public interface KPIDataRepository extends JpaRepository<KPIData, Long> {
    
    // 최신 KPI 데이터 조회
    Optional<KPIData> findTopByOrderByTimestampDesc();
    
    // 특정 기간 KPI 데이터 조회
    List<KPIData> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    // 오늘 최신 KPI 조회 (수정됨)
    @Query("SELECT kd FROM KPIData kd WHERE CAST(kd.timestamp AS DATE) = CURRENT_DATE ORDER BY kd.timestamp DESC LIMIT 1")
    Optional<KPIData> findLatestKPIToday();
    
    // 평균 OEE 조회
    @Query("SELECT AVG(kd.calculatedOEE) FROM KPIData kd WHERE kd.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageOEE(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 평균 FTY 조회
    @Query("SELECT AVG(kd.calculatedFTY) FROM KPIData kd WHERE kd.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageFTY(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 평균 OTD 조회
    @Query("SELECT AVG(kd.calculatedOTD) FROM KPIData kd WHERE kd.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageOTD(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 시간별 KPI 트렌드 (차트용) - 수정됨
    @Query("SELECT HOUR(kd.timestamp) as hour, " +
           "AVG(kd.calculatedOEE) as avgOEE, " +
           "AVG(kd.calculatedFTY) as avgFTY, " +
           "AVG(kd.calculatedOTD) as avgOTD " +
           "FROM KPIData kd WHERE CAST(kd.timestamp AS DATE) = CURRENT_DATE " +
           "GROUP BY HOUR(kd.timestamp) ORDER BY hour")
    List<Object[]> getHourlyKPITrends();
}