package com.u1mobis.dashboard_backend.domain.manufacturing.service;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;
import com.u1mobis.dashboard_backend.domain.manufacturing.repository.ProductionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class ProductionDomainService {
    private final ProductionRepository productionRepository;
    private final Random random;
    
    public ProductionDomainService(ProductionRepository productionRepository) {
        this.productionRepository = productionRepository;
        this.random = new Random();
    }
    
    public Production createNewProduction(CompanyId companyId, ProductionLineId lineId, 
                                        ProductName productName, ProductColor color) {
        // 고유한 생산 주문 ID 생성
        ProductionOrderId productionId = generateProductionOrderId(companyId);
        
        // 납기일 설정 (현재시간 + 8시간)
        LocalDateTime dueDate = LocalDateTime.now().plusHours(8);
        
        Production production = new Production(productionId, companyId, lineId, 
                                             productName, color, dueDate);
        
        return production;
    }
    
    public boolean canStartProduction(ProductionLineId lineId) {
        List<Production> activeProductions = productionRepository.findActiveProductionsByLineId(lineId);
        
        // 라인당 최대 3개의 동시 생산 허용
        return activeProductions.size() < 3;
    }
    
    public void validateProductionCapacity(CompanyId companyId) {
        long activeCount = productionRepository.countActiveProductionsByCompanyId(companyId);
        
        // 회사당 최대 20개의 동시 생산 허용
        if (activeCount >= 20) {
            throw new IllegalStateException("Maximum production capacity reached for company: " + companyId);
        }
    }
    
    public List<Production> getDelayedProductions(CompanyId companyId) {
        return productionRepository.findDelayedProductions(companyId);
    }
    
    public ProductionPerformanceMetrics calculatePerformanceMetrics(CompanyId companyId) {
        long totalCompleted = productionRepository.countCompletedProductionsByCompanyIdToday(companyId);
        long totalActive = productionRepository.countActiveProductionsByCompanyId(companyId);
        double averageCycleTime = productionRepository.getAverageCycleTimeByCompanyId(companyId);
        
        List<Production> delayedProductions = getDelayedProductions(companyId);
        double onTimeDeliveryRate = calculateOnTimeDeliveryRate(companyId);
        
        return new ProductionPerformanceMetrics(
            totalCompleted, 
            totalActive, 
            averageCycleTime, 
            delayedProductions.size(),
            onTimeDeliveryRate
        );
    }
    
    private double calculateOnTimeDeliveryRate(CompanyId companyId) {
        List<Production> completedProductions = productionRepository.findCompletedProductionsByCompanyId(companyId);
        
        if (completedProductions.isEmpty()) {
            return 100.0;
        }
        
        long onTimeCount = completedProductions.stream()
                .mapToLong(production -> production.isOnTime() ? 1 : 0)
                .sum();
        
        return (double) onTimeCount / completedProductions.size() * 100.0;
    }
    
    private ProductionOrderId generateProductionOrderId(CompanyId companyId) {
        // 회사ID + 타임스탬프 + 랜덤으로 고유 ID 생성
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = String.format("%04d", random.nextInt(10000));
        String productionId = "PROD_" + companyId.getValue() + "_" + timestamp + "_" + randomPart;
        
        return new ProductionOrderId(productionId);
    }
    
    public static class ProductionPerformanceMetrics {
        private final long completedToday;
        private final long activeCount;
        private final double averageCycleTime;
        private final long delayedCount;
        private final double onTimeDeliveryRate;
        
        public ProductionPerformanceMetrics(long completedToday, long activeCount, 
                                          double averageCycleTime, long delayedCount, 
                                          double onTimeDeliveryRate) {
            this.completedToday = completedToday;
            this.activeCount = activeCount;
            this.averageCycleTime = averageCycleTime;
            this.delayedCount = delayedCount;
            this.onTimeDeliveryRate = onTimeDeliveryRate;
        }
        
        public long getCompletedToday() {
            return completedToday;
        }
        
        public long getActiveCount() {
            return activeCount;
        }
        
        public double getAverageCycleTime() {
            return averageCycleTime;
        }
        
        public long getDelayedCount() {
            return delayedCount;
        }
        
        public double getOnTimeDeliveryRate() {
            return onTimeDeliveryRate;
        }
    }
}