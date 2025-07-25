package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.ProductionCompleted;

@Repository
public interface ProductionCompletedRepository extends JpaRepository<ProductionCompleted, Long> {
    
    // 기존 문제 있는 쿼리들을 수정
    
    // 오늘 완료된 제품 수 조회 (수정됨)
    @Query("SELECT COUNT(pc) FROM ProductionCompleted pc WHERE CAST(pc.timestamp AS DATE) = CURRENT_DATE")
    Long countTodayCompletedProducts();
    
    // 오늘 양품 수 조회 (수정됨)
    @Query("SELECT COUNT(pc) FROM ProductionCompleted pc WHERE CAST(pc.timestamp AS DATE) = CURRENT_DATE AND pc.quality = 'PASS'")
    Long countTodayGoodProducts();
    
    // 오늘 정시 납기 수 조회 (수정됨)
    @Query("SELECT COUNT(pc) FROM ProductionCompleted pc WHERE CAST(pc.timestamp AS DATE) = CURRENT_DATE AND pc.isOnTime = true")
    Long countTodayOnTimeProducts();
    
    // 오늘 일발 통과 수 조회 (수정됨)
    @Query("SELECT COUNT(pc) FROM ProductionCompleted pc WHERE CAST(pc.timestamp AS DATE) = CURRENT_DATE AND pc.isFirstTimePass = true")
    Long countTodayFirstTimePassProducts();
    
    // 평균 사이클 타임 조회 (수정됨)
    @Query("SELECT AVG(pc.cycleTime) FROM ProductionCompleted pc WHERE CAST(pc.timestamp AS DATE) = CURRENT_DATE")
    Double getAverageCycleTimeToday();
    
    // 다른 기본 메서드들
    List<ProductionCompleted> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<ProductionCompleted> findByQuality(String quality);
    List<ProductionCompleted> findByIsOnTime(Boolean isOnTime);
    List<ProductionCompleted> findByIsFirstTimePass(Boolean isFirstTimePass);
    
    // 특정 기간 FTY 계산 (수정됨)
    @Query("SELECT (COUNT(CASE WHEN pc.isFirstTimePass = true THEN 1 END) * 100.0 / COUNT(*)) " +
           "FROM ProductionCompleted pc WHERE pc.timestamp BETWEEN :startTime AND :endTime")
    Double calculateFTYForPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 특정 기간 OTD 계산 (수정됨)
    @Query("SELECT (COUNT(CASE WHEN pc.isOnTime = true THEN 1 END) * 100.0 / COUNT(*)) " +
           "FROM ProductionCompleted pc WHERE pc.timestamp BETWEEN :startTime AND :endTime")
    Double calculateOTDForPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
