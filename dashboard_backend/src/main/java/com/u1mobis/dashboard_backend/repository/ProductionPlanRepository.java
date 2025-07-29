package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
    
    // 라인별 생산 계획 조회
    List<ProductionPlan> findByLineId(Long lineId);
    
    // 라인별 + 날짜별 생산 계획 조회
    Optional<ProductionPlan> findByLineIdAndPlanDate(Long lineId, LocalDate planDate);
    
    // 라인별 + 상태별 생산 계획 조회
    List<ProductionPlan> findByLineIdAndStatus(Long lineId, String status);
    
    // 특정 날짜의 모든 생산 계획 조회
    List<ProductionPlan> findByPlanDate(LocalDate planDate);
    
    // 회사의 모든 라인 생산 계획 조회
    @Query("SELECT pp FROM ProductionPlan pp JOIN pp.productionLine pl WHERE pl.company.companyId = :companyId")
    List<ProductionPlan> findByCompanyId(@Param("companyId") Long companyId);
    
    // 회사의 특정 날짜 생산 계획 조회
    @Query("SELECT pp FROM ProductionPlan pp JOIN pp.productionLine pl " +
           "WHERE pl.company.companyId = :companyId AND pp.planDate = :planDate")
    List<ProductionPlan> findByCompanyIdAndPlanDate(@Param("companyId") Long companyId, @Param("planDate") LocalDate planDate);
    
    // 진행 중인 생산 계획들 조회
    @Query("SELECT pp FROM ProductionPlan pp WHERE pp.lineId = :lineId AND pp.status = 'IN_PROGRESS'")
    List<ProductionPlan> findInProgressPlansByLineId(@Param("lineId") Long lineId);
    
    // 특정 기간의 생산 계획 조회
    @Query("SELECT pp FROM ProductionPlan pp WHERE pp.lineId = :lineId " +
           "AND pp.planDate BETWEEN :startDate AND :endDate")
    List<ProductionPlan> findByLineIdAndDateRange(@Param("lineId") Long lineId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
    
    // 목표 달성률이 특정 값 이상인 계획들 조회
    @Query("SELECT pp FROM ProductionPlan pp WHERE pp.lineId = :lineId " +
           "AND (pp.currentProgress * 100.0 / pp.dailyTarget) >= :minAchievementRate")
    List<ProductionPlan> findHighAchievementPlans(@Param("lineId") Long lineId, 
                                                @Param("minAchievementRate") Double minAchievementRate);
    
    // 라인의 오늘 생산 계획 조회
    @Query("SELECT pp FROM ProductionPlan pp WHERE pp.lineId = :lineId AND pp.planDate = :today")
    Optional<ProductionPlan> findTodayPlanByLineId(@Param("lineId") Long lineId, @Param("today") LocalDate today);
    
    // 라인별 월간 생산 목표 합계
    @Query("SELECT SUM(pp.dailyTarget) FROM ProductionPlan pp WHERE pp.lineId = :lineId " +
           "AND YEAR(pp.planDate) = :year AND MONTH(pp.planDate) = :month")
    Integer calculateMonthlyTargetByLineId(@Param("lineId") Long lineId, @Param("year") int year, @Param("month") int month);
    
    // 라인별 월간 실제 생산량 합계
    @Query("SELECT SUM(pp.currentProgress) FROM ProductionPlan pp WHERE pp.lineId = :lineId " +
           "AND YEAR(pp.planDate) = :year AND MONTH(pp.planDate) = :month")
    Integer calculateMonthlyProgressByLineId(@Param("lineId") Long lineId, @Param("year") int year, @Param("month") int month);
}