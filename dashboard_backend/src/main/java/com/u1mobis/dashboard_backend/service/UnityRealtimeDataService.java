package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.u1mobis.dashboard_backend.entity.CurrentProduction;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.repository.CurrentProductionRepository;
import com.u1mobis.dashboard_backend.repository.RobotRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnityRealtimeDataService {

    @Autowired
    private CurrentProductionRepository currentProductionRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    private final Random random = new Random();
    
    /**
     * Unity에서 3초마다 호출하는 실시간 데이터 조회
     */
    public Map<String, Object> getRealtimeDataForUnity() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 1. 현재 생산 중인 제품들의 위치 및 상태
            data.put("products", getCurrentProductsData());
            
            // 2. 공정별 상태 정보
            data.put("stations", getStationsData());
            
            // 3. 로봇 상태 정보
            data.put("robots", getRobotsData());
            
            // 4. 전체 라인 상태
            data.put("lineStatus", getLineStatusData());
            
            // 5. 시간 정보
            data.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.error("Unity 실시간 데이터 조회 실패: {}", e.getMessage());
            data.put("error", "데이터 조회 실패");
        }
        
        return data;
    }
    
    /**
     * 현재 생산 중인 제품들의 Unity 좌표 및 상태
     */
    private Map<String, Object> getCurrentProductsData() {
        Map<String, Object> products = new HashMap<>();
        
        try {
            List<CurrentProduction> currentProductions = 
                currentProductionRepository.findByStatus("PROCESSING");
            
            for (CurrentProduction production : currentProductions) {
                String carId = convertProductIdToCarId(production.getProductId());
                
                UnityProductData productData = new UnityProductData();
                productData.productId = production.getProductId();
                productData.carId = carId;
                productData.currentStation = production.getCurrentStation();
                productData.status = production.getStatus();
                productData.productColor = production.getProductColor();
                productData.reworkCount = production.getReworkCount();
                
                // Unity 좌표 계산 (공정에 따른 위치)
                productData.position = calculateUnityPosition(production.getCurrentStation());
                
                // 진행률 계산
                productData.progress = calculateProgress(production.getCurrentStation());
                
                products.put(carId, productData);
            }
            
        } catch (Exception e) {
            log.error("제품 데이터 조회 실패: {}", e.getMessage());
        }
        
        return products;
    }
    
    /**
     * 공정별 상태 정보
     */
    private Map<String, Object> getStationsData() {
        Map<String, Object> stations = new HashMap<>();
        
        try {
            // DoorStation 상태
            UnityStationData doorStation = new UnityStationData();
            doorStation.stationId = "DoorStation";
            doorStation.stationName = "도어 조립 공정";
            doorStation.status = "OPERATING";
            doorStation.efficiency = 85 + random.nextInt(10); // 85-95%
            doorStation.currentProduct = getCurrentProductAtStation("DoorStation");
            doorStation.temperature = 22.0 + random.nextGaussian() * 2;
            doorStation.robotCount = 3;
            
            stations.put("DoorStation", doorStation);
            
            // WaterLeakTestStation 상태
            UnityStationData waterStation = new UnityStationData();
            waterStation.stationId = "WaterLeakTestStation";
            waterStation.stationName = "누수 테스트 공정";
            waterStation.status = "OPERATING";
            waterStation.efficiency = 90 + random.nextInt(8); // 90-98%
            waterStation.currentProduct = getCurrentProductAtStation("WaterLeakTestStation");
            waterStation.temperature = 21.0 + random.nextGaussian() * 1.5;
            waterStation.robotCount = 2;
            
            stations.put("WaterLeakTestStation", waterStation);
            
        } catch (Exception e) {
            log.error("공정 데이터 조회 실패: {}", e.getMessage());
        }
        
        return stations;
    }
    
    /**
     * 로봇 상태 정보
     */
    private Map<String, Object> getRobotsData() {
        Map<String, Object> robots = new HashMap<>();
        
        try {
            List<Robot> robotList = robotRepository.findAll();
            
            for (Robot robot : robotList) {
                UnityRobotData robotData = new UnityRobotData();
                robotData.robotId = robot.getRobotId();
                robotData.robotName = robot.getRobotName();
                robotData.status = random.nextDouble() < 0.9 ? "OPERATING" : "MAINTENANCE";
                robotData.batteryLevel = 80 + random.nextInt(20); // 80-100%
                robotData.currentTask = generateRobotTask(robot.getRobotName());
                robotData.efficiency = 88 + random.nextInt(10); // 88-98%
                
                // 로봇 위치 (Unity 좌표)
                robotData.position = calculateRobotPosition(robot.getRobotId());
                
                robots.put(robot.getRobotId(), robotData);
            }
            
        } catch (Exception e) {
            log.error("로봇 데이터 조회 실패: {}", e.getMessage());
        }
        
        return robots;
    }
    
    /**
     * 전체 라인 상태
     */
    private Map<String, Object> getLineStatusData() {
        Map<String, Object> lineStatus = new HashMap<>();
        
        try {
            int totalCarsInProduction = currentProductionRepository.findByStatus("PROCESSING").size();
            
            lineStatus.put("lineId", "LINE_A");
            lineStatus.put("lineName", "메인 생산라인");
            lineStatus.put("status", "OPERATING");
            lineStatus.put("currentProduction", totalCarsInProduction);
            lineStatus.put("dailyTarget", 200);
            lineStatus.put("currentEfficiency", 92 + random.nextInt(6)); // 92-98%
            lineStatus.put("temperature", 21.5 + random.nextGaussian() * 2);
            lineStatus.put("humidity", 45 + random.nextGaussian() * 8);
            
        } catch (Exception e) {
            log.error("라인 상태 조회 실패: {}", e.getMessage());
        }
        
        return lineStatus;
    }
    
    // 유틸리티 메서드들
    
    private String convertProductIdToCarId(String productId) {
        // "A01_PROD_001" -> "CAR_001"
        if (productId.contains("PROD_")) {
            String number = productId.substring(productId.lastIndexOf("_") + 1);
            return "CAR_" + number;
        }
        return "CAR_001";
    }
    
    private UnityPosition calculateUnityPosition(String station) {
        UnityPosition position = new UnityPosition();
        
        switch (station) {
            case "DoorStation":
                // 도어 공정 Unity 좌표
                position.x = -15.0f + random.nextFloat() * 5; // -15 ~ -10
                position.y = 0.5f;
                position.z = -5.0f + random.nextFloat() * 10; // -5 ~ 5
                break;
                
            case "WaterLeakTestStation":
                // 누수 테스트 공정 Unity 좌표
                position.x = 10.0f + random.nextFloat() * 5; // 10 ~ 15
                position.y = 0.5f;
                position.z = -5.0f + random.nextFloat() * 10; // -5 ~ 5
                break;
                
            default:
                // 기본 위치
                position.x = 0.0f;
                position.y = 0.5f;
                position.z = 0.0f;
        }
        
        return position;
    }
    
    private UnityPosition calculateRobotPosition(String robotId) {
        UnityPosition position = new UnityPosition();
        
        if (robotId.contains("DOOR")) {
            // 도어 공정 로봇 위치
            position.x = -12.0f + random.nextFloat() * 4;
            position.y = 0.0f;
            position.z = -3.0f + random.nextFloat() * 6;
        } else if (robotId.contains("WATER")) {
            // 누수 테스트 공정 로봇 위치
            position.x = 12.0f + random.nextFloat() * 3;
            position.y = 0.0f;
            position.z = -3.0f + random.nextFloat() * 6;
        }
        
        return position;
    }
    
    private float calculateProgress(String currentStation) {
        switch (currentStation) {
            case "DoorStation":
                return 0.3f + random.nextFloat() * 0.2f; // 30-50%
            case "WaterLeakTestStation":
                return 0.7f + random.nextFloat() * 0.2f; // 70-90%
            default:
                return 0.0f;
        }
    }
    
    private String getCurrentProductAtStation(String station) {
        try {
            List<CurrentProduction> products = 
                currentProductionRepository.findByCurrentStationAndStatus(station, "PROCESSING");
            
            if (!products.isEmpty()) {
                return convertProductIdToCarId(products.get(0).getProductId());
            }
        } catch (Exception e) {
            log.error("공정별 제품 조회 실패: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String generateRobotTask(String robotName) {
        if (robotName.contains("도어")) {
            String[] doorTasks = {"도어 조립 중", "볼트 체결 중", "품질 검사 중", "대기 중", "이동 중"};
            return doorTasks[random.nextInt(doorTasks.length)];
        } else if (robotName.contains("누수")) {
            String[] waterTasks = {"누수 검사 중", "밀폐 테스트 중", "데이터 기록 중", "대기 중", "검사 완료"};
            return waterTasks[random.nextInt(waterTasks.length)];
        }
        return "대기 중";
    }
    
    // Unity 데이터 클래스들
    
    @Data
    public static class UnityProductData {
        public String productId;
        public String carId;
        public String currentStation;
        public String status;
        public String productColor;
        public Integer reworkCount;
        public UnityPosition position;
        public Float progress;
    }
    
    @Data
    public static class UnityStationData {
        public String stationId;
        public String stationName;
        public String status;
        public Integer efficiency;
        public String currentProduct;
        public Double temperature;
        public Integer robotCount;
    }
    
    @Data
    public static class UnityRobotData {
        public String robotId;
        public String robotName;
        public String status;
        public Integer batteryLevel;
        public String currentTask;
        public Integer efficiency;
        public UnityPosition position;
    }
    
    @Data
    public static class UnityPosition {
        public Float x = 0.0f;
        public Float y = 0.0f;
        public Float z = 0.0f;
    }
}