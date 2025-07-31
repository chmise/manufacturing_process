package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.entity.SecurityAuditLog;
import com.u1mobis.dashboard_backend.service.SecurityAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 보안 감사 로그 관리 컨트롤러
 * 보안 이벤트 조회, 통계, 분석 기능 제공
 */
@RestController
@RequestMapping("/api/security/audit")
public class SecurityAuditController {
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    /**
     * 보안 로그 조회 (페이징)
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSecurityLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            // 기본 시간 범위 설정 (지정되지 않은 경우 최근 7일)
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            // 문자열을 열거형으로 변환
            SecurityAuditLog.EventType eventTypeEnum = null;
            if (eventType != null && !eventType.isEmpty()) {
                try {
                    eventTypeEnum = SecurityAuditLog.EventType.valueOf(eventType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "잘못된 이벤트 타입: " + eventType
                    ));
                }
            }
            
            SecurityAuditLog.RiskLevel riskLevelEnum = null;
            if (riskLevel != null && !riskLevel.isEmpty()) {
                try {
                    riskLevelEnum = SecurityAuditLog.RiskLevel.valueOf(riskLevel.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "잘못된 위험도 레벨: " + riskLevel
                    ));
                }
            }
            
            Page<SecurityAuditLog> logs = securityAuditService.getSecurityLogs(
                userId, companyId, eventTypeEnum, riskLevelEnum, clientIp, 
                startTime, endTime, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logs.getContent());
            response.put("totalElements", logs.getTotalElements());
            response.put("totalPages", logs.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "보안 로그 조회 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 보안 통계 조회
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSecurityStatistics(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            // 기본 시간 범위 설정 (최근 30일)
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> statistics = securityAuditService.getSecurityStatistics(
                companyId, startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("timeRange", Map.of(
                "startTime", startTime,
                "endTime", endTime
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "보안 통계 조회 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 최근 고위험 이벤트 조회
     */
    @GetMapping("/recent-high-risk")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentHighRiskEvents(
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            List<SecurityAuditLog> highRiskEvents = securityAuditService.getRecentHighRiskEvents(hours);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("events", highRiskEvents);
            response.put("count", highRiskEvents.size());
            response.put("timeRange", hours + " hours");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "고위험 이벤트 조회 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 사용자별 의심스러운 활동 조회
     */
    @GetMapping("/suspicious-activities/{userId}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserSuspiciousActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            List<SecurityAuditLog> suspiciousActivities = 
                securityAuditService.getUserSuspiciousActivities(userId, days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("activities", suspiciousActivities);
            response.put("count", suspiciousActivities.size());
            response.put("timeRange", days + " days");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "의심스러운 활동 조회 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 실패한 로그인 시도 분석
     */
    @GetMapping("/failed-logins/analyze")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> analyzeFailedLogins(
            @RequestParam String clientIp,
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            Map<String, Object> analysis = securityAuditService.analyzeFailedLogins(clientIp, hours);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysis);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "로그인 실패 분석 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 수동으로 보안 이벤트 로깅 (테스트용)
     */
    @PostMapping("/log-event")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> logSecurityEvent(
            @RequestBody Map<String, Object> eventData,
            HttpServletRequest request) {
        
        try {
            String eventTypeStr = (String) eventData.get("eventType");
            String riskLevelStr = (String) eventData.get("riskLevel");
            String description = (String) eventData.get("description");
            Long userId = eventData.get("userId") != null ? 
                Long.valueOf(eventData.get("userId").toString()) : null;
            Long companyId = eventData.get("companyId") != null ? 
                Long.valueOf(eventData.get("companyId").toString()) : null;
            Boolean success = (Boolean) eventData.getOrDefault("success", true);
            
            SecurityAuditLog.EventType eventType = SecurityAuditLog.EventType.valueOf(eventTypeStr.toUpperCase());
            SecurityAuditLog.RiskLevel riskLevel = SecurityAuditLog.RiskLevel.valueOf(riskLevelStr.toUpperCase());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalData = (Map<String, Object>) eventData.get("additionalData");
            
            securityAuditService.logSecurityEvent(eventType, riskLevel, description, 
                                                userId, companyId, request, success, additionalData);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "보안 이벤트가 성공적으로 로깅되었습니다."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "보안 이벤트 로깅 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 사용 가능한 이벤트 타입 목록 조회
     */
    @GetMapping("/event-types")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> getEventTypes() {
        Map<String, String> eventTypes = new HashMap<>();
        for (SecurityAuditLog.EventType eventType : SecurityAuditLog.EventType.values()) {
            eventTypes.put(eventType.name(), eventType.getDescription());
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "eventTypes", eventTypes
        ));
    }
    
    /**
     * 사용 가능한 위험도 레벨 목록 조회
     */
    @GetMapping("/risk-levels")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> getRiskLevels() {
        Map<String, String> riskLevels = new HashMap<>();
        for (SecurityAuditLog.RiskLevel riskLevel : SecurityAuditLog.RiskLevel.values()) {
            riskLevels.put(riskLevel.name(), riskLevel.getDescription());
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "riskLevels", riskLevels
        ));
    }
    
    /**
     * 보안 대시보드 요약 정보
     */
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            @RequestParam(required = false) Long companyId) {
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);
            LocalDateTime oneWeekAgo = now.minusDays(7);
            
            // 최근 24시간 통계
            Map<String, Object> last24Hours = securityAuditService.getSecurityStatistics(
                companyId, oneDayAgo, now);
            
            // 최근 7일 통계
            Map<String, Object> last7Days = securityAuditService.getSecurityStatistics(
                companyId, oneWeekAgo, now);
            
            // 최근 고위험 이벤트
            List<SecurityAuditLog> recentHighRisk = securityAuditService.getRecentHighRiskEvents(24);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("last24Hours", last24Hours);
            summary.put("last7Days", last7Days);
            summary.put("recentHighRiskEvents", recentHighRisk);
            summary.put("highRiskCount", recentHighRisk.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "summary", summary
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "대시보드 요약 조회 실패: " + e.getMessage()
            ));
        }
    }
}