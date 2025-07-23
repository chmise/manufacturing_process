package com.u1mobis.dashboard_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.Robot;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
    
    List<Robot> findByCompanyId(Long companyId);
    
    List<Robot> findByStationCode(String stationCode);
    
    List<Robot> findByCompanyIdAndStationCode(Long companyId, String stationCode);
    
    Optional<Robot> findByRobotIdAndCompanyId(Long robotId, Long companyId);
    
    @Query("SELECT r FROM Robot r WHERE r.companyId = :companyId AND r.stationCode LIKE :stationPrefix%")
    List<Robot> findByCompanyIdAndStationPrefix(@Param("companyId") Long companyId, @Param("stationPrefix") String stationPrefix);
}