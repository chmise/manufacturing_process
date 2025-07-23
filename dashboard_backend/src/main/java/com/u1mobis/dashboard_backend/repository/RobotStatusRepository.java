package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.RobotStatus;

@Repository
public interface RobotStatusRepository extends JpaRepository<RobotStatus, Long> {
    
    List<RobotStatus> findByCompanyId(Long companyId);
    
    List<RobotStatus> findByRobotId(Long robotId);
    
    Optional<RobotStatus> findByRobotIdAndCompanyId(Long robotId, Long companyId);
    
    @Query("SELECT rs FROM RobotStatus rs WHERE rs.companyId = :companyId ORDER BY rs.timestamp DESC")
    List<RobotStatus> findLatestByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT rs FROM RobotStatus rs WHERE rs.robotId = :robotId ORDER BY rs.timestamp DESC LIMIT 1")
    Optional<RobotStatus> findLatestByRobotId(@Param("robotId") Long robotId);
    
    @Query("SELECT rs FROM RobotStatus rs WHERE rs.companyId = :companyId AND rs.timestamp >= :fromTime ORDER BY rs.timestamp DESC")
    List<RobotStatus> findByCompanyIdAndTimestampAfter(@Param("companyId") Long companyId, @Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT rs FROM RobotStatus rs WHERE rs.robotId IN :robotIds ORDER BY rs.timestamp DESC")
    List<RobotStatus> findLatestByRobotIds(@Param("robotIds") List<Long> robotIds);
}