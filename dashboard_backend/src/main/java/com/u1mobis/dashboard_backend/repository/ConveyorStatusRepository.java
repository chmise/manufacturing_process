package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.ConveyorStatus;


@Repository
public interface ConveyorStatusRepository extends JpaRepository<ConveyorStatus, Long> {
    
    // 최신 컨베이어 상태 조회
    Optional<ConveyorStatus> findTopByOrderByTimestampDesc();
    
    // 특정 기간 컨베이어 로그 조회
    List<ConveyorStatus> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    // 특정 명령 기록 조회
    List<ConveyorStatus> findByCommand(String command);
    
    // 오늘 정지 횟수 조회 (수정됨)
    @Query("SELECT COUNT(cs) FROM ConveyorStatus cs WHERE CAST(cs.timestamp AS DATE) = CURRENT_DATE AND cs.command = 'STOP'")
    Long countTodayStopCommands();
    
    // 비상정지 횟수 조회
    @Query("SELECT COUNT(cs) FROM ConveyorStatus cs WHERE cs.command = 'EMERGENCY_STOP' AND cs.timestamp BETWEEN :startTime AND :endTime")
    Long countEmergencyStops(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}