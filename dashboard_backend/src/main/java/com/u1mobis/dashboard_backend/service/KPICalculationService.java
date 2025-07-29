package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.KPIData;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.CurrentProductionRepository;
import com.u1mobis.dashboard_backend.repository.KPIDataRepository;
import com.u1mobis.dashboard_backend.repository.ProductionCompletedRepository;
import com.u1mobis.dashboard_backend.service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KPICalculationService {
    
    private final KPIDataRepository kpiDataRepository;
    private final ProductionCompletedRepository productionCompletedRepository;
    private final CurrentProductionRepository currentProductionRepository;
    private final CompanyRepository companyRepository;
    private final AlertService alertService;
    
    // 전체 실시간 KPI 조회 (이전 버전 유지)
    public Map<String, Object> getRealTimeKPI() {
        try {
            Optional<KPIData> latestKPI = kpiDataRepository.findTopByOrderByTimestampDesc();
            
            if (latestKPI.isPresent()) {
                KPIData kpi = latestKPI.get();
                return Map.of(
                    "oee", kpi.getCalculatedOEE() != null ? kpi.getCalculatedOEE() * 100 : 0.0,
                    "fty", kpi.getCalculatedFTY() != null ? kpi.getCalculatedFTY() * 100 : 0.0,
                    "otd", kpi.getCalculatedOTD() != null ? kpi.getCalculatedOTD() * 100 : 0.0,
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
    
    // 회사별 실시간 KPI 조회 (새로 추가)
    public Map<String, Object> getRealTimeKPIByCompany(String companyName) {
        try {
            Optional<KPIData> latestKPI = kpiDataRepository.findTopByCompany_CompanyNameOrderByTimestampDesc(companyName);
            
            if (latestKPI.isPresent()) {
                KPIData kpi = latestKPI.get();
                return Map.of(
                    "oee", kpi.getCalculatedOEE() != null ? kpi.getCalculatedOEE() * 100 : 0.0,
                    "fty", kpi.getCalculatedFTY() != null ? kpi.getCalculatedFTY() * 100 : 0.0,
                    "otd", kpi.getCalculatedOTD() != null ? kpi.getCalculatedOTD() * 100 : 0.0,
                    "timestamp", kpi.getTimestamp() != null ? kpi.getTimestamp().toString() : LocalDateTime.now().toString(),
                    "companyName", companyName,
                    "lineId", kpi.getLineId()
                );
            }
            
            // 데이터가 없을 때 기본값 반환
            return Map.of(
                "oee", 0.0,
                "fty", 0.0,
                "otd", 0.0,
                "timestamp", LocalDateTime.now().toString(),
                "companyName", companyName,
                "message", companyName + " 회사의 KPI 데이터가 없습니다."
            );
            
        } catch (Exception e) {
            log.error("{} 회사 KPI 조회 실패: {}", companyName, e.getMessage());
            return Map.of(
                "oee", 0.0,
                "fty", 0.0,
                "otd", 0.0,
                "timestamp", LocalDateTime.now().toString(),
                "companyName", companyName,
                "error", "KPI 조회 중 오류 발생"
            );
        }
    }
    
    // KPI 데이터 처리 및 계산
    public KPIData processKPIData(String companyName, Long lineId, Integer plannedTime, Integer downtime, Double targetCycleTime, 
                                  Integer goodCount, Integer totalCount, Integer firstTimePassCount, 
                                  Integer onTimeDeliveryCount) {
        
        log.info("KPI 데이터 처리 시작 - 회사: {}, 라인: {}, 파라미터: planned={}, downtime={}, target={}, good={}, total={}, ftp={}, otd={}", 
                companyName, lineId, plannedTime, downtime, targetCycleTime, goodCount, totalCount, firstTimePassCount, onTimeDeliveryCount);
        
        try {
        
        // OEE 계산 (표준 공식)
        // 1. Availability (가용성) = 가동시간 / 계획시간
        double availability = (plannedTime - downtime) / (double) plannedTime;
        
        // 2. Performance (성능률) = 목표사이클타임 / 실제사이클타임
        // 실제사이클타임 = 가동시간(초) / 생산수량
        double actualCycleTime = ((plannedTime - downtime) * 60.0) / totalCount;
        double performance = targetCycleTime / actualCycleTime;
        performance = Math.min(performance, 1.0); // 100%를 넘지 않도록 제한
        
        // 3. Quality (품질률) = 양품수량 / 총생산수량
        double quality = goodCount / (double) totalCount;
        
        // 4. OEE = Availability × Performance × Quality
        double oee = availability * performance * quality;
        
        // 디버깅 로그 추가
        log.info("=== OEE 계산 디버깅 ===");
        log.info("입력값 - plannedTime: {}분, downtime: {}분, targetCycleTime: {}초, totalCount: {}개", 
                plannedTime, downtime, targetCycleTime, totalCount);
        log.info("Availability: {} ({}%)", availability, availability * 100);
        log.info("actualCycleTime: {}초", actualCycleTime);
        log.info("Performance (제한 전): {}", targetCycleTime / actualCycleTime);
        log.info("Performance (제한 후): {} ({}%)", performance, performance * 100);
        log.info("Quality: {} ({}%)", quality, quality * 100);
        log.info("최종 OEE: {} ({}%)", oee, oee * 100);
        
        // FTY 계산 (소수점으로 저장)
        double fty = firstTimePassCount / (double) totalCount;
        
        // OTD 계산 (소수점으로 저장)
        double otd = onTimeDeliveryCount / (double) totalCount;
        
        // 회사 정보 조회 (Company 엔티티 필요)
        Company company = getCompanyByName(companyName);
        
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
            .company(company)
            .lineId(lineId)
            .build();
            
        KPIData saved = kpiDataRepository.save(kpiData);
        log.info("KPI 계산 완료 - OEE: {}%, FTY: {}%, OTD: {}%", 
                Math.round(oee * 100) / 100.0, 
                Math.round(fty * 100) / 100.0, 
                Math.round(otd * 100) / 100.0);
        
        // KPI 임계값 체크 및 알림 전송
        checkKPIThresholds(company, saved);
                
        return saved;
        
        } catch (Exception e) {
            log.error("KPI 데이터 처리 중 오류 발생 - 회사: {}, 오류: {}", companyName, e.getMessage());
            log.error("에러 스택 트레이스:", e);
            throw e;
        }
    }
    
    // 회사명으로 Company 엔티티 조회
    private Company getCompanyByName(String companyName) {
        Optional<Company> company = companyRepository.findByCompanyName(companyName);
        if (company.isPresent()) {
            return company.get();
        } else {
            log.warn("회사를 찾을 수 없습니다: {}", companyName);
            throw new RuntimeException("회사를 찾을 수 없습니다: " + companyName);
        }
    }
    
    // KPI 임계값 체크 및 알림 전송
    private void checkKPIThresholds(Company company, KPIData kpiData) {
        try {
            double oee = kpiData.getCalculatedOEE() != null ? kpiData.getCalculatedOEE() : 0.0;
            double fty = kpiData.getCalculatedFTY() != null ? kpiData.getCalculatedFTY() : 0.0;
            double otd = kpiData.getCalculatedOTD() != null ? kpiData.getCalculatedOTD() : 0.0;
            
            // OEE 임계값 체크 (70% 미만)
            if (oee < 0.70) {
                alertService.sendThresholdAlert(company, "OEE_LOW", 
                    String.format("OEE 성능 저하: %.1f%% (기준: 70%%)", oee * 100), 
                    oee * 100);
            }
            
            // FTY 임계값 체크 (85% 미만)
            if (fty < 0.85) {
                alertService.sendThresholdAlert(company, "FTY_LOW", 
                    String.format("FTY 품질 저하: %.1f%% (기준: 85%%)", fty * 100), 
                    fty * 100);
            }
            
            // OTD 임계값 체크 (90% 미만)
            if (otd < 0.90) {
                alertService.sendThresholdAlert(company, "OTD_LOW", 
                    String.format("OTD 납기 지연: %.1f%% (기준: 90%%)", otd * 100), 
                    otd * 100);
            }
            
            // 매우 높은 성능 알림 (OEE 95% 이상)
            if (oee >= 0.95) {
                alertService.sendThresholdAlert(company, "OEE_EXCELLENT", 
                    String.format("우수 OEE 성능: %.1f%% - 축하합니다!", oee * 100), 
                    oee * 100);
            }
            
        } catch (Exception e) {
            log.error("KPI 임계값 체크 중 오류 - 회사: {}, 오류: {}", company.getCompanyName(), e.getMessage());
        }
    }
}
