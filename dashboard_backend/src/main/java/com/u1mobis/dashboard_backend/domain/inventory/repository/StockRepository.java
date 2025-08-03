package com.u1mobis.dashboard_backend.domain.inventory.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialType;
import com.u1mobis.dashboard_backend.domain.inventory.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findById(MaterialId id);
    List<Stock> findByCompanyId(CompanyId companyId);
    List<Stock> findByCompanyIdAndType(CompanyId companyId, MaterialType type);
    List<Stock> findLowStockItems(CompanyId companyId);
    List<Stock> findOutOfStockItems(CompanyId companyId);
    List<Stock> findByLocation(CompanyId companyId, String location);
    List<Stock> findBySupplier(CompanyId companyId, String supplier);
    void delete(Stock stock);
}