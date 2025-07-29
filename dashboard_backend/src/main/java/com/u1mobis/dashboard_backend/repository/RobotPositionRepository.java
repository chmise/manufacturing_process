package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.RobotPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RobotPositionRepository extends JpaRepository<RobotPosition, String> {
    
    // 라인별 로봇 위치 조회
    List<RobotPosition> findByLineId(Long lineId);
    
    // 라인별 + 활성 로봇들 조회
    List<RobotPosition> findByLineIdAndIsActiveTrue(Long lineId);
    
    // 특정 동작 상태의 로봇들 조회
    List<RobotPosition> findByLineIdAndCurrentAction(Long lineId, String currentAction);
    
    // 회사의 모든 라인 로봇 조회
    @Query("SELECT rp FROM RobotPosition rp JOIN rp.productionLine pl WHERE pl.company.companyId = :companyId")
    List<RobotPosition> findByCompanyId(@Param("companyId") Long companyId);
    
    // 배터리 레벨이 특정 값 이하인 로봇들 조회
    @Query("SELECT rp FROM RobotPosition rp WHERE rp.lineId = :lineId AND rp.batteryLevel <= :minBattery")
    List<RobotPosition> findLowBatteryRobots(@Param("lineId") Long lineId, @Param("minBattery") Integer minBattery);
    
    // 이동 중인 로봇들 조회
    @Query("SELECT rp FROM RobotPosition rp WHERE rp.lineId = :lineId AND rp.currentAction = 'MOVING'")
    List<RobotPosition> findMovingRobotsByLineId(@Param("lineId") Long lineId);
    
    // 작업 중인 로봇들 조회
    @Query("SELECT rp FROM RobotPosition rp WHERE rp.lineId = :lineId AND rp.currentAction = 'WORKING'")
    List<RobotPosition> findWorkingRobotsByLineId(@Param("lineId") Long lineId);
    
    // Unity 좌표 범위로 로봇 검색
    @Query("SELECT rp FROM RobotPosition rp WHERE rp.lineId = :lineId " +
           "AND rp.positionX BETWEEN :minX AND :maxX " +
           "AND rp.positionY BETWEEN :minY AND :maxY " +
           "AND rp.isActive = true")
    List<RobotPosition> findByLineIdAndPositionRange(@Param("lineId") Long lineId,
                                                   @Param("minX") Double minX, @Param("maxX") Double maxX,
                                                   @Param("minY") Double minY, @Param("maxY") Double maxY);
    
    // 라인의 평균 배터리 레벨 계산
    @Query("SELECT AVG(rp.batteryLevel) FROM RobotPosition rp WHERE rp.lineId = :lineId AND rp.isActive = true")
    Double calculateAverageBatteryLevelByLineId(@Param("lineId") Long lineId);
}