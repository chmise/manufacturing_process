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
    
    List<Robot> findByStatusText(String statusText);
    
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.companyId = :companyId AND r.statusText = '작동중'")
    Long countActiveRobotsByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(r) FROM Robot r WHERE r.statusText = '작동중'")
    Long countActiveRobots();
    
    // ===== 성능 최적화된 회사별 조회 메서드들 =====
    
    /**
     * 회사별 로봇 목록 조회 (Company 정보와 함께 조회 - N+1 문제 방지)
     */
    @Query("SELECT r FROM Robot r LEFT JOIN FETCH r.company WHERE r.companyId = :companyId")
    List<Robot> findByCompanyIdWithCompany(@Param("companyId") Long companyId);
    
    /**
     * 회사별 로봇 목록 조회 (ProductionLine 정보와 함께 조회)
     */
    @Query("SELECT r FROM Robot r LEFT JOIN FETCH r.productionLine WHERE r.companyId = :companyId")
    List<Robot> findByCompanyIdWithProductionLine(@Param("companyId") Long companyId);
    
    /**
     * 회사별 활성 로봇 목록 조회
     */
    @Query("SELECT r FROM Robot r WHERE r.companyId = :companyId AND r.statusText IN ('작동중', 'RUNNING')")
    List<Robot> findActiveRobotsByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * 회사별 로봇 상태별 통계
     */
    @Query("SELECT r.statusText, COUNT(r) FROM Robot r WHERE r.companyId = :companyId GROUP BY r.statusText")
    List<Object[]> getRobotStatusStatsByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * 온도 기준으로 문제 로봇 조회
     */
    @Query("SELECT r FROM Robot r WHERE r.companyId = :companyId AND r.temperature > :maxTemperature")
    List<Robot> findRobotsWithHighTemperature(@Param("companyId") Long companyId, @Param("maxTemperature") Double maxTemperature);
}