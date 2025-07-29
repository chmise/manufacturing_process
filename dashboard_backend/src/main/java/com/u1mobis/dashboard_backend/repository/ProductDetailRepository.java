package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, String> {
    
    // 라인별 제품 상세 정보 조회
    List<ProductDetail> findByLineId(Long lineId);
    
    // 라인별 + 진행률 기준 조회
    List<ProductDetail> findByLineIdAndWorkProgressBetween(Long lineId, Integer minProgress, Integer maxProgress);
    
    // 특정 문 색상의 제품들 조회
    List<ProductDetail> findByDoorColorAndLineId(String doorColor, Long lineId);
    
    // 회사의 모든 라인에서 제품 상세 정보 조회
    @Query("SELECT pd FROM ProductDetail pd JOIN pd.productionLine pl WHERE pl.company.companyId = :companyId")
    List<ProductDetail> findByCompanyId(@Param("companyId") Long companyId);
    
    // 진행 중인 제품들 조회 (진행률 0-99%)
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.lineId = :lineId AND pd.workProgress < 100")
    List<ProductDetail> findInProgressByLineId(@Param("lineId") Long lineId);
    
    // 완료된 제품들 조회 (진행률 100%)
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.lineId = :lineId AND pd.workProgress = 100")
    List<ProductDetail> findCompletedByLineId(@Param("lineId") Long lineId);
    
    // Unity 좌표 범위로 제품 검색
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.lineId = :lineId " +
           "AND pd.positionX BETWEEN :minX AND :maxX " +
           "AND pd.positionY BETWEEN :minY AND :maxY")
    List<ProductDetail> findByLineIdAndPositionRange(@Param("lineId") Long lineId,
                                                   @Param("minX") Double minX, @Param("maxX") Double maxX,
                                                   @Param("minY") Double minY, @Param("maxY") Double maxY);
}