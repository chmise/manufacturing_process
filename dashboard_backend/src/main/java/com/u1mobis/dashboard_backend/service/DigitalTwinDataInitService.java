package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.entity.RobotStatus;
import com.u1mobis.dashboard_backend.entity.ProductPosition;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.RobotStatusRepository;
import com.u1mobis.dashboard_backend.repository.ProductPositionRepository;

@Service
public class DigitalTwinDataInitService implements CommandLineRunner {
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private RobotStatusRepository robotStatusRepository;
    
    @Autowired
    private ProductPositionRepository productPositionRepository;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        // 기존 데이터가 있는지 확인
        if (robotRepository.count() == 0) {
            initializeRobotData();
        }
        
        if (robotStatusRepository.count() == 0) {
            initializeRobotStatusData();
        }
        
        if (productPositionRepository.count() == 0) {
            initializeProductData();
        }
        
        System.out.println("✅ 디지털 트윈 샘플 데이터 초기화 완료 (DoorStation → WaterLeakTestStation)");
    }
    
    private void initializeRobotData() {
        // 2개 공정에만 로봇 데이터 생성: DoorStation → WaterLeakTestStation
        String[][] stationRobots = {
            {"DOOR_STATION", "도어 스테이션", "arm1", "arm2"},
            {"WATER_LEAK_TEST_STATION", "누수 검사 스테이션", "arm1", "arm2"}
        };
        
        for (String[] stationData : stationRobots) {
            String stationCode = stationData[0];
            String stationName = stationData[1];
            
            // 각 공정에 2개의 로봇 생성 (arm1, arm2)
            for (int i = 2; i < stationData.length; i++) {
                String robotName = stationData[i];
                
                // 데이터베이스 스키마에 맞는 로봇 기본 정보 저장
                Robot robot = Robot.builder()
                    .robotName(robotName)
                    .companyId(1L) // 현대차 (companies 테이블의 company_id)
                    .stationCode(stationCode)
                    .robotType("ASSEMBLY") // 조립용 로봇
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                Robot savedRobot = robotRepository.save(robot);
                System.out.println("🤖 로봇 생성: " + robotName + " (" + stationName + ") - ID: " + savedRobot.getRobotId());
            }
        }
        
        System.out.println("📊 2개 공정 로봇 데이터 생성 완료");
    }
    
    private void initializeRobotStatusData() {
        // 생성된 모든 로봇에 대해 상태 데이터 생성
        for (Robot robot : robotRepository.findAll()) {
            int motorStatus = random.nextInt(100); // 0-99 범위의 모터 상태
            int ledStatus = random.nextBoolean() ? 1 : 0; // LED 켜짐(1) 또는 꺼짐(0)
            
            RobotStatus robotStatus = RobotStatus.builder()
                .robotId(robot.getRobotId())
                .companyId(robot.getCompanyId())
                .motorStatus(motorStatus)
                .ledStatus(ledStatus)
                .timestamp(LocalDateTime.now().minusMinutes(random.nextInt(30)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            robotStatusRepository.save(robotStatus);
            System.out.println("📊 로봇 상태 생성: Robot ID " + robot.getRobotId() + " - Motor: " + motorStatus + ", LED: " + ledStatus);
        }
        
        System.out.println("📊 로봇 상태 데이터 생성 완료");
    }
    
    private void initializeProductData() {
        String[] statuses = {"IN_PROGRESS", "WAITING", "COMPLETED", "DEFECTIVE"};
        String[] stationCodes = {"DOOR_STATION", "WATER_LEAK_TEST_STATION"};
        String[] carModels = {"SONATA", "GRANDEUR", "TUCSON", "SANTA_FE"};
        
        // 2개 공정에만 제품 위치 데이터 생성
        for (String stationCode : stationCodes) {
            int productCount = random.nextInt(3) + 2; // 2~4개 제품
            
            for (int i = 0; i < productCount; i++) {
                String carModel = carModels[random.nextInt(carModels.length)];
                String productId = stationCode + "_" + carModel + "_" + String.format("%03d", i + 1);
                
                // 스테이션별 위치 설정
                double baseX = stationCode.equals("DOOR_STATION") ? 10.0 : 50.0;
                double baseY = stationCode.equals("DOOR_STATION") ? 5.0 : 15.0;
                
                ProductPosition productPosition = ProductPosition.builder()
                    .companyId(1L) // 현대차 (companies 테이블의 company_id)
                    .productId(productId)
                    .stationCode(stationCode)
                    .xPosition(baseX + (random.nextDouble() * 10)) // 스테이션 기준 위치
                    .yPosition(baseY + (random.nextDouble() * 10))
                    .zPosition(random.nextDouble() * 3)  // 0~3 범위
                    .status(statuses[random.nextInt(statuses.length)])
                    .timestamp(LocalDateTime.now().minusMinutes(random.nextInt(60)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                productPositionRepository.save(productPosition);
                System.out.println("📦 제품 생성: " + productId + " - " + stationCode + " (" + productPosition.getStatus() + ")");
            }
        }
        
        System.out.println("📦 2개 공정 제품 위치 데이터 생성 완료");
    }
}