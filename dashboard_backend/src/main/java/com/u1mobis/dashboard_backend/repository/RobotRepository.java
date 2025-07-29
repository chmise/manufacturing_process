package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends JpaRepository<Robot, String> {

    // ===== 기존 메서드들 =====
    
    List<Robot> findByCompanyId(Long companyId);
    Optional<Robot> findByRobotName(String robotName);
    Optional<Robot> findByCompanyIdAndRobotName(Long companyId, String robotName);
    long countByCompanyId(Long companyId);

    @Query("SELECT r FROM Robot r WHERE r.robotId = :robotId AND r.companyId = :companyId")
    Optional<Robot> findByRobotIdAndCompanyId(@Param("robotId") String robotId, 
                                             @Param("companyId") Long companyId);

    @Query("SELECT r FROM Robot r LEFT JOIN FETCH r.company WHERE r.robotId = :robotId")
    Optional<Robot> findRobotWithCompany(@Param("robotId") String robotId);

    // ===== Digital Twin용 추가 메서드들 =====
    
    List<Robot> findByStationCode(String stationCode);
    List<Robot> findByStatusText(String statusText);
    
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.companyId = :companyId AND r.statusText = '작동중'")
    Long countActiveRobotsByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.statusText = '작동중'")
    Long countActiveRobots();
}