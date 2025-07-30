package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    
    // 회사별 재고 조회
    List<Stock> findByCompanyId(Long companyId);
    
    // 회사별 재고 조회 (페이징)
    Page<Stock> findByCompanyId(Long companyId, Pageable pageable);
}