package com.u1mobis.dashboard_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.entity.SecurityAuditLog;
import com.u1mobis.dashboard_backend.repository.SecurityAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 보안 감사 서비스
 * 모든 보안 이벤트를 로깅하고 추적하는 중앙 집중식 서비스
 */
@Service
@Transactional
public class SecurityAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditService.class);
    
    @Autowired
    private SecurityAuditLogRepository auditLogRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 보안 이벤트 로그 기록 (비동기)
     */
    @Async
    public void logSecurityEvent(SecurityAuditLog.EventType eventType,
                                SecurityAuditLog.RiskLevel riskLevel,
                                String description,
                                Long userId,
                                Long companyId,
                                HttpServletRequest request,
                                boolean success,
                                Map<String, Object> additionalData) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .eventType(eventType)
                .riskLevel(riskLevel)
                .eventDescription(description)
                .userId(userId)
                .companyId(companyId)
                .success(success)
                .build();
            
            // 요청 정보 추출
            if (request != null) {
                auditLog.setClientIp(extractClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestUri(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
                auditLog.setSessionId(request.getSession().getId());
            }
            
            // 추가 데이터 JSON 직렬화
            if (additionalData != null && !additionalData.isEmpty()) {
                auditLog.setAdditionalData(objectMapper.writeValueAsString(additionalData));
            }
            
            // 고유 감사 추적 ID 생성
            auditLog.setAuditTrailId(generateAuditTrailId());
            
            auditLogRepository.save(auditLog);
            
            // 고위험 이벤트는 즉시 로그로도 출력
            if (riskLevel == SecurityAuditLog.RiskLevel.HIGH || 
                riskLevel == SecurityAuditLog.RiskLevel.CRITICAL) {
                logger.warn("HIGH RISK SECURITY EVENT: {} - {} (User: {}, IP: {})", 
                          eventType, description, userId, auditLog.getClientIp());
            }
            
        } catch (Exception e) {
            logger.error("Failed to log security event: {} - {}", eventType, description, e);
        }
    }
    
    /**
     * 로그인 시도 로깅
     */
    public void logLoginAttempt(String username, String clientIp, boolean success, 
                               String failureReason, HttpServletRequest request) {
        SecurityAuditLog.EventType eventType = success ? 
            SecurityAuditLog.EventType.LOGIN_SUCCESS : SecurityAuditLog.EventType.LOGIN_FAILURE;
        
        SecurityAuditLog.RiskLevel riskLevel = success ? 
            SecurityAuditLog.RiskLevel.LOW : SecurityAuditLog.RiskLevel.MEDIUM;
        
        String description = success ? 
            "사용자 로그인 성공: " + username : 
            "사용자 로그인 실패: " + username + (failureReason != null ? " (" + failureReason + ")" : "");
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("username", username);
        if (failureReason != null) {
            additionalData.put("failureReason", failureReason);
        }
        
        logSecurityEvent(eventType, riskLevel, description, null, null, 
                        request, success, additionalData);
    }
    
    /**
     * 권한 관련 이벤트 로깅
     */
    public void logPermissionEvent(Long userId, Long companyId, String permissionName, 
                                  boolean granted, String reason, HttpServletRequest request) {
        SecurityAuditLog.EventType eventType = granted ? 
            SecurityAuditLog.EventType.PERMISSION_GRANTED : SecurityAuditLog.EventType.PERMISSION_DENIED;
        
        SecurityAuditLog.RiskLevel riskLevel = granted ? 
            SecurityAuditLog.RiskLevel.LOW : SecurityAuditLog.RiskLevel.MEDIUM;
        
        String description = String.format("권한 %s: %s (%s)", 
                                         granted ? "부여" : "거부", permissionName, reason);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("permissionName", permissionName);
        additionalData.put("reason", reason);
        
        logSecurityEvent(eventType, riskLevel, description, userId, companyId, 
                        request, granted, additionalData);
    }
    
    /**
     * 의심스러운 활동 로깅
     */
    public void logSuspiciousActivity(Long userId, Long companyId, String activityDescription, 
                                    int riskScore, HttpServletRequest request) {
        SecurityAuditLog.RiskLevel riskLevel;
        if (riskScore >= 80) {
            riskLevel = SecurityAuditLog.RiskLevel.CRITICAL;
        } else if (riskScore >= 60) {
            riskLevel = SecurityAuditLog.RiskLevel.HIGH;
        } else {
            riskLevel = SecurityAuditLog.RiskLevel.MEDIUM;
        }
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("riskScore", riskScore);
        additionalData.put("detectionTime", LocalDateTime.now());
        
        logSecurityEvent(SecurityAuditLog.EventType.SUSPICIOUS_ACTIVITY, riskLevel, 
                        activityDescription, userId, companyId, request, false, additionalData);
    }
    
    /**
     * 데이터 접근 로깅
     */
    public void logDataAccess(Long userId, Long companyId, String resourceName, 
                             String operation, boolean success, HttpServletRequest request) {
        SecurityAuditLog.EventType eventType;
        switch (operation.toUpperCase()) {
            case "READ":
                eventType = SecurityAuditLog.EventType.DATA_ACCESS;
                break;
            case "WRITE":
            case "UPDATE":
            case "DELETE":
                eventType = SecurityAuditLog.EventType.DATA_MODIFICATION;
                break;
            case "EXPORT":
                eventType = SecurityAuditLog.EventType.DATA_EXPORT;
                break;
            default:
                eventType = SecurityAuditLog.EventType.DATA_ACCESS;
        }
        
        String description = String.format("데이터 %s: %s", operation, resourceName);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("resourceName", resourceName);
        additionalData.put("operation", operation);
        
        logSecurityEvent(eventType, SecurityAuditLog.RiskLevel.LOW, description, 
                        userId, companyId, request, success, additionalData);
    }
    
    // === 조회 메서드들 ===
    
    /**
     * 보안 로그 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getSecurityLogs(Long userId, Long companyId, 
                                                 SecurityAuditLog.EventType eventType,
                                                 SecurityAuditLog.RiskLevel riskLevel,
                                                 String clientIp,
                                                 LocalDateTime startTime,
                                                 LocalDateTime endTime,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        return auditLogRepository.findByMultipleCriteria(
            userId, companyId, eventType, riskLevel, clientIp, 
            startTime, endTime, pageable);
    }
    
    /**
     * 보안 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSecurityStatistics(Long companyId, 
                                                     LocalDateTime startTime, 
                                                     LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 이벤트 타입별 통계
        List<Object[]> eventTypeStats = auditLogRepository.getEventTypeStatistics(startTime, endTime);
        Map<String, Long> eventTypeCounts = eventTypeStats.stream()
            .collect(Collectors.toMap(
                row -> ((SecurityAuditLog.EventType) row[0]).getDescription(),
                row -> (Long) row[1]
            ));
        statistics.put("eventTypes", eventTypeCounts);
        
        // 위험도별 통계
        List<Object[]> riskLevelStats = auditLogRepository.getRiskLevelStatistics(startTime, endTime);
        Map<String, Long> riskLevelCounts = riskLevelStats.stream()
            .collect(Collectors.toMap(
                row -> ((SecurityAuditLog.RiskLevel) row[0]).getDescription(),
                row -> (Long) row[1]
            ));
        statistics.put("riskLevels", riskLevelCounts);
        
        // 일별 이벤트 수
        List<Object[]> dailyStats = auditLogRepository.getDailyEventCounts(startTime, endTime);
        Map<String, Long> dailyCounts = dailyStats.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
        statistics.put("dailyEvents", dailyCounts);
        
        // 회사별 요약 (companyId가 있는 경우)
        if (companyId != null) {
            Object[] companySummary = auditLogRepository.getCompanySecuritySummary(
                companyId, startTime, endTime);
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEvents", companySummary[0]);
            summary.put("successfulEvents", companySummary[1]);
            summary.put("failedEvents", companySummary[2]);
            summary.put("highRiskEvents", companySummary[3]);
            statistics.put("companySummary", summary);
        }
        
        return statistics;
    }
    
    /**
     * 최근 고위험 이벤트 조회
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getRecentHighRiskEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findRecentHighRiskEvents(since);
    }
    
    /**
     * 사용자별 의심스러운 활동 조회
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getUserSuspiciousActivities(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findSuspiciousActivitiesByUser(userId, since);
    }
    
    /**
     * 실패한 로그인 시도 분석
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeFailedLogins(String clientIp, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<SecurityAuditLog> failedAttempts = auditLogRepository.findFailedLoginAttempts(clientIp, since);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalAttempts", failedAttempts.size());
        analysis.put("timeRange", hours + " hours");
        analysis.put("clientIp", clientIp);
        
        // 시도된 사용자명 목록
        Set<String> attemptedUsernames = failedAttempts.stream()
            .map(SecurityAuditLog::getUsername)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        analysis.put("attemptedUsernames", attemptedUsernames);
        
        // 시간대별 분포
        Map<Integer, Long> hourlyDistribution = failedAttempts.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().getHour(),
                Collectors.counting()
            ));
        analysis.put("hourlyDistribution", hourlyDistribution);
        
        return analysis;
    }
    
    // === 헬퍼 메서드들 ===
    
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String generateAuditTrailId() {
        return "AUDIT_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}