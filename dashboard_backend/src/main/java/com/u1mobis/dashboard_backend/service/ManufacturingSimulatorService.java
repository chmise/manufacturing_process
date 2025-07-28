package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.CurrentProduction;
import com.u1mobis.dashboard_backend.entity.EnvironmentSensor;
import com.u1mobis.dashboard_backend.entity.ProductionCompleted;
import com.u1mobis.dashboard_backend.entity.ProductionLine;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.CurrentProductionRepository;
import com.u1mobis.dashboard_backend.repository.EnvironmentSensorRepository;
import com.u1mobis.dashboard_backend.repository.ProductionCompletedRepository;
import com.u1mobis.dashboard_backend.repository.ProductionLineRepository;
import com.u1mobis.dashboard_backend.repository.RobotRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManufacturingSimulatorService {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private ProductionLineRepository productionLineRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CurrentProductionRepository currentProductionRepository;
    
    @Autowired
    private ProductionCompletedRepository productionCompletedRepository;
    
    @Autowired
    private EnvironmentSensorRepository environmentSensorRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    private MqttClient mqttClient;
    private Company hyundaiCompany;
    private ProductionLine mainLine;
    private List<Robot> robots;
    
    // 시뮬레이션 상태
    private volatile boolean simulationRunning = false;
    private int carCounter = 1;
    private final List<String> carColors = Arrays.asList("빨강", "파랑", "흰색", "검정", "은색", "회색");
    private final List<String> stationCodes = Arrays.asList("DoorStation", "WaterLeakTestStation");
    private final List<String> currentCarsInProduction = new ArrayList<>();

    @PostConstruct
    public void initialize() {
        try {
            // 회사 및 생산라인 초기화
            initializeBasicData();
            
            // MQTT 클라이언트 초기화
            initializeMqttClient();
            
            log.info("🏭 제조공정 시뮬레이터가 초기화되었습니다.");
        } catch (Exception e) {
            log.error("❌ 시뮬레이터 초기화 실패: {}", e.getMessage());
        }
    }
    
    private void initializeBasicData() {
        // 현대자동차 회사 데이터 초기화
        hyundaiCompany = companyRepository.findByCompanyName("현대자동차")
            .orElse(companyRepository.save(Company.builder()
                .companyName("현대자동차")
                .description("현대자동차 의장공정")
                .createdAt(LocalDateTime.now())
                .build()));
        
        // 메인 생산라인 초기화
        mainLine = productionLineRepository.findByLineCode("LINE_A")
            .orElse(productionLineRepository.save(new ProductionLine(
                hyundaiCompany, "Main Production Line", "LINE_A", "의장공정 메인 라인")));
        
        // 로봇 데이터 초기화
        initializeRobots();
        
        log.info("✅ 기본 데이터 초기화 완료 - 회사: {}, 라인: {}", 
                hyundaiCompany.getCompanyName(), mainLine.getLineName());
    }
    
    private void initializeRobots() {
        robots = new ArrayList<>();
        
        // DoorStation 로봇들
        for (int i = 1; i <= 3; i++) {
            Robot robot = Robot.builder()
                .robotId("ROBOT_DOOR_" + String.format("%03d", i))
                .robotName("도어 조립 로봇 " + i)
                .companyId(hyundaiCompany.getCompanyId())
                .lineId(mainLine.getLineId())
                .build();
            
            robotRepository.save(robot);
            robots.add(robot);
        }
        
        // WaterLeakTestStation 로봇들
        for (int i = 1; i <= 2; i++) {
            Robot robot = Robot.builder()
                .robotId("ROBOT_WATER_" + String.format("%03d", i))
                .robotName("누수 검사 로봇 " + i)
                .companyId(hyundaiCompany.getCompanyId())
                .lineId(mainLine.getLineId())
                .build();
            
            robotRepository.save(robot);
            robots.add(robot);
        }
        
        log.info("🤖 로봇 {} 대 초기화 완료", robots.size());
    }
    
    private void initializeMqttClient() {
        try {
            mqttClient = new MqttClient("tcp://localhost:1883", 
                "manufacturing-simulator-" + System.currentTimeMillis());
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setKeepAliveInterval(30);
            
            mqttClient.connect(options);
            log.info("📡 MQTT 클라이언트 연결 완료");
        } catch (Exception e) {
            log.error("❌ MQTT 클라이언트 초기화 실패: {}", e.getMessage());
        }
    }
    
    public void startSimulation() {
        if (simulationRunning) {
            log.warn("⚠️ 시뮬레이션이 이미 실행 중입니다.");
            return;
        }
        
        simulationRunning = true;
        log.info("🚀 제조공정 시뮬레이션 시작!");
        
        // 1. 환경 센서 데이터 시뮬레이션 (10초마다)
        scheduler.scheduleAtFixedRate(this::simulateEnvironmentData, 0, 10, TimeUnit.SECONDS);
        
        // 2. 새 차량 생산 시작 (30초마다)
        scheduler.scheduleAtFixedRate(this::startNewCarProduction, 5, 30, TimeUnit.SECONDS);
        
        // 3. 생산 진행 상황 업데이트 (15초마다)
        scheduler.scheduleAtFixedRate(this::updateProductionProgress, 10, 15, TimeUnit.SECONDS);
        
        // 4. 로봇 상태 업데이트 (20초마다)
        scheduler.scheduleAtFixedRate(this::updateRobotStatus, 15, 20, TimeUnit.SECONDS);
    }
    
    public void stopSimulation() {
        simulationRunning = false;
        scheduler.shutdown();
        log.info("⏹️ 제조공정 시뮬레이션 중지");
    }
    
    private void simulateEnvironmentData() {
        if (!simulationRunning) return;
        
        try {
            // 환경 센서 데이터 생성
            EnvironmentSensor envData = EnvironmentSensor.builder()
                .timestamp(LocalDateTime.now())
                .temperature(20.0 + random.nextGaussian() * 3.0) // 20±3도
                .humidity(45.0 + random.nextGaussian() * 10.0)   // 45±10%
                .airQuality(50 + random.nextInt(30))             // 50-80
                .lineId(mainLine.getLineId())
                .sensorLocation("메인 생산라인")
                .build();
            
            environmentSensorRepository.save(envData);
            
            // MQTT로 환경 데이터 전송
            EnvironmentMqttData mqttData = new EnvironmentMqttData();
            mqttData.timestamp = envData.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            mqttData.temperature = envData.getTemperature();
            mqttData.humidity = envData.getHumidity();
            mqttData.airQuality = envData.getAirQuality();
            mqttData.location = envData.getSensorLocation();
            
            publishMqttMessage("factory/LINE_A/environment", mqttData);
            
        } catch (Exception e) {
            log.error("환경 데이터 시뮬레이션 오류: {}", e.getMessage());
        }
    }
    
    private void startNewCarProduction() {
        if (!simulationRunning) return;
        
        try {
            String productId = String.format("A01_PROD_%03d", carCounter);
            String carColor = carColors.get(random.nextInt(carColors.size()));
            
            // 새 차량 생산 시작
            CurrentProduction newCar = CurrentProduction.builder()
                .productId(productId)
                .productColor(carColor)
                .startTime(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusHours(4)) // 4시간 후 납기
                .reworkCount(0)
                .currentStation("DoorStation")
                .status("PROCESSING")
                .lineId(mainLine.getLineId())
                .build();
            
            currentProductionRepository.save(newCar);
            currentCarsInProduction.add(productId);
            
            // MQTT로 생산 시작 알림
            ProductionStartMqttData mqttData = new ProductionStartMqttData();
            mqttData.productId = productId;
            mqttData.productColor = carColor;
            mqttData.startTime = newCar.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            mqttData.currentStation = "DoorStation";
            mqttData.status = "STARTED";
            
            publishMqttMessage("factory/LINE_A/DoorStation/production/started", mqttData);
            
            carCounter++;
            log.info("🚗 새 차량 생산 시작: {} (색상: {})", productId, carColor);
            
        } catch (Exception e) {
            log.error("차량 생산 시작 오류: {}", e.getMessage());
        }
    }
    
    private void updateProductionProgress() {
        if (!simulationRunning) return;
        
        try {
            List<CurrentProduction> carsInProduction = 
                currentProductionRepository.findByStatus("PROCESSING");
            
            for (CurrentProduction car : carsInProduction) {
                // 70% 확률로 다음 공정으로 이동
                if (random.nextDouble() < 0.7) {
                    String currentStation = car.getCurrentStation();
                    String nextStation = getNextStation(currentStation);
                    
                    if (nextStation != null) {
                        // 다음 공정으로 이동
                        car.setCurrentStation(nextStation);
                        currentProductionRepository.save(car);
                        
                        // MQTT로 공정 이동 알림
                        StationTransferMqttData mqttData = new StationTransferMqttData();
                        mqttData.productId = car.getProductId();
                        mqttData.fromStation = currentStation;
                        mqttData.toStation = nextStation;
                        mqttData.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        
                        publishMqttMessage(
                            String.format("factory/LINE_A/%s/production/transfer", nextStation), 
                            mqttData);
                        
                        log.info("🔄 차량 {} 이동: {} → {}", 
                                car.getProductId(), currentStation, nextStation);
                        
                    } else if ("WaterLeakTestStation".equals(currentStation)) {
                        // 마지막 공정 완료
                        completeProduction(car);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("생산 진행 업데이트 오류: {}", e.getMessage());
        }
    }
    
    private String getNextStation(String currentStation) {
        switch (currentStation) {
            case "DoorStation":
                return "WaterLeakTestStation";
            case "WaterLeakTestStation":
                return null; // 마지막 공정
            default:
                return null;
        }
    }
    
    private void completeProduction(CurrentProduction car) {
        try {
            // 품질 판정 (95% 합격)
            boolean isPass = random.nextDouble() < 0.95;
            String quality = isPass ? "PASS" : "FAIL";
            
            // 사이클 타임 계산 (90-150분)
            double cycleTimeMinutes = 90 + random.nextDouble() * 60;
            
            // 정시 납기 여부 확인
            boolean isOnTime = LocalDateTime.now().isBefore(car.getDueDate());
            
            // 생산 완료 기록
            ProductionCompleted completed = ProductionCompleted.builder()
                .productId(car.getProductId())
                .timestamp(LocalDateTime.now())
                .cycleTime(cycleTimeMinutes * 60) // 초 단위로 저장
                .quality(quality)
                .dueDate(car.getDueDate())
                .isOnTime(isOnTime)
                .isFirstTimePass(car.getReworkCount() == 0)
                .lineId(car.getLineId())
                .build();
            
            productionCompletedRepository.save(completed);
            
            // 현재 생산에서 제거
            car.setStatus("COMPLETED");
            currentProductionRepository.save(car);
            currentCarsInProduction.remove(car.getProductId());
            
            // MQTT로 생산 완료 알림
            ProductionCompletedMqttData mqttData = new ProductionCompletedMqttData();
            mqttData.productId = car.getProductId();
            mqttData.completedTime = completed.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            mqttData.cycleTime = cycleTimeMinutes;
            mqttData.quality = quality;
            mqttData.isOnTime = isOnTime;
            mqttData.isFirstTimePass = completed.getIsFirstTimePass();
            
            publishMqttMessage("factory/LINE_A/WaterLeakTestStation/production/completed", mqttData);
            
            log.info("✅ 차량 {} 생산 완료 - 품질: {}, 사이클타임: {:.1f}분, 정시납기: {}", 
                    car.getProductId(), quality, cycleTimeMinutes, isOnTime);
            
        } catch (Exception e) {
            log.error("생산 완료 처리 오류: {}", e.getMessage());
        }
    }
    
    private void updateRobotStatus() {
        if (!simulationRunning) return;
        
        try {
            for (Robot robot : robots) {
                // 로봇 상태 시뮬레이션
                String status = random.nextDouble() < 0.9 ? "OPERATING" : "MAINTENANCE";
                int batteryLevel = 80 + random.nextInt(20); // 80-100%
                String currentTask = generateRandomTask(robot.getRobotName());
                
                // MQTT로 로봇 상태 전송
                RobotStatusMqttData mqttData = new RobotStatusMqttData();
                mqttData.robotId = robot.getRobotId();
                mqttData.robotName = robot.getRobotName();
                mqttData.status = status;
                mqttData.batteryLevel = batteryLevel;
                mqttData.currentTask = currentTask;
                mqttData.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                
                String station = robot.getRobotId().contains("DOOR") ? "DoorStation" : "WaterLeakTestStation";
                publishMqttMessage(
                    String.format("factory/LINE_A/%s/robot/status", station), 
                    mqttData);
            }
            
        } catch (Exception e) {
            log.error("로봇 상태 업데이트 오류: {}", e.getMessage());
        }
    }
    
    private String generateRandomTask(String robotName) {
        if (robotName.contains("도어")) {
            String[] doorTasks = {"도어 조립", "볼트 체결", "검사", "대기 중"};
            return doorTasks[random.nextInt(doorTasks.length)];
        } else if (robotName.contains("누수")) {
            String[] waterTasks = {"누수 검사", "밀폐성 확인", "데이터 기록", "대기 중"};
            return waterTasks[random.nextInt(waterTasks.length)];
        }
        return "대기 중";
    }
    
    private void publishMqttMessage(String topic, Object data) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        
        try {
            String jsonPayload = objectMapper.writeValueAsString(data);
            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            
        } catch (Exception e) {
            log.error("MQTT 메시지 발행 실패: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    public void cleanup() {
        stopSimulation();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.error("MQTT 클라이언트 정리 실패: {}", e.getMessage());
        }
    }
    
    public boolean isSimulationRunning() {
        return simulationRunning;
    }
    
    public int getCurrentCarCount() {
        return currentCarsInProduction.size();
    }
    
    public List<String> getCurrentCarsInProduction() {
        return new ArrayList<>(currentCarsInProduction);
    }
    
    // MQTT 데이터 클래스들
    @Data
    public static class EnvironmentMqttData {
        public String timestamp;
        public Double temperature;
        public Double humidity;
        public Integer airQuality;
        public String location;
    }
    
    @Data
    public static class ProductionStartMqttData {
        public String productId;
        public String productColor;
        public String startTime;
        public String currentStation;
        public String status;
    }
    
    @Data
    public static class StationTransferMqttData {
        public String productId;
        public String fromStation;
        public String toStation;
        public String timestamp;
    }
    
    @Data
    public static class ProductionCompletedMqttData {
        public String productId;
        public String completedTime;
        public Double cycleTime;
        public String quality;
        public Boolean isOnTime;
        public Boolean isFirstTimePass;
    }
    
    @Data
    public static class RobotStatusMqttData {
        public String robotId;
        public String robotName;
        public String status;
        public Integer batteryLevel;
        public String currentTask;
        public String timestamp;
    }
}