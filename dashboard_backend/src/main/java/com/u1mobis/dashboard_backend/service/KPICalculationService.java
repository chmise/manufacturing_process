package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.u1mobis.dashboard_backend.entity.KPIData;
import com.u1mobis.dashboard_backend.repository.CurrentProductionRepository;
import com.u1mobis.dashboard_backend.repository.KPIDataRepository;
import com.u1mobis.dashboard_backend.repository.ProductionCompletedRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KPICalculationService {
    
    private final KPIDataRepository kpiDataRepository;
    private final ProductionCompletedRepository productionCompletedRepository;
    private final CurrentProductionRepository currentProductionRepository;
    
    // 실시간 KPI 조회 (수정됨)
    public Map<String, Object> getRealTimeKPI() {
        try {
            Optional<KPIData> latestKPI = kpiDataRepository.findTopByOrderByTimestampDesc();
            
            if (latestKPI.isPresent()) {
                KPIData kpi = latestKPI.get();
                return Map.of(
                    "oee", kpi.getCalculatedOEE() != null ? kpi.getCalculatedOEE() : 0.0,
                    "fty", kpi.getCalculatedFTY() != null ? kpi.getCalculatedFTY() : 0.0,
                    "otd", kpi.getCalculatedOTD() != null ? kpi.getCalculatedOTD() : 0.0,
                    "timestamp", kpi.getTimestamp() != null ? kpi.getTimestamp().toString() : LocalDateTime.now().toString()
                );
            }
            
            // 데이터가 없을 때 기본값 반환
            return Map.of(
                "oee", 0.0,
                "fty", 0.0,
                "otd", 0.0,
                "timestamp", LocalDateTime.now().toString(),
                "message", "아직 KPI 데이터가 없습니다. 먼저 데이터를 입력해주세요."
            );
            
        } catch (Exception e) {
            log.error("KPI 조회 실패: {}", e.getMessage());
            return Map.of(
                "oee", 0.0,
                "fty", 0.0,
                "otd", 0.0,
                "timestamp", LocalDateTime.now().toString(),
                "error", "KPI 조회 중 오류 발생"
            );
        }
    }
    
    // KPI 데이터 처리 및 계산
    public KPIData processKPIData(Integer plannedTime, Integer downtime, Double targetCycleTime, 
                                  Integer goodCount, Integer totalCount, Integer firstTimePassCount, 
                                  Integer onTimeDeliveryCount) {
        
        // OEE 계산
        double availability = (plannedTime - downtime) / (double) plannedTime * 100;
        double performance = (targetCycleTime * totalCount) / ((plannedTime - downtime) * 60) * 100;
        double quality = goodCount / (double) totalCount * 100;
        double oee = (availability * performance * quality) / 10000; // 백분율로 변환
        
        // FTY 계산
        double fty = firstTimePassCount / (double) totalCount * 100;
        
        // OTD 계산
        double otd = onTimeDeliveryCount / (double) totalCount * 100;
        
        // KPI 데이터 저장
        KPIData kpiData = KPIData.builder()
            .timestamp(LocalDateTime.now())
            .plannedTime(plannedTime)
            .downtime(downtime)
            .targetCycleTime(targetCycleTime)
            .goodCount(goodCount)
            .totalCount(totalCount)
            .firstTimePassCount(firstTimePassCount)
            .onTimeDeliveryCount(onTimeDeliveryCount)
            .calculatedOEE(oee)
            .calculatedFTY(fty)
            .calculatedOTD(otd)
            .build();
            
        KPIData saved = kpiDataRepository.save(kpiData);
        log.info("KPI 계산 완료 - OEE: {}%, FTY: {}%, OTD: {}%", 
                Math.round(oee * 100) / 100.0, 
                Math.round(fty * 100) / 100.0, 
                Math.round(otd * 100) / 100.0);
                
        return saved;
    }
}
