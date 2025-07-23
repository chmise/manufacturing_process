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
        
        if (productPositionRepository.count() == 0) {
            initializeProductData();
        }
        
        System.out.println("✅ 디지털 트윈 샘플 데이터 초기화 완료");
    }
    
    private void initializeRobotData() {
        // 각 공정별 로봇 데이터 생성
        String[][] stationRobots = {
            {"A01", "도어 탈거", "KUKA", "ABB", "Fanuc"},
            {"A02", "와이어링", "KUKA", "Universal", "ABB", "Fanuc"},
            {"A03", "헤드라이너", "ABB", "KUKA"},
            {"A04", "크래쉬패드", "Fanuc", "Universal", "KUKA", "ABB", "Fanuc"},
            {"B01", "연료탱크", "ABB", "KUKA"},
            {"B02", "샤시 메리지", "Fanuc", "Universal", "ABB", "KUKA"},
            {"B03", "머플러", "Universal", "Fanuc"},
            {"C01", "FEM", "KUKA", "ABB", "Fanuc"},
            {"C02", "글라스", "ABB", "Fanuc", "Universal"},
            {"C03", "시트", "KUKA", "ABB", "Fanuc"},
            {"C04", "범퍼", "Universal", "KUKA"},
            {"C05", "타이어", "Universal", "KUKA"},
            {"D01", "휠 얼라이언트", "KUKA", "Universal", "Fanuc"},
            {"D02", "헤드램프", "Fanuc", "ABB"},
            {"D03", "수밀검사", "ABB", "Fanuc", "Universal", "KUKA"}
        };
        
        Long robotId = 1L;
        
        for (String[] stationData : stationRobots) {
            String stationCode = stationData[0];
            
            // 각 공정의 로봇들 생성 (첫 번째 요소는 공정 코드, 나머지는 로봇 타입)
            for (int i = 1; i < stationData.length; i++) {
                String robotType = stationData[i];
                
                // 로봇 기본 정보 저장
                Robot robot = Robot.builder()
                    .robotId(robotId)
                    .robotName("로봇" + robotId)
                    .companyId(1L) // 현대차
                    .stationCode(stationCode)
                    .robotType(robotType)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                robotRepository.save(robot);
                
                // 로봇 상태 정보 저장
                int motorStatus = random.nextBoolean() ? 1 : 0;
                int ledStatus = random.nextBoolean() ? 1 : 0;
                
                RobotStatus robotStatus = RobotStatus.builder()
                    .robotId(robotId)
                    .companyId(1L)
                    .motorStatus(motorStatus)
                    .ledStatus(ledStatus)
                    .timestamp(LocalDateTime.now().minusMinutes(random.nextInt(30)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                robotStatusRepository.save(robotStatus);
                
                robotId++;
            }
        }
        
        System.out.println("📊 로봇 데이터 " + (robotId - 1) + "개 생성 완료");
    }
    
    private void initializeProductData() {
        String[] statuses = {"PROCESSING", "WAITING", "COMPLETED", "ERROR"};
        String[] stationCodes = {
            "A01", "A02", "A03", "A04",
            "B01", "B02", "B03",
            "C01", "C02", "C03", "C04", "C05",
            "D01", "D02", "D03"
        };
        
        // 각 공정마다 1-3개의 제품 생성
        for (String stationCode : stationCodes) {
            int productCount = random.nextInt(3) + 1; // 1~3개
            
            for (int i = 0; i < productCount; i++) {
                String productId = stationCode + "_PROD_" + String.format("%03d", i + 1);
                
                ProductPosition productPosition = ProductPosition.builder()
                    .companyId(1L) // 현대차
                    .productId(productId)
                    .stationCode(stationCode)
                    .xPosition(random.nextDouble() * 100) // 0~100 범위
                    .yPosition(random.nextDouble() * 100)
                    .zPosition(random.nextDouble() * 10)  // 0~10 범위
                    .status(statuses[random.nextInt(statuses.length)])
                    .timestamp(LocalDateTime.now().minusMinutes(random.nextInt(60)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                productPositionRepository.save(productPosition);
            }
        }
        
        System.out.println("📦 제품 위치 데이터 생성 완료");
    }
}