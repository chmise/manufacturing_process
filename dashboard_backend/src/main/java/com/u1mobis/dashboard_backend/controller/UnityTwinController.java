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
    private final RobotRepository robotRepository;

    /**
     * Unity 트윈을 위한 전체 실시간 데이터
     */
    @GetMapping("/realtime-data")
    public ResponseEntity<Map<String, Object>> getUnityRealtimeData() {
        Map<String, Object> unityData = new HashMap<>();

        try {
            // 생산 현황 데이터
            Map<String, Object> productionStatus = productionService.getCurrentProductionStatus();
            @SuppressWarnings("unchecked")
            List<CurrentProduction> processingProducts = (List<CurrentProduction>) productionStatus.get("processing_products");
            
            // Unity용 제품 위치 데이터 변환 (실제 DB 스키마 기반)
            Map<String, Object> products = new HashMap<>();
            for (CurrentProduction product : processingProducts) {
                String unityCarId = convertToUnityCarId(product.getProductId());
                products.put(unityCarId, Map.of(
                    "productId", product.getProductId(),
                    "currentStation", product.getCurrentStation(), // DB에 실제 존재하는 필드
                    "status", product.getStatus(),
                    "reworkCount", product.getReworkCount() != null ? product.getReworkCount() : 0,
                    "productColor", product.getProductColor() != null ? product.getProductColor() : "WHITE", // Unity에서 차량 색상 표시
                    "lineId", product.getLineId(), // 라인 정보
                    "position", getProductPosition(product.getCurrentStation()),
                    "startTime", product.getStartTime(),
                    "dueDate", product.getDueDate()
                ));
            }

            // 공정별 상태 데이터
            Map<String, Object> stations = Map.of(
                "DoorStation", Map.of(
                    "status", "OPERATING",
                    "currentProduct", getCurrentProductAtStation("DOOR"),
                    "efficiency", 85.2
                ),
                "WaterLeakTestStation", Map.of(
                    "status", "OPERATING", 
                    "currentProduct", getCurrentProductAtStation("WATER_LEAK"),
                    "efficiency", 92.1
                )
            );

            // 실제 데이터베이스에서 로봇 상태 데이터 조회
            Map<String, Object> robots = new HashMap<>();
            try {
                var allRobots = robotRepository.findAll();
                for (var robot : allRobots) {
                    robots.put(robot.getRobotId(), Map.of(
                        "robotId", robot.getRobotId(),
                        "robotName", robot.getRobotName(),
                        "companyId", robot.getCompanyId(),
                        "lineId", robot.getLineId(),
                        "status", "ACTIVE", // 실제로는 별도 상태 테이블에서 조회
                        "currentTask", determineCurrentTask(robot.getRobotId()),
                        "batteryLevel", 95 // 실제로는 IoT 센서 데이터에서 조회
                    ));
                }
            } catch (Exception e) {
                log.warn("로봇 데이터 조회 실패, 기본값 사용", e);
                // 기본값으로 폴백
                robots = Map.of(
                    "ROBOT_001", Map.of(
                        "status", "ACTIVE",
                        "currentTask", "DOOR_ASSEMBLY",
                        "batteryLevel", 87
                    )
                );
            }

            // KPI 데이터
            Map<String, Object> kpiData = kpiCalculationService.getRealTimeKPI();

            unityData.put("products", products);
            unityData.put("stations", stations);
            unityData.put("robots", robots);
            unityData.put("kpi", kpiData);
            unityData.put("timestamp", System.currentTimeMillis());

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
                "position", getProductPosition(currentStation)
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
}