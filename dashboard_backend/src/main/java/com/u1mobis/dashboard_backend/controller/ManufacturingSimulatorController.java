package com.u1mobis.dashboard_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.u1mobis.dashboard_backend.service.ManufacturingSimulatorService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Slf4j
public class ManufacturingSimulatorController {

    @Autowired
    private ManufacturingSimulatorService simulatorService;
    
    /**
     * 시뮬레이션 시작
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSimulation() {
        try {
            simulatorService.startSimulation();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "제조공정 시뮬레이션이 시작되었습니다.");
            response.put("status", "RUNNING");
            
            log.info("🚀 API를 통해 시뮬레이션이 시작되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 시뮬레이션 시작 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "시뮬레이션 시작 실패: " + e.getMessage());
            response.put("status", "ERROR");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 시뮬레이션 중지
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopSimulation() {
        try {
            simulatorService.stopSimulation();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "제조공정 시뮬레이션이 중지되었습니다.");
            response.put("status", "STOPPED");
            
            log.info("⏹️ API를 통해 시뮬레이션이 중지되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 시뮬레이션 중지 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "시뮬레이션 중지 실패: " + e.getMessage());
            response.put("status", "ERROR");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 시뮬레이션 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSimulationStatus() {
        try {
            boolean isRunning = simulatorService.isSimulationRunning();
            int currentCarCount = simulatorService.getCurrentCarCount();
            List<String> carsInProduction = simulatorService.getCurrentCarsInProduction();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isRunning", isRunning);
            response.put("status", isRunning ? "RUNNING" : "STOPPED");
            response.put("currentCarCount", currentCarCount);
            response.put("carsInProduction", carsInProduction);
            response.put("message", isRunning ? "시뮬레이션 실행 중" : "시뮬레이션 중지됨");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 시뮬레이션 상태 조회 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "상태 조회 실패: " + e.getMessage());
            response.put("isRunning", false);
            response.put("status", "ERROR");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 시뮬레이터 정보 조회
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSimulatorInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("simulatorName", "현대자동차 의장공정 디지털 트윈 시뮬레이터");
        response.put("version", "1.0.0");
        response.put("description", "DoorStation → WaterLeakTestStation 2개 공정 시뮬레이션");
        
        // 시뮬레이션 기능 설명
        Map<String, Object> features = new HashMap<>();
        features.put("환경센서", "온도, 습도, 공기질 실시간 데이터 생성 (10초마다)");
        features.put("차량생산", "새 차량 30초마다 생산 시작 (랜덤 색상)");
        features.put("공정진행", "DoorStation → WaterLeakTestStation 자동 이동 (15초마다)");
        features.put("로봇상태", "5대 로봇 상태 및 작업 시뮬레이션 (20초마다)");
        features.put("품질관리", "95% 합격률, 사이클타임 90-150분");
        features.put("MQTT통신", "실시간 IoT 데이터 MQTT 브로커로 전송");
        
        response.put("features", features);
        
        // 지원하는 MQTT 토픽
        Map<String, String> mqttTopics = new HashMap<>();
        mqttTopics.put("환경데이터", "factory/LINE_A/environment");
        mqttTopics.put("생산시작", "factory/LINE_A/*/production/started");
        mqttTopics.put("공정이동", "factory/LINE_A/*/production/transfer");
        mqttTopics.put("생산완료", "factory/LINE_A/*/production/completed");
        mqttTopics.put("로봇상태", "factory/LINE_A/*/robot/status");
        
        response.put("mqttTopics", mqttTopics);
        
        // Unity 연동 정보
        Map<String, Object> unityIntegration = new HashMap<>();
        unityIntegration.put("지원객체", List.of("robot", "station", "product"));
        unityIntegration.put("로봇수", 5);
        unityIntegration.put("공정수", 2);
        unityIntegration.put("실시간업데이트", "3초마다 Unity로 데이터 전송");
        
        response.put("unityIntegration", unityIntegration);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 빠른 테스트를 위한 즉시 차량 생산
     */
    @PostMapping("/test/produce-car")
    public ResponseEntity<Map<String, Object>> produceTestCar() {
        try {
            // 시뮬레이션이 실행 중이 아니어도 테스트 차량 생산 가능
            // 실제로는 simulatorService에 테스트용 메서드 추가 필요
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "테스트 차량 생산이 요청되었습니다.");
            response.put("note", "시뮬레이션이 실행 중이어야 정상 작동합니다.");
            
            log.info("🧪 테스트 차량 생산 요청");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 테스트 차량 생산 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "테스트 차량 생산 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Manufacturing Simulator");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}