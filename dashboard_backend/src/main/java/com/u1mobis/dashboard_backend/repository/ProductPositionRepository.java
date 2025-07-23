package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.ProductPosition;

@Repository
public interface ProductPositionRepository extends JpaRepository<ProductPosition, Long> {
    
    List<ProductPosition> findByCompanyId(Long companyId);
    
    List<ProductPosition> findByStationCode(String stationCode);
    
    List<ProductPosition> findByCompanyIdAndStationCode(Long companyId, String stationCode);
    
    Optional<ProductPosition> findByProductIdAndCompanyId(String productId, Long companyId);
    
    @Query("SELECT pp FROM ProductPosition pp WHERE pp.companyId = :companyId ORDER BY pp.timestamp DESC")
    List<ProductPosition> findLatestByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT pp FROM ProductPosition pp WHERE pp.productId = :productId ORDER BY pp.timestamp DESC LIMIT 1")
    Optional<ProductPosition> findLatestByProductId(@Param("productId") String productId);
    
    @Query("SELECT pp FROM ProductPosition pp WHERE pp.companyId = :companyId AND pp.timestamp >= :fromTime ORDER BY pp.timestamp DESC")
    List<ProductPosition> findByCompanyIdAndTimestampAfter(@Param("companyId") Long companyId, @Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT pp FROM ProductPosition pp WHERE pp.stationCode LIKE :stationPrefix% AND pp.companyId = :companyId")
    List<ProductPosition> findByCompanyIdAndStationPrefix(@Param("companyId") Long companyId, @Param("stationPrefix") String stationPrefix);
}