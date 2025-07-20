package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.u1mobis.dashboard_backend.entity.CurrentProduction;

@Repository
public interface CurrentProductionRepository extends JpaRepository<CurrentProduction, String> {
    
    // 현재 생산 중인 제품 조회
    List<CurrentProduction> findByStatus(String status);
    
    // 특정 스테이션의 제품 조회
    List<CurrentProduction> findByCurrentStation(String currentStation);
    
    // 재작업 횟수별 조회
    List<CurrentProduction> findByReworkCountGreaterThan(Integer reworkCount);
    
    // 납기일이 임박한 제품 조회 (24시간 이내)
    @Query("SELECT cp FROM CurrentProduction cp WHERE cp.dueDate BETWEEN :startTime AND :endTime AND cp.status = 'PROCESSING'")
    List<CurrentProduction> findProductsWithUpcomingDeadline(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    // 특정 기간 동안 시작된 제품 조회
    List<CurrentProduction> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 재작업 횟수 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE CurrentProduction cp SET cp.reworkCount = cp.reworkCount + 1 WHERE cp.productId = :productId")
    int incrementReworkCount(@Param("productId") String productId);
    
    // 현재 스테이션 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE CurrentProduction cp SET cp.currentStation = :station WHERE cp.productId = :productId")
    int updateCurrentStation(@Param("productId") String productId, @Param("station") String station);
}