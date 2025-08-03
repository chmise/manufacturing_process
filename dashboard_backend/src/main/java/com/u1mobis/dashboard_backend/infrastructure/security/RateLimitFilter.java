package com.u1mobis.dashboard_backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastResetTime = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long MINUTE_IN_MILLISECONDS = 60 * 1000;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        
        if (isRateLimited(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp) {
        long currentTime = System.currentTimeMillis();
        
        // Reset counter if a minute has passed
        Long lastReset = lastResetTime.get(clientIp);
        if (lastReset == null || (currentTime - lastReset) > MINUTE_IN_MILLISECONDS) {
            requestCounts.put(clientIp, new AtomicInteger(0));
            lastResetTime.put(clientIp, currentTime);
        }
        
        AtomicInteger count = requestCounts.get(clientIp);
        if (count == null) {
            count = new AtomicInteger(0);
            requestCounts.put(clientIp, count);
        }
        
        int currentCount = count.incrementAndGet();
        
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} (requests: {})", clientIp, currentCount);
            return true;
        }
        
        return false;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
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
}