package com.u1mobis.dashboard_backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting 서비스
 * IP 기반 요청 제한으로 브루트포스 공격 방지
 */
@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    @Value("${security.rate-limit.login.max-attempts:10}")
    private int maxLoginAttempts;
    
    @Value("${security.rate-limit.login.window-minutes:15}")
    private int loginWindowMinutes;
    
    @Value("${security.rate-limit.api.max-requests:100}")
    private int maxApiRequests;
    
    @Value("${security.rate-limit.api.window-minutes:1}")
    private int apiWindowMinutes;
    
    @Value("${security.rate-limit.registration.max-attempts:5}")
    private int maxRegistrationAttempts;
    
    @Value("${security.rate-limit.registration.window-minutes:60}")
    private int registrationWindowMinutes;
    
    // IP별 로그인 시도 추적
    private final ConcurrentHashMap<String, AttemptTracker> loginAttempts = new ConcurrentHashMap<>();
    
    // IP별 API 요청 추적
    private final ConcurrentHashMap<String, AttemptTracker> apiRequests = new ConcurrentHashMap<>();
    
    // IP별 회원가입 시도 추적
    private final ConcurrentHashMap<String, AttemptTracker> registrationAttempts = new ConcurrentHashMap<>();
    
    /**
     * 로그인 시도 가능 여부 확인
     */
    public boolean isLoginAllowed(String clientIp) {
        return isAllowed(clientIp, loginAttempts, maxLoginAttempts, loginWindowMinutes, "LOGIN");
    }
    
    /**
     * 로그인 시도 기록
     */
    public void recordLoginAttempt(String clientIp, boolean success) {
        recordAttempt(clientIp, loginAttempts, success, "LOGIN");
    }
    
    /**
     * API 요청 가능 여부 확인
     */
    public boolean isApiRequestAllowed(String clientIp) {
        return isAllowed(clientIp, apiRequests, maxApiRequests, apiWindowMinutes, "API");
    }
    
    /**
     * API 요청 기록
     */
    public void recordApiRequest(String clientIp) {
        recordAttempt(clientIp, apiRequests, true, "API");
    }
    
    /**
     * 회원가입 시도 가능 여부 확인
     */
    public boolean isRegistrationAllowed(String clientIp) {
        return isAllowed(clientIp, registrationAttempts, maxRegistrationAttempts, registrationWindowMinutes, "REGISTRATION");
    }
    
    /**
     * 회원가입 시도 기록
     */
    public void recordRegistrationAttempt(String clientIp, boolean success) {
        recordAttempt(clientIp, registrationAttempts, success, "REGISTRATION");
    }
    
    /**
     * 공통 허용 여부 확인 로직
     */
    private boolean isAllowed(String clientIp, ConcurrentHashMap<String, AttemptTracker> attemptMap, 
                             int maxAttempts, int windowMinutes, String type) {
        
        if (clientIp == null || clientIp.isEmpty()) {
            return true; // IP를 확인할 수 없으면 허용 (로그는 남김)
        }
        
        AttemptTracker tracker = attemptMap.get(clientIp);
        if (tracker == null) {
            return true; // 첫 시도
        }
        
        // 시간 윈도우 확인
        LocalDateTime windowStart = LocalDateTime.now().minus(windowMinutes, ChronoUnit.MINUTES);
        if (tracker.getWindowStart().isBefore(windowStart)) {
            // 윈도우가 만료되었으면 리셋
            attemptMap.remove(clientIp);
            return true;
        }
        
        boolean allowed = tracker.getAttemptCount().get() < maxAttempts;
        
        if (!allowed) {
            logger.warn("{} rate limit exceeded for IP: {}, attempts: {}/{}, window: {} minutes", 
                       type, clientIp, tracker.getAttemptCount().get(), maxAttempts, windowMinutes);
        }
        
        return allowed;
    }
    
    /**
     * 공통 시도 기록 로직
     */
    private void recordAttempt(String clientIp, ConcurrentHashMap<String, AttemptTracker> attemptMap, 
                              boolean success, String type) {
        
        if (clientIp == null || clientIp.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        attemptMap.compute(clientIp, (ip, tracker) -> {
            if (tracker == null) {
                tracker = new AttemptTracker(now);
            }
            
            // 윈도우가 만료되었으면 새로 시작
            int windowMinutes = getWindowMinutes(type);
            LocalDateTime windowStart = now.minus(windowMinutes, ChronoUnit.MINUTES);
            
            if (tracker.getWindowStart().isBefore(windowStart)) {
                tracker = new AttemptTracker(now);
            }
            
            tracker.getAttemptCount().incrementAndGet();
            tracker.setLastAttempt(now);
            
            if (!success) {
                tracker.getFailedAttempts().incrementAndGet();
            }
            
            return tracker;
        });
        
        // 주기적으로 오래된 엔트리 정리
        cleanupOldEntries(attemptMap, getWindowMinutes(type));
    }
    
    /**
     * 타입별 윈도우 시간 반환
     */
    private int getWindowMinutes(String type) {
        return switch (type) {
            case "LOGIN" -> loginWindowMinutes;
            case "API" -> apiWindowMinutes;
            case "REGISTRATION" -> registrationWindowMinutes;
            default -> 15;
        };
    }
    
    /**
     * 오래된 엔트리 정리
     */
    private void cleanupOldEntries(ConcurrentHashMap<String, AttemptTracker> attemptMap, int windowMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minus(windowMinutes * 2L, ChronoUnit.MINUTES);
        
        attemptMap.entrySet().removeIf(entry -> 
            entry.getValue().getWindowStart().isBefore(cutoff)
        );
    }
    
    /**
     * IP 차단 해제
     */
    public void unblockIp(String clientIp, String type) {
        switch (type.toUpperCase()) {
            case "LOGIN" -> {
                loginAttempts.remove(clientIp);
                logger.info("Login rate limit cleared for IP: {}", clientIp);
            }
            case "API" -> {
                apiRequests.remove(clientIp);
                logger.info("API rate limit cleared for IP: {}", clientIp);
            }
            case "REGISTRATION" -> {
                registrationAttempts.remove(clientIp);
                logger.info("Registration rate limit cleared for IP: {}", clientIp);
            }
            case "ALL" -> {
                loginAttempts.remove(clientIp);
                apiRequests.remove(clientIp);
                registrationAttempts.remove(clientIp);
                logger.info("All rate limits cleared for IP: {}", clientIp);
            }
        }
    }
    
    /**
     * 현재 차단된 IP 목록 조회
     */
    public java.util.Map<String, String> getBlockedIps() {
        java.util.Map<String, String> blocked = new java.util.HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 로그인 차단 확인
        loginAttempts.forEach((ip, tracker) -> {
            if (tracker.getAttemptCount().get() >= maxLoginAttempts) {
                long remainingMinutes = ChronoUnit.MINUTES.between(now, 
                    tracker.getWindowStart().plus(loginWindowMinutes, ChronoUnit.MINUTES));
                if (remainingMinutes > 0) {
                    blocked.put(ip, "LOGIN (" + remainingMinutes + "분 남음)");
                }
            }
        });
        
        return blocked;
    }
    
    /**
     * 통계 정보 조회
     */
    public RateLimitStats getStats() {
        return new RateLimitStats(
            loginAttempts.size(),
            apiRequests.size(),
            registrationAttempts.size(),
            getBlockedIps().size()
        );
    }
    
    /**
     * 시도 추적 클래스
     */
    private static class AttemptTracker {
        private final LocalDateTime windowStart;
        private final AtomicInteger attemptCount;
        private final AtomicInteger failedAttempts;
        private volatile LocalDateTime lastAttempt;
        
        public AttemptTracker(LocalDateTime windowStart) {
            this.windowStart = windowStart;
            this.attemptCount = new AtomicInteger(0);
            this.failedAttempts = new AtomicInteger(0);
            this.lastAttempt = windowStart;
        }
        
        public LocalDateTime getWindowStart() { return windowStart; }
        public AtomicInteger getAttemptCount() { return attemptCount; }
        public AtomicInteger getFailedAttempts() { return failedAttempts; }
        public LocalDateTime getLastAttempt() { return lastAttempt; }
        public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }
    }
    
    /**
     * 통계 정보 클래스
     */
    public static class RateLimitStats {
        private final int loginTrackers;
        private final int apiTrackers;
        private final int registrationTrackers;
        private final int blockedIps;
        
        public RateLimitStats(int loginTrackers, int apiTrackers, int registrationTrackers, int blockedIps) {
            this.loginTrackers = loginTrackers;
            this.apiTrackers = apiTrackers;
            this.registrationTrackers = registrationTrackers;
            this.blockedIps = blockedIps;
        }
        
        public int getLoginTrackers() { return loginTrackers; }
        public int getApiTrackers() { return apiTrackers; }
        public int getRegistrationTrackers() { return registrationTrackers; }
        public int getBlockedIps() { return blockedIps; }
    }
}