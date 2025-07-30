package com.u1mobis.dashboard_backend.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.u1mobis.dashboard_backend.service.ManufacturingSimulatorService;
import com.u1mobis.dashboard_backend.util.JwtUtil;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.entity.Company;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/{companyName}/simulator")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ManufacturingSimulatorController {
    
    private final ManufacturingSimulatorService simulatorService;
    private final JwtUtil jwtUtil;
    private final CompanyRepository companyRepository;
    
    /**
     * 시뮬레이션 시작 - 회사별 URL 경로 사용
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSimulation(
            @PathVariable String companyName,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // URL의 회사명으로 회사 ID 조회
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                response.put("success", false);
                response.put("message", "회사 정보를 찾을 수 없습니다: " + companyName);
                return ResponseEntity.status(404).body(response);
            }
            
            // JWT 토큰으로 권한 확인 (현재 사용자가 해당 회사에 속하는지)
            if (!validateCompanyAccess(request, companyId)) {
                response.put("success", false);
                response.put("message", "해당 회사에 대한 권한이 없습니다");
                return ResponseEntity.status(403).body(response);
            }
            
            // 시뮬레이션 시작
            simulatorService.startSimulation(companyId);
            
            response.put("success", true);
            response.put("message", "시뮬레이션이 시작되었습니다");
            response.put("companyName", companyName);
            response.put("companyId", companyId);
            
            log.info("시뮬레이션 시작 요청 완료 - 회사: {}, ID: {}", companyName, companyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 시작 실패 - 회사: {}", companyName, e);
            response.put("success", false);
            response.put("message", "시뮬레이션 시작 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 시뮬레이션 중지
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopSimulation(
            @PathVariable String companyName,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                response.put("success", false);
                response.put("message", "회사 정보를 찾을 수 없습니다: " + companyName);
                return ResponseEntity.status(404).body(response);
            }
            
            if (!validateCompanyAccess(request, companyId)) {
                response.put("success", false);
                response.put("message", "해당 회사에 대한 권한이 없습니다");
                return ResponseEntity.status(403).body(response);
            }
            
            simulatorService.stopSimulation(companyId);
            
            response.put("success", true);
            response.put("message", "시뮬레이션이 중지되었습니다");
            response.put("companyName", companyName);
            response.put("companyId", companyId);
            
            log.info("시뮬레이션 중지 요청 완료 - 회사: {}, ID: {}", companyName, companyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 중지 실패 - 회사: {}", companyName, e);
            response.put("success", false);
            response.put("message", "시뮬레이션 중지 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 특정 라인 시뮬레이션 시작
     */
    @PostMapping("/start/{lineId}")
    public ResponseEntity<Map<String, Object>> startLineSimulation(
            @PathVariable String companyName,
            @PathVariable Long lineId, 
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                response.put("success", false);
                response.put("message", "회사 정보를 찾을 수 없습니다: " + companyName);
                return ResponseEntity.status(404).body(response);
            }
            
            if (!validateCompanyAccess(request, companyId)) {
                response.put("success", false);
                response.put("message", "해당 회사에 대한 권한이 없습니다");
                return ResponseEntity.status(403).body(response);
            }
            
            simulatorService.startLineSimulation(companyId, lineId);
            
            response.put("success", true);
            response.put("message", "라인 " + lineId + " 시뮬레이션이 시작되었습니다");
            response.put("companyId", companyId);
            response.put("lineId", lineId);
            
            log.info("라인 시뮬레이션 시작 요청 완료 - 회사 ID: {}, 라인 ID: {}", companyId, lineId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("라인 시뮬레이션 시작 실패 - 라인 ID: {}", lineId, e);
            response.put("success", false);
            response.put("message", "라인 시뮬레이션 시작 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 특정 라인 시뮬레이션 중지
     */
    @PostMapping("/stop/{lineId}")
    public ResponseEntity<Map<String, Object>> stopLineSimulation(
            @PathVariable String companyName,
            @PathVariable Long lineId, 
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                response.put("success", false);
                response.put("message", "회사 정보를 찾을 수 없습니다: " + companyName);
                return ResponseEntity.status(404).body(response);
            }
            
            if (!validateCompanyAccess(request, companyId)) {
                response.put("success", false);
                response.put("message", "해당 회사에 대한 권한이 없습니다");
                return ResponseEntity.status(403).body(response);
            }
            
            simulatorService.stopLineSimulation(companyId, lineId);
            
            response.put("success", true);
            response.put("message", "라인 " + lineId + " 시뮬레이션이 중지되었습니다");
            response.put("companyId", companyId);
            response.put("lineId", lineId);
            
            log.info("라인 시뮬레이션 중지 요청 완료 - 회사 ID: {}, 라인 ID: {}", companyId, lineId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("라인 시뮬레이션 중지 실패 - 라인 ID: {}", lineId, e);
            response.put("success", false);
            response.put("message", "라인 시뮬레이션 중지 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 시뮬레이션 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSimulationStatus(
            @PathVariable String companyName,
            HttpServletRequest request) {
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Map<String, Object> status = simulatorService.getSimulationStatus(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("companyId", companyId);
            response.put("simulationStatus", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 상태 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "시뮬레이션 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 시뮬레이션 설정 정보 조회
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSimulationConfig(
            @PathVariable String companyName,
            HttpServletRequest request) {
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Map<String, Object> config = new HashMap<>();
            config.put("companyId", companyId);
            config.put("supportedStations", new String[]{"DoorStation", "WaterLeakTestStation"});
            config.put("availableColors", new String[]{"Black", "Gray", "Red"}); // Unity 프리팹과 정확히 일치
            config.put("availableDoorColors", new String[]{"Black", "Gray", "Red"}); // Unity 프리팹과 일치
            config.put("simulationIntervals", Map.of(
                "environmentUpdate", "5초",
                "productMovement", "10초", 
                "robotStatusUpdate", "7초",
                "conveyorUpdate", "15초",
                "newProductGeneration", "30초",
                "productCompletion", "20초"
            ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("config", config);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 설정 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "설정 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 시뮬레이션 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSimulationStatistics(
            @PathVariable String companyName,
            HttpServletRequest request) {
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "인증 정보를 찾을 수 없습니다");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Map<String, Object> simulationStatus = simulatorService.getSimulationStatus(companyId);
            
            // 통계 데이터 생성
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalLines", simulationStatus.size());
            
            int runningLines = 0;
            int totalProductionCount = 0;
            
            for (Object lineStatusObj : simulationStatus.values()) {
                if (lineStatusObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> lineStatus = (Map<String, Object>) lineStatusObj;
                    
                    Boolean isRunning = (Boolean) lineStatus.get("isRunning");
                    if (Boolean.TRUE.equals(isRunning)) {
                        runningLines++;
                    }
                    
                    Integer productionCount = (Integer) lineStatus.get("productionCount");
                    if (productionCount != null) {
                        totalProductionCount += productionCount;
                    }
                }
            }
            
            statistics.put("runningLines", runningLines);
            statistics.put("totalProductionCount", totalProductionCount);
            statistics.put("averageProductionPerLine", 
                simulationStatus.size() > 0 ? (double) totalProductionCount / simulationStatus.size() : 0.0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("companyId", companyId);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 통계 조회 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 시뮬레이션 리셋 (모든 데이터 초기화)
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSimulation(
            @PathVariable String companyName,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long companyId = getCompanyIdByName(companyName);
            if (companyId == null) {
                response.put("success", false);
                response.put("message", "회사 정보를 찾을 수 없습니다: " + companyName);
                return ResponseEntity.status(404).body(response);
            }
            
            // 시뮬레이션 중지 후 재시작으로 리셋 구현
            simulatorService.stopSimulation(companyId);
            
            // 잠시 대기 후 재시작
            Thread.sleep(1000);
            simulatorService.startSimulation(companyId);
            
            response.put("success", true);
            response.put("message", "시뮬레이션이 리셋되었습니다");
            response.put("companyId", companyId);
            
            log.info("시뮬레이션 리셋 완료 - 회사 ID: {}", companyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("시뮬레이션 리셋 실패", e);
            response.put("success", false);
            response.put("message", "시뮬레이션 리셋 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 회사명으로 회사 ID 조회
     */
    private Long getCompanyIdByName(String companyName) {
        try {
            return companyRepository.findByCompanyName(companyName)
                .map(Company::getCompanyId)
                .orElse(null);
        } catch (Exception e) {
            log.error("회사 ID 조회 실패 - 회사명: {}", companyName, e);
            return null;
        }
    }
    
    /**
     * JWT 토큰을 통해 현재 사용자가 해당 회사에 접근 권한이 있는지 확인
     */
    private boolean validateCompanyAccess(HttpServletRequest request, Long companyId) {
        try {
            Long userCompanyId = extractCompanyIdFromRequest(request);
            return userCompanyId != null && userCompanyId.equals(companyId);
        } catch (Exception e) {
            log.error("회사 접근 권한 확인 실패", e);
            return false;
        }
    }
    
    /**
     * HTTP 요청에서 JWT 토큰을 통해 회사 ID 추출
     */
    private Long extractCompanyIdFromRequest(HttpServletRequest request) {
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.getCompanyIdFromToken(token);
            }
            
            // SecurityContext에서 추출 시도 (대안)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                // 추가적인 인증 정보 추출 로직이 필요할 수 있음
                log.warn("SecurityContext에서 회사 ID 추출 실패");
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("회사 ID 추출 실패", e);
            return null;
        }
    }
}