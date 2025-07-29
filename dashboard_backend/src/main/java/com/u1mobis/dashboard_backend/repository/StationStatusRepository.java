package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.StationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationStatusRepository extends JpaRepository<StationStatus, String> {
    
    // 라인별 스테이션 상태 조회
    List<StationStatus> findByLineId(Long lineId);
    
    // 라인별 + 상태별 스테이션 조회
    List<StationStatus> findByLineIdAndStatus(Long lineId, String status);
    
    // 특정 상태의 모든 스테이션 조회
    List<StationStatus> findByStatus(String status);
    
    // 회사의 모든 라인 스테이션 조회
    @Query("SELECT ss FROM StationStatus ss JOIN ss.productionLine pl WHERE pl.company.companyId = :companyId")
    List<StationStatus> findByCompanyId(@Param("companyId") Long companyId);
    
    // 가동 중인 스테이션들 조회
    @Query("SELECT ss FROM StationStatus ss WHERE ss.lineId = :lineId AND ss.status = 'OPERATING'")
    List<StationStatus> findOperatingStationsByLineId(@Param("lineId") Long lineId);
    
    // 효율이 특정 값 이하인 스테이션들 조회
    @Query("SELECT ss FROM StationStatus ss WHERE ss.lineId = :lineId AND ss.efficiency < :minEfficiency")
    List<StationStatus> findLowEfficiencyStations(@Param("lineId") Long lineId, @Param("minEfficiency") Double minEfficiency);
    
    // 현재 작업 중인 제품이 있는 스테이션들 조회
    @Query("SELECT ss FROM StationStatus ss WHERE ss.lineId = :lineId AND ss.currentProduct IS NOT NULL")
    List<StationStatus> findStationsWithCurrentProduct(@Param("lineId") Long lineId);
    
    // 특정 제품이 작업 중인 스테이션 조회
    Optional<StationStatus> findByCurrentProduct(String productId);
    
    // 라인의 평균 효율 계산
    @Query("SELECT AVG(ss.efficiency) FROM StationStatus ss WHERE ss.lineId = :lineId AND ss.status = 'OPERATING'")
    Double calculateAverageEfficiencyByLineId(@Param("lineId") Long lineId);
}