package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.ConveyorControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConveyorControlRepository extends JpaRepository<ConveyorControl, String> {
    
    // 라인별 컨베이어 조회
    List<ConveyorControl> findByLineId(Long lineId);
    
    // 라인별 + 명령 상태별 컨베이어 조회
    List<ConveyorControl> findByLineIdAndCommand(Long lineId, String command);
    
    // 특정 명령 상태의 모든 컨베이어 조회
    List<ConveyorControl> findByCommand(String command);
    
    // 회사의 모든 라인 컨베이어 조회
    @Query("SELECT cc FROM ConveyorControl cc JOIN cc.productionLine pl WHERE pl.company.companyId = :companyId")
    List<ConveyorControl> findByCompanyId(@Param("companyId") Long companyId);
    
    // 가동 중인 컨베이어들 조회
    @Query("SELECT cc FROM ConveyorControl cc WHERE cc.lineId = :lineId AND cc.command = 'START'")
    List<ConveyorControl> findOperatingConveyorsByLineId(@Param("lineId") Long lineId);
    
    // 비상 정지 상태의 컨베이어들 조회
    @Query("SELECT cc FROM ConveyorControl cc WHERE cc.lineId = :lineId AND cc.isEmergency = true")
    List<ConveyorControl> findEmergencyStoppedConveyors(@Param("lineId") Long lineId);
    
    // 정비 모드의 컨베이어들 조회
    @Query("SELECT cc FROM ConveyorControl cc WHERE cc.lineId = :lineId AND cc.maintenanceMode = true")
    List<ConveyorControl> findMaintenanceModeConveyors(@Param("lineId") Long lineId);
    
    // 센서가 감지된 컨베이어들 조회
    @Query("SELECT cc FROM ConveyorControl cc WHERE cc.lineId = :lineId AND cc.sensorStatus = true")
    List<ConveyorControl> findConveyorsWithSensorDetection(@Param("lineId") Long lineId);
    
    // 특정 속도 범위의 컨베이어들 조회
    @Query("SELECT cc FROM ConveyorControl cc WHERE cc.lineId = :lineId " +
           "AND cc.speed BETWEEN :minSpeed AND :maxSpeed AND cc.command = 'START'")
    List<ConveyorControl> findByLineIdAndSpeedRange(@Param("lineId") Long lineId,
                                                  @Param("minSpeed") Double minSpeed,
                                                  @Param("maxSpeed") Double maxSpeed);
    
    // 라인의 평균 컨베이어 속도 계산
    @Query("SELECT AVG(cc.speed) FROM ConveyorControl cc WHERE cc.lineId = :lineId AND cc.command = 'START'")
    Double calculateAverageSpeedByLineId(@Param("lineId") Long lineId);
}