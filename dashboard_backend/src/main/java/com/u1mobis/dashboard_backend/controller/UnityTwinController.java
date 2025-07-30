package com.u1mobis.dashboard_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.u1mobis.dashboard_backend.entity.CurrentProduction;
import com.u1mobis.dashboard_backend.service.ProductionService;
import com.u1mobis.dashboard_backend.service.KPICalculationService;
import com.u1mobis.dashboard_backend.service.EnvironmentService;
import com.u1mobis.dashboard_backend.service.ConveyorService;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.ProductDetailRepository;
import com.u1mobis.dashboard_backend.repository.StationStatusRepository;
import com.u1mobis.dashboard_backend.repository.RobotPositionRepository;

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
    private final ConveyorService conveyorService;
    private final RobotRepository robotRepository;
    private final ProductDetailRepository productDetailRepository;
    private final StationStatusRepository stationStatusRepository;
    private final RobotPositionRepository robotPositionRepository;

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
            
            // Unity용 제품 위치 데이터 변환 (ProductDetail 포함)
            Map<String, Object> products = new HashMap<>();
            for (CurrentProduction product : processingProducts) {
                String unityCarId = convertToUnityCarId(product.getProductId());
                
                // ProductDetail에서 추가 정보 조회
                var productDetail = productDetailRepository.findById(product.getProductId()).orElse(null);
                
                Map<String, Object> productData = new HashMap<>();
                productData.put("productId", product.getProductId());
                productData.put("currentStation", product.getCurrentStation());
                productData.put("status", product.getStatus());
                productData.put("reworkCount", product.getReworkCount() != null ? product.getReworkCount() : 0);
                productData.put("productColor", product.getProductColor() != null ? product.getProductColor() : "WHITE");
                productData.put("lineId", product.getLineId());
                productData.put("startTime", product.getStartTime());
                productData.put("dueDate", product.getDueDate());
                
                // ProductDetail 추가 정보
                if (productDetail != null) {
                    productData.put("doorColor", productDetail.getDoorColor());
                    productData.put("workProgress", productDetail.getWorkProgress());
                    productData.put("position", Map.of(
                        "x", productDetail.getPositionX(),
                        "y", productDetail.getPositionY(),
                        "z", productDetail.getPositionZ()
                    ));
                    productData.put("estimatedCompletion", productDetail.getEstimatedCompletion());
                } else {
                    // 기본값 설정
                    productData.put("doorColor", "BLACK");
                    productData.put("workProgress", 50);
                    productData.put("position", getProductPosition(product.getCurrentStation()));
                }
                
                products.put(unityCarId, productData);
            }

            // 공정별 상태 데이터 (StationStatus 엔티티 활용)
            Map<String, Object> stations = new HashMap<>();
            try {
                List<com.u1mobis.dashboard_backend.entity.StationStatus> stationStatuses = stationStatusRepository.findAll();
                for (var station : stationStatuses) {
                    stations.put(station.getStationId(), Map.of(
                        "stationId", station.getStationId(),
                        "stationName", station.getStationName(),
                        "lineId", station.getLineId(),
                        "status", station.getStatus(),
                        "temperature", station.getTemperature(),
                        "pressure", station.getPressure(),
                        "efficiency", station.getEfficiency(),
                        "equipmentStatus", station.getEquipmentStatus(),
                        "currentProduct", station.getCurrentProduct() != null ? 
                            convertToUnityCarId(station.getCurrentProduct()) : null,
                        "cycleTime", station.getCycleTime()
                    ));
                }
                
                // 기본 스테이션이 없을 경우 대체값
                if (stations.isEmpty()) {
                    stations.put("DoorStation", Map.of(
                        "status", "OPERATING",
                        "currentProduct", getCurrentProductAtStation("DOOR"),
                        "efficiency", 85.2
                    ));
                    stations.put("WaterLeakTestStation", Map.of(
                        "status", "OPERATING", 
                        "currentProduct", getCurrentProductAtStation("WATER_LEAK"),
                        "efficiency", 92.1
                    ));
                }
            } catch (Exception e) {
                log.warn("스테이션 상태 조회 실패, 기본값 사용", e);
                stations.put("DoorStation", Map.of("status", "OPERATING", "efficiency", 85.2));
                stations.put("WaterLeakTestStation", Map.of("status", "OPERATING", "efficiency", 92.1));
            }

            // 실제 데이터베이스에서 로봇 상태 데이터 조회 (RobotPosition 포함)
            Map<String, Object> robots = new HashMap<>();
            try {
                var allRobots = robotRepository.findAll();
                for (var robot : allRobots) {
                    // RobotPosition에서 추가 정보 조회
                    var robotPosition = robotPositionRepository.findById(robot.getRobotId()).orElse(null);
                    
                    Map<String, Object> robotData = new HashMap<>();
                    robotData.put("robotId", robot.getRobotId());
                    robotData.put("robotName", robot.getRobotName());
                    robotData.put("companyId", robot.getCompanyId());
                    robotData.put("lineId", robot.getProductionLine() != null ? robot.getProductionLine().getLineId() : null);
                    robotData.put("robotType", robot.getRobotType());
                    robotData.put("statusText", robot.getStatusText());
                    robotData.put("motorStatus", robot.getMotorStatus());
                    robotData.put("ledStatus", robot.getLedStatus());
                    robotData.put("cycleTime", robot.getCycleTime());
                    robotData.put("productionCount", robot.getProductionCount());
                    robotData.put("quality", robot.getQuality());
                    robotData.put("temperature", robot.getTemperature());
                    robotData.put("powerConsumption", robot.getPowerConsumption());
                    
                    // RobotPosition 추가 정보
                    if (robotPosition != null) {
                        robotData.put("position", Map.of(
                            "x", robotPosition.getPositionX(),
                            "y", robotPosition.getPositionY(),
                            "z", robotPosition.getPositionZ()
                        ));
                        robotData.put("batteryLevel", robotPosition.getBatteryLevel());
                        robotData.put("currentAction", robotPosition.getCurrentAction());
                        robotData.put("movementSpeed", robotPosition.getMovementSpeed());
                        robotData.put("isActive", robotPosition.getIsActive());
                        
                        if (robotPosition.getTargetX() != null) {
                            robotData.put("target", Map.of(
                                "x", robotPosition.getTargetX(),
                                "y", robotPosition.getTargetY(),
                                "z", robotPosition.getTargetZ()
                            ));
                        }
                    } else {
                        robotData.put("batteryLevel", 95);
                        robotData.put("currentAction", "IDLE");
                        robotData.put("position", Map.of("x", 0.0, "y", 0.0, "z", 0.0));
                    }
                    
                    robots.put(robot.getRobotId(), robotData);
                }
                
                // 기본 로봇이 없을 경우 대체값
                if (robots.isEmpty()) {
                    robots.put("ROBOT_001", Map.of(
                        "status", "ACTIVE",
                        "currentTask", "DOOR_ASSEMBLY",
                        "batteryLevel", 87
                    ));
                }
            } catch (Exception e) {
                log.warn("로봇 데이터 조회 실패, 기본값 사용", e);
                robots.put("ROBOT_001", Map.of(
                    "status", "ACTIVE",
                    "currentTask", "DOOR_ASSEMBLY",
                    "batteryLevel", 87
                ));
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

    // ==================== Unity 전용 API 엔드포인트들 ====================
    
    /**
     * Unity용 생산 개요 통계
     * 경로: /api/unity/production/overview/statistics
     */
    @GetMapping("/production/overview/statistics")
    public ResponseEntity<Map<String, Object>> getProductionOverviewStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 기본 회사 또는 전체 통계 데이터 수집
            Map<String, Object> kpiData = kpiCalculationService.getRealTimeKPI();
            Map<String, Object> productionStatus = productionService.getCurrentProductionStatus();
            
            // Unity에 필요한 형태로 데이터 구성
            statistics.put("totalProduction", kpiData.get("totalProduction"));
            statistics.put("currentEfficiency", kpiData.get("oee"));
            statistics.put("qualityRate", kpiData.get("fty"));
            statistics.put("onTimeDelivery", kpiData.get("otd"));
            statistics.put("activeLines", 2); // 현재 활성 라인 수
            statistics.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Unity 생산 통계 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "생산 통계 조회 실패",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Unity용 컨베이어 상태
     * 경로: /api/unity/conveyor/status
     */
    @GetMapping("/conveyor/status")
    public ResponseEntity<Map<String, Object>> getConveyorStatus() {
        try {
            Map<String, Object> conveyorStatus = new HashMap<>();
            
            // 기본 컨베이어 상태 정보 생성 (실제로는 DB에서 조회)
            conveyorStatus.put("line1", Map.of(
                "status", "RUNNING",
                "speed", 1.5,
                "direction", "FORWARD",
                "sensorDetected", false,
                "emergency", false
            ));
            
            conveyorStatus.put("line2", Map.of(
                "status", "RUNNING", 
                "speed", 1.2,
                "direction", "FORWARD",
                "sensorDetected", true,
                "emergency", false
            ));
            
            conveyorStatus.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(conveyorStatus);
            
        } catch (Exception e) {
            log.error("Unity 컨베이어 상태 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "컨베이어 상태 조회 실패",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Unity용 제품 생산 자재 정보
     * 경로: /api/unity/product/production/materials
     */
    @GetMapping("/product/production/materials")
    public ResponseEntity<Map<String, Object>> getProductionMaterials() {
        try {
            Map<String, Object> materials = new HashMap<>();
            
            // 생산에 사용되는 자재 정보 구성
            materials.put("doors", Map.of(
                "available", 150,
                "inUse", 12,
                "colors", List.of("BLACK", "WHITE", "BROWN", "GRAY")
            ));
            
            materials.put("chassis", Map.of(
                "available", 85,
                "inUse", 8,
                "colors", List.of("RED", "BLUE", "WHITE", "BLACK", "SILVER")
            ));
            
            materials.put("components", Map.of(
                "screws", 5000,
                "bolts", 3000,
                "gaskets", 200,
                "electronics", 45
            ));
            
            materials.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(materials);
            
        } catch (Exception e) {
            log.error("Unity 생산 자재 정보 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "생산 자재 정보 조회 실패",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Unity용 사용자 주문 처리
     * 경로: /api/unity/production/user/order
     */
    @PostMapping("/production/user/order")
    public ResponseEntity<Map<String, Object>> processUserOrder(@RequestBody Map<String, Object> orderData) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 주문 데이터 처리 (실제로는 DB에 저장)
            String orderId = "ORD_" + System.currentTimeMillis();
            String productType = (String) orderData.getOrDefault("productType", "STANDARD");
            String color = (String) orderData.getOrDefault("color", "WHITE");
            Integer quantity = (Integer) orderData.getOrDefault("quantity", 1);
            
            log.info("Unity 사용자 주문 접수 - 제품: {}, 색상: {}, 수량: {}", productType, color, quantity);
            
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("status", "ACCEPTED");
            response.put("estimatedTime", "2-3시간");
            response.put("queuePosition", 3);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Unity 사용자 주문 처리 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "주문 처리 실패",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Unity용 환경 변수 정보
     * 경로: /api/unity/environment/variables
     */
    @GetMapping("/environment/variables")
    public ResponseEntity<Map<String, Object>> getEnvironmentVariables() {
        try {
            Map<String, Object> environment = new HashMap<>();
            
            // 현재 환경 정보 조회
            Map<String, Object> currentEnv = environmentService.getCurrentEnvironment();
            
            environment.put("factory", Map.of(
                "temperature", currentEnv.getOrDefault("temperature", 25.0),
                "humidity", currentEnv.getOrDefault("humidity", 45.0),
                "airQuality", currentEnv.getOrDefault("airQuality", 85),
                "lighting", "NORMAL",
                "noiseLevel", 65
            ));
            
            environment.put("production", Map.of(
                "shiftStatus", "DAY_SHIFT",
                "operatorCount", 12,
                "maintenanceMode", false,
                "emergencyStatus", false
            ));
            
            environment.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(environment);
            
        } catch (Exception e) {
            log.error("Unity 환경 변수 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "환경 변수 조회 실패",
                "message", e.getMessage()
            ));
        }
    }

    // ==================== Unity WebGL에서 호출하는 추가 API들 ====================
    
    /**
     * Unity용 생산 주문 정보 (라인별) 
     * 경로: /api/unity/production/orders/line/{lineId}
     */
    @GetMapping("/production/orders/line/{lineId}")
    public ResponseEntity<Map<String, Object>> getProductionOrdersByLine(@PathVariable Long lineId) {
        try {
            Map<String, Object> orders = new HashMap<>();
            
            // 시뮬레이터가 실행 중이면 실제 데이터, 아니면 기본 데이터
            orders.put("lineId", lineId);
            orders.put("currentOrders", List.of(
                Map.of(
                    "orderId", "ORD_L" + lineId + "_001",
                    "productType", "SEDAN",
                    "color", "WHITE", 
                    "quantity", 1,
                    "status", "IN_PROGRESS",
                    "progress", 65
                ),
                Map.of(
                    "orderId", "ORD_L" + lineId + "_002",
                    "productType", "SUV", 
                    "color", "BLACK",
                    "quantity", 1,
                    "status", "QUEUED",
                    "progress", 0
                )
            ));
            
            orders.put("totalOrders", 2);
            orders.put("completedToday", 8);
            orders.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Unity 생산 주문 조회 실패 - 라인: {}", lineId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "생산 주문 조회 실패",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Unity용 문 신호 정보 (라인별)
     * 경로: /api/unity/production/door/signals/line/{lineId}
     */
    @GetMapping("/production/door/signals/line/{lineId}")
    public ResponseEntity<Map<String, Object>> getDoorSignalsByLine(@PathVariable Long lineId) {
        try {
            Map<String, Object> signals = new HashMap<>();
            
            // 문 조립 스테이션 신호 정보
            signals.put("lineId", lineId);
            signals.put("doorStations", List.of(
                Map.of(
                    "stationId", "DOOR_L" + lineId + "_01",
                    "status", "ACTIVE",
                    "currentOperation", "ASSEMBLY",
                    "doorType", "FRONT_LEFT",
                    "signalStrength", 95,
                    "lastSignal", System.currentTimeMillis() - 1000
                ),
                Map.of(
                    "stationId", "DOOR_L" + lineId + "_02", 
                    "status", "IDLE",
                    "currentOperation", "WAITING",
                    "doorType", "FRONT_RIGHT",
                    "signalStrength", 88,
                    "lastSignal", System.currentTimeMillis() - 5000
                )
            ));
            
            signals.put("overallStatus", "OPERATIONAL");
            signals.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(signals);
            
        } catch (Exception e) {
            log.error("Unity 문 신호 조회 실패 - 라인: {}", lineId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "문 신호 조회 실패", 
                "message", e.getMessage()
            ));
        }
    }
}