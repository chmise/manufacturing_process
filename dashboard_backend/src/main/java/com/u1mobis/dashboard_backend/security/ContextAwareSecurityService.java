package com.u1mobis.dashboard_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 컨텍스트 인식 보안 서비스
 * 위치(IP), 시간, 디바이스 기반 동적 보안 제어
 */
@Service
public class ContextAwareSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextAwareSecurityService.class);
    
    @Autowired
    private DynamicRoleService dynamicRoleService;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 위험도 점수 계산 캐시
    private final ConcurrentHashMap<String, RiskScoreCache> riskScoreCache = new ConcurrentHashMap<>();
    
    // 의심스러운 활동 추적
    private final ConcurrentHashMap<String, SuspiciousActivityTracker> suspiciousActivities = new ConcurrentHashMap<>();
    
    // 디바이스 핑거프린팅 캐시
    private final ConcurrentHashMap<String, DeviceFingerprint> deviceCache = new ConcurrentHashMap<>();
    
    // 지역별 위험도 매핑 (간단한 구현)
    private final Map<String, Integer> countryRiskLevels = Map.of(
        "KR", 0,   // 대한민국
        "US", 1,   // 미국
        "JP", 1,   // 일본
        "CN", 3,   // 중국
        "RU", 5,   // 러시아
        "Unknown", 7
    );
    
    /**
     * 종합적인 보안 컨텍스트 평가
     */
    public SecurityContextAssessment assessSecurityContext(Long userId, Long companyId, 
                                                         HttpServletRequest request) {
        try {
            String clientIp = extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String sessionId = request.getSession().getId();
            
            logger.debug("Assessing security context for user: {} from IP: {}", userId, clientIp);
            
            // 1. 기본 위험도 점수 계산
            int baseRiskScore = calculateBaseRiskScore(clientIp, userAgent);
            
            // 2. 시간 기반 위험도
            int timeRiskScore = calculateTimeBasedRisk();
            
            // 3. 디바이스 기반 위험도
            int deviceRiskScore = calculateDeviceRisk(userId, userAgent, clientIp);
            
            // 4. 위치 기반 위험도
            int locationRiskScore = calculateLocationRisk(clientIp);
            
            // 5. 행동 패턴 기반 위험도
            int behaviorRiskScore = calculateBehaviorRisk(userId, clientIp, sessionId);
            
            // 6. 전체 위험도 점수 계산 (0-100, 낮을수록 안전)
            int totalRiskScore = Math.min(100, 
                (baseRiskScore * 2 + timeRiskScore + deviceRiskScore + locationRiskScore + behaviorRiskScore) / 6);
            
            // 7. 권장 보안 레벨 결정
            SecurityLevel recommendedLevel = determineSecurityLevel(totalRiskScore);
            
            // 8. 컨텍스트 제한사항 생성
            DynamicRoleService.ContextualRestriction restriction = createContextualRestriction(
                userId, companyId, clientIp, userAgent, recommendedLevel);
            
            // 9. 의심스러운 활동 추적
            trackSuspiciousActivity(userId, clientIp, userAgent, totalRiskScore);
            
            // 10. 위험도 점수 캐시 업데이트
            updateRiskScoreCache(userId, totalRiskScore);
            
            SecurityContextAssessment assessment = new SecurityContextAssessment(
                totalRiskScore,
                recommendedLevel,
                restriction,
                createRiskDetails(baseRiskScore, timeRiskScore, deviceRiskScore, 
                                locationRiskScore, behaviorRiskScore),
                createRecommendations(totalRiskScore)
            );
            
            logger.info("Security context assessed - User: {}, Risk Score: {}, Level: {}", 
                       userId, totalRiskScore, recommendedLevel);
            
            return assessment;
            
        } catch (Exception e) {
            logger.error("Security context assessment failed for user: {}", userId, e);
            // 실패 시 보수적 접근: 높은 보안 레벨 적용
            return SecurityContextAssessment.createHighSecurityDefault();
        }
    }
    
    /**
     * 실시간 위험도 모니터링
     */
    public boolean isRiskLevelAcceptable(Long userId, DynamicRoleService.Permission permission, 
                                       HttpServletRequest request) {
        SecurityContextAssessment assessment = assessSecurityContext(userId, null, request);
        
        // 권한별 최대 허용 위험도 정의
        int maxRiskScore = getMaxRiskScoreForPermission(permission);
        
        boolean acceptable = assessment.getRiskScore() <= maxRiskScore;
        
        if (!acceptable) {
            logger.warn("Risk level too high for permission: {} (score: {}, max: {})", 
                       permission.getPermissionName(), assessment.getRiskScore(), maxRiskScore);
            
            // 고위험 활동 알림 발송 (실제 구현에서는 알림 서비스 호출)
            sendHighRiskAlert(userId, permission, assessment);
        }
        
        return acceptable;
    }
    
    /**
     * 디바이스 신뢰성 검증
     */
    public DeviceTrustLevel evaluateDeviceTrust(Long userId, String userAgent, String clientIp) {
        String deviceKey = generateDeviceKey(userId, userAgent, clientIp);
        DeviceFingerprint fingerprint = deviceCache.get(deviceKey);
        
        if (fingerprint == null) {
            // 새로운 디바이스
            fingerprint = new DeviceFingerprint(userAgent, clientIp, LocalDateTime.now(), 1);
            deviceCache.put(deviceKey, fingerprint);
            
            logger.info("New device detected for user: {} from IP: {}", userId, clientIp);
            return DeviceTrustLevel.UNKNOWN;
        }
        
        // 디바이스 사용 빈도 및 일관성 평가
        fingerprint.incrementAccessCount();
        fingerprint.setLastAccess(LocalDateTime.now());
        
        long usageDays = fingerprint.getUsageDays();
        int accessCount = fingerprint.getAccessCount();
        
        if (usageDays >= 30 && accessCount >= 50) {
            return DeviceTrustLevel.TRUSTED;
        } else if (usageDays >= 7 && accessCount >= 10) {
            return DeviceTrustLevel.FAMILIAR;
        } else if (usageDays >= 1 && accessCount >= 3) {
            return DeviceTrustLevel.RECOGNIZED;
        } else {
            return DeviceTrustLevel.UNKNOWN;
        }
    }
    
    /**
     * 지리적 위치 기반 보안 정책 적용
     */
    public boolean isGeographicAccessAllowed(String clientIp, Long companyId) {
        try {
            // 실제 구현에서는 GeoIP 라이브러리 사용
            String countryCode = extractCountryFromIp(clientIp);
            
            // 회사별 허용 국가 목록 조회 (실제 구현에서는 DB에서)
            Set<String> allowedCountries = getAllowedCountriesForCompany(companyId);
            
            if (allowedCountries.isEmpty()) {
                return true; // 제한 없음
            }
            
            boolean allowed = allowedCountries.contains(countryCode);
            
            if (!allowed) {
                logger.warn("Geographic access denied: IP {} (country: {}) for company: {}", 
                           clientIp, countryCode, companyId);
            }
            
            return allowed;
            
        } catch (Exception e) {
            logger.error("Geographic access check failed", e);
            return true; // 실패 시 허용 (가용성 우선)
        }
    }
    
    /**
     * 시간 기반 접근 제어 (근무시간 외 제한)
     */
    public boolean isTimeBasedAccessAllowed(Long companyId, DynamicRoleService.Permission permission) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek dayOfWeek = now.getDayOfWeek();
            LocalTime timeOfDay = now.toLocalTime();
            
            // 회사별 근무 시간 정책 조회 (실제 구현에서는 DB에서)
            CompanyWorkingHours workingHours = getCompanyWorkingHours(companyId);
            
            // 주말 접근 제한 확인
            if (workingHours.isWeekendAccessRestricted() && 
                (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) {
                
                // 고권한 작업은 주말에도 제한
                if (permission.getMinimumRole().getLevel() >= 60) {
                    logger.warn("Weekend access denied for high-privilege operation: {}", 
                               permission.getPermissionName());
                    return false;
                }
            }
            
            // 근무 시간 확인
            if (!timeOfDay.isAfter(workingHours.getStartTime()) || 
                !timeOfDay.isBefore(workingHours.getEndTime())) {
                
                // 업무시간 외 고권한 작업 제한
                if (permission.getMinimumRole().getLevel() >= 80) {
                    logger.warn("After-hours access denied for admin operation: {}", 
                               permission.getPermissionName());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Time-based access check failed", e);
            return true; // 실패 시 허용
        }
    }
    
    // === 내부 도우미 메서드들 ===
    
    private int calculateBaseRiskScore(String clientIp, String userAgent) {
        int score = 0;
        
        // IP 기반 위험도
        if (isPrivateIp(clientIp)) {
            score += 0; // 내부 IP는 안전
        } else if (isKnownVpnIp(clientIp)) {
            score += 15; // VPN 사용시 중간 위험
        } else if (isTorIp(clientIp)) {
            score += 30; // Tor 사용시 고위험
        }
        
        // User-Agent 기반 위험도
        if (userAgent == null || userAgent.trim().isEmpty()) {
            score += 20; // User-Agent 없음
        } else if (isAutomatedUserAgent(userAgent)) {
            score += 15; // 자동화된 도구
        } else if (isObscureUserAgent(userAgent)) {
            score += 10; // 일반적이지 않은 브라우저
        }
        
        return Math.min(score, 50);
    }
    
    private int calculateTimeBasedRisk() {
        LocalTime now = LocalTime.now();
        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
        
        // 주말은 약간 위험도 증가
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return 5;
        }
        
        // 심야 시간대 (23:00 - 06:00) 위험도 증가
        if (now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(6, 0))) {
            return 10;
        }
        
        // 일반 업무시간은 안전
        return 0;
    }
    
    private int calculateDeviceRisk(Long userId, String userAgent, String clientIp) {
        DeviceTrustLevel trustLevel = evaluateDeviceTrust(userId, userAgent, clientIp);
        
        return switch (trustLevel) {
            case TRUSTED -> 0;
            case FAMILIAR -> 5;
            case RECOGNIZED -> 10;
            case UNKNOWN -> 25;
        };
    }
    
    private int calculateLocationRisk(String clientIp) {
        try {
            String countryCode = extractCountryFromIp(clientIp);
            return countryRiskLevels.getOrDefault(countryCode, 10);
        } catch (Exception e) {
            return 10; // 불명확한 위치는 중간 위험도
        }
    }
    
    private int calculateBehaviorRisk(Long userId, String clientIp, String sessionId) {
        String activityKey = userId + ":" + clientIp;
        SuspiciousActivityTracker tracker = suspiciousActivities.get(activityKey);
        
        if (tracker == null) {
            return 0; // 첫 접근은 안전
        }
        
        int score = 0;
        
        // 짧은 시간 내 다중 접근 시도
        if (tracker.getRecentAttempts() > 10) {
            score += 15;
        }
        
        // 다양한 IP에서의 접근
        if (tracker.getUniqueIps().size() > 3) {
            score += 20;
        }
        
        // 실패한 로그인 시도 비율
        if (tracker.getFailureRate() > 0.3) {
            score += 25;
        }
        
        return Math.min(score, 40);
    }
    
    private SecurityLevel determineSecurityLevel(int riskScore) {
        if (riskScore <= 10) {
            return SecurityLevel.LOW;
        } else if (riskScore <= 25) {
            return SecurityLevel.MEDIUM;
        } else if (riskScore <= 50) {
            return SecurityLevel.HIGH;
        } else {
            return SecurityLevel.CRITICAL;
        }
    }
    
    private DynamicRoleService.ContextualRestriction createContextualRestriction(
            Long userId, Long companyId, String clientIp, String userAgent, SecurityLevel securityLevel) {
        
        DynamicRoleService.ContextualRestriction restriction = new DynamicRoleService.ContextualRestriction();
        
        switch (securityLevel) {
            case CRITICAL -> {
                // 매우 엄격한 제한
                restriction.setAllowedIps(Set.of(clientIp)); // 현재 IP만 허용
                restriction.setWorkingHours(new DynamicRoleService.WorkingHours(
                    LocalTime.of(9, 0), LocalTime.of(18, 0))); // 업무시간만
                restriction.setAllowedDevices(Set.of(extractDeviceFingerprint(userAgent)));
                restriction.setValidUntil(LocalDateTime.now().plusHours(1)); // 1시간만 유효
            }
            case HIGH -> {
                // 엄격한 제한
                restriction.setWorkingHours(new DynamicRoleService.WorkingHours(
                    LocalTime.of(8, 0), LocalTime.of(20, 0))); // 연장 업무시간
                restriction.setValidUntil(LocalDateTime.now().plusHours(4)); // 4시간 유효
            }
            case MEDIUM -> {
                // 적당한 제한
                restriction.setWorkingHours(new DynamicRoleService.WorkingHours(
                    LocalTime.of(6, 0), LocalTime.of(22, 0))); // 확장된 시간
                restriction.setValidUntil(LocalDateTime.now().plusHours(8)); // 8시간 유효
            }
            case LOW -> {
                // 최소 제한
                restriction.setValidUntil(LocalDateTime.now().plusHours(24)); // 24시간 유효
            }
        }
        
        return restriction;
    }
    
    private void trackSuspiciousActivity(Long userId, String clientIp, String userAgent, int riskScore) {
        String activityKey = userId + ":" + clientIp;
        
        suspiciousActivities.compute(activityKey, (key, tracker) -> {
            if (tracker == null) {
                tracker = new SuspiciousActivityTracker();
            }
            
            boolean isSuspicious = riskScore > 50;
            tracker.recordActivity(isSuspicious);
            tracker.addIp(clientIp);
            
            // 의심스러운 활동 감지 시 보안 감사 로깅
            if (isSuspicious && riskScore > 70) {
                String activityDescription = String.format(
                    "높은 위험도 점수 (%d) 탐지 - IP: %s, UserAgent: %s", 
                    riskScore, clientIp, userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "Unknown"
                );
                
                securityAuditService.logSuspiciousActivity(userId, null, activityDescription, riskScore, null);
                
                logger.warn("Suspicious activity detected for user {} from IP {}: risk score {}", 
                           userId, clientIp, riskScore);
            }
            
            return tracker;
        });
    }
    
    private void updateRiskScoreCache(Long userId, int riskScore) {
        String cacheKey = "risk:" + userId;
        riskScoreCache.put(cacheKey, new RiskScoreCache(riskScore, LocalDateTime.now()));
    }
    
    private Map<String, Object> createRiskDetails(int baseRisk, int timeRisk, int deviceRisk, 
                                                int locationRisk, int behaviorRisk) {
        Map<String, Object> details = new HashMap<>();
        details.put("baseRisk", baseRisk);
        details.put("timeRisk", timeRisk);
        details.put("deviceRisk", deviceRisk);
        details.put("locationRisk", locationRisk);
        details.put("behaviorRisk", behaviorRisk);
        details.put("assessmentTime", LocalDateTime.now());
        return details;
    }
    
    private List<String> createRecommendations(int riskScore) {
        List<String> recommendations = new ArrayList<>();
        
        if (riskScore > 50) {
            recommendations.add("즉시 관리자에게 알림");
            recommendations.add("추가 인증 요구");
            recommendations.add("세션 시간 제한");
        } else if (riskScore > 25) {
            recommendations.add("추가 모니터링 필요");
            recommendations.add("세션 시간 단축");
        } else if (riskScore > 10) {
            recommendations.add("정기적 재인증 권장");
        }
        
        return recommendations;
    }
    
    private int getMaxRiskScoreForPermission(DynamicRoleService.Permission permission) {
        return switch (permission.getMinimumRole()) {
            case PLATFORM_ADMIN -> 20;
            case COMPANY_OWNER -> 25;
            case COMPANY_ADMIN -> 35;
            case DEPARTMENT_HEAD -> 45;
            case EMPLOYEE -> 60;
            case READONLY -> 80;
            case GUEST -> 90;
        };
    }
    
    private void sendHighRiskAlert(Long userId, DynamicRoleService.Permission permission, 
                                 SecurityContextAssessment assessment) {
        // 실제 구현에서는 알림 서비스 호출
        logger.warn("HIGH RISK ALERT - User: {}, Permission: {}, Risk Score: {}", 
                   userId, permission.getPermissionName(), assessment.getRiskScore());
    }
    
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
    
    private String generateDeviceKey(Long userId, String userAgent, String clientIp) {
        return userId + ":" + Math.abs((userAgent + clientIp).hashCode());
    }
    
    private String extractDeviceFingerprint(String userAgent) {
        if (userAgent == null) return "unknown";
        
        // 간단한 디바이스 핑거프린팅
        if (userAgent.contains("Chrome")) return "chrome";
        if (userAgent.contains("Firefox")) return "firefox";
        if (userAgent.contains("Safari")) return "safari";
        if (userAgent.contains("Edge")) return "edge";
        
        return "other";
    }
    
    private boolean isPrivateIp(String ip) {
        return ip.startsWith("192.168.") || ip.startsWith("10.") || 
               ip.startsWith("172.") || ip.equals("127.0.0.1");
    }
    
    private boolean isKnownVpnIp(String ip) {
        // 실제 구현에서는 VPN IP 데이터베이스 확인
        return false;
    }
    
    private boolean isTorIp(String ip) {
        // 실제 구현에서는 Tor 출구 노드 리스트 확인
        return false;
    }
    
    private boolean isAutomatedUserAgent(String userAgent) {
        String lowerUA = userAgent.toLowerCase();
        return lowerUA.contains("bot") || lowerUA.contains("crawler") || 
               lowerUA.contains("spider") || lowerUA.contains("curl") ||
               lowerUA.contains("wget") || lowerUA.contains("python");
    }
    
    private boolean isObscureUserAgent(String userAgent) {
        // 일반적이지 않은 브라우저 패턴 감지
        return !userAgent.contains("Chrome") && !userAgent.contains("Firefox") && 
               !userAgent.contains("Safari") && !userAgent.contains("Edge");
    }
    
    private String extractCountryFromIp(String ip) {
        // 실제 구현에서는 GeoIP 라이브러리 사용
        // 현재는 간단한 더미 구현
        if (isPrivateIp(ip)) {
            return "KR"; // 내부 IP는 한국으로 가정
        }
        return "Unknown";
    }
    
    private Set<String> getAllowedCountriesForCompany(Long companyId) {
        // 실제 구현에서는 데이터베이스에서 조회
        return Set.of("KR", "US", "JP"); // 기본 허용 국가
    }
    
    private CompanyWorkingHours getCompanyWorkingHours(Long companyId) {
        // 실제 구현에서는 데이터베이스에서 조회
        return new CompanyWorkingHours(
            LocalTime.of(9, 0), LocalTime.of(18, 0), true);
    }
    
    // === 내부 클래스들 ===
    
    public enum SecurityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum DeviceTrustLevel {
        TRUSTED, FAMILIAR, RECOGNIZED, UNKNOWN
    }
    
    private static class RiskScoreCache {
        private final int score;
        private final LocalDateTime timestamp;
        
        public RiskScoreCache(int score, LocalDateTime timestamp) {
            this.score = score;
            this.timestamp = timestamp;
        }
        
        public int getScore() { return score; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    private static class SuspiciousActivityTracker {
        private int recentAttempts = 0;
        private int totalAttempts = 0;
        private int failedAttempts = 0;
        private final Set<String> uniqueIps = new HashSet<>();
        private LocalDateTime lastActivity = LocalDateTime.now();
        
        public void recordActivity(boolean suspicious) {
            recentAttempts++;
            totalAttempts++;
            if (suspicious) {
                failedAttempts++;
            }
            lastActivity = LocalDateTime.now();
        }
        
        public void addIp(String ip) {
            uniqueIps.add(ip);
        }
        
        public int getRecentAttempts() { return recentAttempts; }
        public Set<String> getUniqueIps() { return uniqueIps; }
        public double getFailureRate() { 
            return totalAttempts > 0 ? (double) failedAttempts / totalAttempts : 0.0; 
        }
    }
    
    private static class DeviceFingerprint {
        private final String userAgent;
        private final String firstIp;
        private final LocalDateTime firstSeen;
        private LocalDateTime lastAccess;
        private int accessCount;
        
        public DeviceFingerprint(String userAgent, String firstIp, LocalDateTime firstSeen, int accessCount) {
            this.userAgent = userAgent;
            this.firstIp = firstIp;
            this.firstSeen = firstSeen;
            this.lastAccess = firstSeen;
            this.accessCount = accessCount;
        }
        
        public void incrementAccessCount() { accessCount++; }
        public void setLastAccess(LocalDateTime lastAccess) { this.lastAccess = lastAccess; }
        
        public long getUsageDays() {
            return java.time.Duration.between(firstSeen, LocalDateTime.now()).toDays();
        }
        
        public int getAccessCount() { return accessCount; }
    }
    
    private static class CompanyWorkingHours {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final boolean weekendAccessRestricted;
        
        public CompanyWorkingHours(LocalTime startTime, LocalTime endTime, boolean weekendAccessRestricted) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.weekendAccessRestricted = weekendAccessRestricted;
        }
        
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public boolean isWeekendAccessRestricted() { return weekendAccessRestricted; }
    }
    
    /**
     * 보안 컨텍스트 평가 결과
     */
    public static class SecurityContextAssessment {
        private final int riskScore;
        private final SecurityLevel securityLevel;
        private final DynamicRoleService.ContextualRestriction contextualRestriction;
        private final Map<String, Object> riskDetails;
        private final List<String> recommendations;
        
        public SecurityContextAssessment(int riskScore, SecurityLevel securityLevel, 
                                       DynamicRoleService.ContextualRestriction contextualRestriction,
                                       Map<String, Object> riskDetails, List<String> recommendations) {
            this.riskScore = riskScore;
            this.securityLevel = securityLevel;
            this.contextualRestriction = contextualRestriction;
            this.riskDetails = riskDetails;
            this.recommendations = recommendations;
        }
        
        public static SecurityContextAssessment createHighSecurityDefault() {
            DynamicRoleService.ContextualRestriction restriction = new DynamicRoleService.ContextualRestriction();
            restriction.setWorkingHours(new DynamicRoleService.WorkingHours(
                LocalTime.of(9, 0), LocalTime.of(18, 0)));
            restriction.setValidUntil(LocalDateTime.now().plusHours(1));
            
            return new SecurityContextAssessment(
                75, SecurityLevel.HIGH, restriction, 
                Map.of("error", "Assessment failed - applying conservative security"),
                List.of("시스템 관리자에게 문의", "추가 인증 필요")
            );
        }
        
        // Getters
        public int getRiskScore() { return riskScore; }
        public SecurityLevel getSecurityLevel() { return securityLevel; }
        public DynamicRoleService.ContextualRestriction getContextualRestriction() { return contextualRestriction; }
        public Map<String, Object> getRiskDetails() { return riskDetails; }
        public List<String> getRecommendations() { return recommendations; }
    }
}