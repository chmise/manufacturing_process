package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ClickEventService {

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // 클릭 이벤트 통합 처리 (로봇, 공정만)
    public Map<String, Object> handleClick(String objectType, String objectId) {
        Map<String, Object> response = new HashMap<>();
        
        switch (objectType.toLowerCase()) {
            case "robot":
                response = getRobotStatusData(objectId);
                break;
            case "station":
                response = getStationData(objectId);
                break;
            default:
                response.put("error", "지원하지 않는 객체 타입입니다: " + objectType);
                response.put("supported_types", "robot, station");
        }
        
        response.put("clickedObjectId", objectId);
        response.put("clickedObjectType", objectType);
        return response;
    }

    // 로봇 상태 데이터 (실제 데이터베이스 연동)
    public Map<String, Object> getRobotStatusData(String robotId) {
        Map<String, Object> robotData = new HashMap<>();
        
        try {
            Optional<Robot> robotOptional = robotRepository.findById(robotId);
            
            if (robotOptional.isPresent()) {
                Robot robot = robotOptional.get();
                
                // PostgreSQL에서 기본 로봇 정보 조회
                robotData.put("robot_id", robot.getRobotId());
                robotData.put("robot_name", robot.getRobotName());
                robotData.put("company_id", robot.getCompanyId());
                
                // 회사 정보 추가
                if (robot.getCompany() != null) {
                    robotData.put("company_name", robot.getCompany().getCompanyName());
                }
                
                // TODO: InfluxDB에서 실시간 로봇 상태 데이터 조회 필요
                // robot_status 테이블: motor_status, led_status
                robotData.put("motor_status", "데이터 없음 - InfluxDB 연동 필요");
                robotData.put("led_status", "데이터 없음 - InfluxDB 연동 필요");
                robotData.put("status", "PostgreSQL 연동 완료, InfluxDB 연동 필요");
                
            } else {
                robotData.put("error", "로봇을 찾을 수 없습니다. ID: " + robotId);
                robotData.put("robot_id", robotId);
            }
            
        } catch (Exception e) {
            robotData.put("error", "로봇 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            robotData.put("robot_id", robotId);
        }
        
        return robotData;
    }


    // 공정 데이터 (현재 운영 중인 2개 공정 기반)
    public Map<String, Object> getStationData(String stationId) {
        Map<String, Object> stationData = new HashMap<>();
        
        try {
            // 현재 운영 중인 공정 정보
            stationData.put("station_id", stationId);
            stationData.put("station_name", getStationName(stationId));
            stationData.put("company_id", 1L); // 현대차 고정값
            stationData.put("is_active", isActiveStation(stationId));
            
            if (isActiveStation(stationId)) {
                // 활성 공정 - 실시간 데이터 표시
                stationData.put("status", "운영중");
                stationData.put("current_production", "진행중");
                
                // TODO: 실제 MQTT 데이터 연동 필요
                stationData.put("production_count", "MQTT 데이터 연동 필요");
                stationData.put("cycle_time", "MQTT 데이터 연동 필요");
                stationData.put("last_update", "실시간 데이터 연동 필요");
                
                // 공정별 특화 정보
                if ("DoorStation".equalsIgnoreCase(stationId) || "1".equals(stationId)) {
                    stationData.put("process_type", "도어 조립 공정");
                    stationData.put("equipment_status", "정상 가동");
                } else if ("WaterLeakTestStation".equalsIgnoreCase(stationId) || "2".equals(stationId)) {
                    stationData.put("process_type", "수밀 검사 공정");
                    stationData.put("test_status", "검사 진행 중");
                }
                
            } else {
                // 비활성 공정
                stationData.put("status", "비활성");
                stationData.put("current_production", "대기");
                stationData.put("note", "현재 운영하지 않는 공정입니다.");
            }
            
        } catch (Exception e) {
            stationData.put("error", "공정 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            stationData.put("station_id", stationId);
        }
        
        return stationData;
    }

    // 현재 운영중인 공정인지 확인 (현재 2개 공정만 활성)
    private boolean isActiveStation(String stationId) {
        return "DoorStation".equalsIgnoreCase(stationId) || 
               "WaterLeakTestStation".equalsIgnoreCase(stationId) ||
               "1".equals(stationId) || "2".equals(stationId);
    }

    // 공정 이름 매핑 (현재 운영 상황 기반)
    private String getStationName(String stationId) {
        switch (stationId.toLowerCase()) {
            case "doorstation":
            case "1":
                return "도어 조립 공정 (DoorStation)";
            case "waterleakteststation":
            case "2":
                return "수밀 검사 공정 (WaterLeakTestStation)";
            default:
                return "미정의 공정 (" + stationId + ")";
        }
    }

    // 제품 데이터 조회 (제품 위치 및 상태 정보)
    public Map<String, Object> getProductData(String productId) {
        Map<String, Object> productData = new HashMap<>();
        
        try {
            productData.put("product_id", productId);
            
            // 제품 ID 형식 분석 (예: A01_PROD_001, CAR_001 등)
            if (productId.startsWith("A01_PROD_") || productId.startsWith("CAR_")) {
                // 현재 운영 중인 공정의 제품
                productData.put("station_code", "A01");
                productData.put("station_name", "도어 조립 공정");
                productData.put("status_text", "가공중");
                productData.put("company_id", 1L);
                
                // TODO: InfluxDB product_position 테이블에서 실제 위치 조회
                productData.put("x_position", "InfluxDB 연동 필요");
                productData.put("y_position", "InfluxDB 연동 필요");
                productData.put("z_position", "InfluxDB 연동 필요");
                productData.put("last_update", "실시간 데이터 연동 필요");
                
                // 제품 이동 경로 정보
                productData.put("current_station", "도어 조립 공정");
                productData.put("next_station", "수밀 검사 공정");
                productData.put("progress_percent", 50);
                
            } else if (productId.startsWith("A02_PROD_") || productId.contains("WaterLeak")) {
                // 수밀 검사 공정의 제품
                productData.put("station_code", "A02");
                productData.put("station_name", "수밀 검사 공정");
                productData.put("status_text", "검사중");
                productData.put("company_id", 1L);
                
                productData.put("x_position", "InfluxDB 연동 필요");
                productData.put("y_position", "InfluxDB 연동 필요");
                productData.put("z_position", "InfluxDB 연동 필요");
                productData.put("last_update", "실시간 데이터 연동 필요");
                
                productData.put("current_station", "수밀 검사 공정");
                productData.put("next_station", "완료");
                productData.put("progress_percent", 90);
                
            } else {
                // 알 수 없는 제품 ID 형식
                productData.put("status_text", "대기중");
                productData.put("station_code", "UNKNOWN");
                productData.put("station_name", "위치 확인 필요");
                productData.put("note", "제품 ID 형식을 확인해주세요: " + productId);
            }
            
        } catch (Exception e) {
            productData.put("error", "제품 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            productData.put("product_id", productId);
        }
        
        return productData;
    }
}