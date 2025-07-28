package com.u1mobis.dashboard_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.u1mobis.dashboard_backend.entity.CurrentProduction;
import com.u1mobis.dashboard_backend.service.ProductionService;
import com.u1mobis.dashboard_backend.service.KPICalculationService;
import com.u1mobis.dashboard_backend.service.EnvironmentService;
import com.u1mobis.dashboard_backend.service.UnityRealtimeDataService;
import com.u1mobis.dashboard_backend.repository.RobotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/unity")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class UnityTwinController {

    private final ProductionService productionService;
    private final KPICalculationService kpiCalculationService;
    private final EnvironmentService environmentService;
    private final UnityRealtimeDataService unityRealtimeDataService;
    private final RobotRepository robotRepository;

    /**
     * Unity 트윈을 위한 전체 실시간 데이터 (시뮬레이터 연동)
     */
    @GetMapping("/realtime-data")
    public ResponseEntity<Map<String, Object>> getUnityRealtimeData() {
        try {
            // 새로운 UnityRealtimeDataService 사용
            Map<String, Object> unityData = unityRealtimeDataService.getRealtimeDataForUnity();
            
            return ResponseEntity.ok(unityData);

        } catch (Exception e) {
            log.error("Unity 실시간 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "데이터 조회 실패",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 특정 제품의 현재 위치 조회
     */
    @GetMapping("/product/{productId}/position")
    public ResponseEntity<Map<String, Object>> getProductPosition(@PathVariable String productId) {
        try {
            // 데이터베이스에서 제품 정보 조회 로직
            String unityCarId = convertToUnityCarId(productId);
            String currentStation = "DoorStation"; // 실제로는 DB에서 조회
            
            return ResponseEntity.ok(Map.of(
                "unityCarId", unityCarId,
                "productId", productId,
                "currentStation", currentStation,
                "position", getStationPosition(currentStation)
            ));
        } catch (Exception e) {
            log.error("제품 위치 조회 실패: {}", productId, e);
            return ResponseEntity.status(500).body(Map.of("error", "제품 위치 조회 실패"));
        }
    }

    /**
     * 공정별 현재 작업 중인 제품 조회
     */
    @GetMapping("/station/{stationCode}/current-product") 
    public ResponseEntity<Map<String, Object>> getStationCurrentProduct(@PathVariable String stationCode) {
        try {
            String currentProduct = getCurrentProductAtStation(stationCode);
            
            return ResponseEntity.ok(Map.of(
                "stationCode", stationCode,
                "currentProduct", currentProduct != null ? currentProduct : "NONE",
                "status", currentProduct != null ? "PROCESSING" : "IDLE"
            ));
        } catch (Exception e) {
            log.error("공정 현재 제품 조회 실패: {}", stationCode, e);
            return ResponseEntity.status(500).body(Map.of("error", "공정 상태 조회 실패"));
        }
    }

    // Unity 유틸리티 메서드들
    private String convertToUnityCarId(String productId) {
        // "A01_PROD_001" -> "CAR_001" 형태로 변환
        if (productId.contains("PROD_")) {
            String number = productId.substring(productId.lastIndexOf("_") + 1);
            return "CAR_" + number;
        }
        return "CAR_001";
    }


    private String getCurrentProductAtStation(String stationCode) {
        try {
            // 실제 데이터베이스에서 현재 해당 공정에서 작업 중인 제품 조회
            Map<String, Object> productionStatus = productionService.getCurrentProductionStatus();
            @SuppressWarnings("unchecked")
            List<CurrentProduction> processingProducts = (List<CurrentProduction>) productionStatus.get("processing_products");
            
            for (CurrentProduction product : processingProducts) {
                String currentStation = product.getCurrentStation();
                if (matchesStation(currentStation, stationCode)) {
                    return convertToUnityCarId(product.getProductId());
                }
            }
            return null;
        } catch (Exception e) {
            log.error("공정별 현재 제품 조회 실패: {}", stationCode, e);
            return null;
        }
    }

    private boolean matchesStation(String currentStation, String stationCode) {
        if (currentStation == null) return false;
        return (stationCode.equals("DOOR") && currentStation.contains("DOOR")) ||
               (stationCode.equals("WATER_LEAK") && currentStation.contains("WATER")) ||
               (stationCode.equals("STATION_01") && currentStation.contains("01")) ||
               (stationCode.equals("STATION_02") && currentStation.contains("02"));
    }

    private String determineCurrentTask(String robotId) {
        // 실제로는 현재 해당 로봇이 수행 중인 작업을 DB에서 조회
        if (robotId.contains("001")) {
            return "DOOR_ASSEMBLY";
        } else if (robotId.contains("002")) {
            return "WATER_LEAK_TEST";
        }
        return "IDLE";
    }
    
    private Map<String, Object> getStationPosition(String station) {
        // Unity 좌표계에 맞는 기본 위치 반환
        switch (station) {
            case "DoorStation":
                return Map.of("x", -15.0, "y", 0.5, "z", 0.0);
            case "WaterLeakTestStation":
                return Map.of("x", 15.0, "y", 0.5, "z", 0.0);
            default:
                return Map.of("x", 0.0, "y", 0.5, "z", 0.0);
        }
    }
}