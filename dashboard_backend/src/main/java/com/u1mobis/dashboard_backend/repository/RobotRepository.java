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

    // 회사별 로봇 조회
    List<Robot> findByCompanyId(Long companyId);

    // 로봇 이름으로 조회
    Optional<Robot> findByRobotName(String robotName);

    // 회사별 로봇 이름 조회
    Optional<Robot> findByCompanyIdAndRobotName(Long companyId, String robotName);

    // 특정 회사의 로봇 개수
    long countByCompanyId(Long companyId);

    // 로봇 ID와 회사 ID로 조회 (보안용)
    @Query("SELECT r FROM Robot r WHERE r.robotId = :robotId AND r.companyId = :companyId")
    Optional<Robot> findByRobotIdAndCompanyId(@Param("robotId") String robotId, 
                                             @Param("companyId") Long companyId);

    // 로봇 상태를 포함한 조회 (향후 확장용)
    @Query("SELECT r FROM Robot r LEFT JOIN FETCH r.company WHERE r.robotId = :robotId")
    Optional<Robot> findRobotWithCompany(@Param("robotId") String robotId);
}