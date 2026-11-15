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

        KpiResult result = calculate(orZero(plannedTime), orZero(downtime), orZero(targetCycleTime),
                orZero(goodCount), orZero(totalCount), orZero(firstTimePassCount), orZero(onTimeDeliveryCount));
        double oee = result.oee();
        double fty = result.fty();
        double otd = result.otd();

        log.debug("OEE 계산 - 가용성: {}, 성능: {}, 품질: {}, OEE: {}",
                result.availability(), result.performance(), result.quality(), oee);

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
    
    /**
     * OEE · FTY · OTD 계산 결과 (모두 0~1 사이 비율)
     */
    public record KpiResult(double availability, double performance, double quality,
                            double oee, double fty, double otd) {
    }

    /**
     * KPI 계산 (표준 OEE 공식). 계획시간이나 생산수량이 0이어도 NaN/Infinity 없이 0을 돌려준다.
     *
     * @param plannedTime       계획 가동시간 (분)
     * @param downtime          비가동시간 (분)
     * @param targetCycleTime   목표 사이클타임 (초)
     */
    public static KpiResult calculate(int plannedTime, int downtime, double targetCycleTime,
                                      int goodCount, int totalCount, int firstTimePassCount,
                                      int onTimeDeliveryCount) {
        int operatingTime = Math.max(plannedTime - downtime, 0);

        // 1. Availability (가용성) = 가동시간 / 계획시간
        double availability = ratio(operatingTime, plannedTime);

        // 2. Performance (성능률) = 목표사이클타임 / 실제사이클타임 (100%를 넘지 않도록 제한)
        //    실제사이클타임 = 가동시간(초) / 생산수량
        double actualCycleTime = ratio(operatingTime * 60.0, totalCount);
        double performance = actualCycleTime > 0 ? Math.min(targetCycleTime / actualCycleTime, 1.0) : 0.0;

        // 3. Quality (품질률) = 양품수량 / 총생산수량
        double quality = ratio(goodCount, totalCount);

        // 4. OEE = Availability × Performance × Quality
        double oee = availability * performance * quality;

        return new KpiResult(availability, performance, quality, oee,
                ratio(firstTimePassCount, totalCount),   // FTY
                ratio(onTimeDeliveryCount, totalCount)); // OTD
    }

    private static double ratio(double numerator, double denominator) {
        return denominator > 0 ? numerator / denominator : 0.0;
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }

    private static double orZero(Double value) {
        return value != null ? value : 0.0;
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
