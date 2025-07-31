package com.u1mobis.dashboard_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.dto.JwtResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting 필터
 * API 요청 제한을 통한 브루트포스 공격 방지
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    @Autowired
    private RateLimitService rateLimitService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 특정 엔드포인트에 대한 Rate Limiting 적용
        if (shouldApplyRateLimit(requestURI, method)) {
            
            RateLimitType limitType = getRateLimitType(requestURI, method);
            
            boolean allowed = switch (limitType) {
                case LOGIN -> rateLimitService.isLoginAllowed(clientIp);
                case REGISTRATION -> rateLimitService.isRegistrationAllowed(clientIp);
                case API -> rateLimitService.isApiRequestAllowed(clientIp);
            };
            
            if (!allowed) {
                handleRateLimitExceeded(response, limitType, clientIp);
                return;
            }
            
            // API 요청인 경우 요청 카운트 증가
            if (limitType == RateLimitType.API) {
                rateLimitService.recordApiRequest(clientIp);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Rate Limiting을 적용할지 판단
     */
    private boolean shouldApplyRateLimit(String requestURI, String method) {
        // 정적 자원 제외
        if (requestURI.startsWith("/static/") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") ||
            requestURI.startsWith("/favicon.ico")) {
            return false;
        }
        
        // Health check 제외
        if (requestURI.startsWith("/actuator/health")) {
            return false;
        }
        
        // 모든 API 요청에 적용
        return requestURI.startsWith("/api/");
    }
    
    /**
     * Rate Limit 타입 결정
     */
    private RateLimitType getRateLimitType(String requestURI, String method) {
        if ("POST".equals(method)) {
            if (requestURI.contains("/login")) {
                return RateLimitType.LOGIN;
            } else if (requestURI.contains("/register")) {
                return RateLimitType.REGISTRATION;
            }
        }
        
        return RateLimitType.API;
    }
    
    /**
     * Rate Limit 초과 처리
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitType limitType, String clientIp) 
            throws IOException {
        
        String message = switch (limitType) {
            case LOGIN -> "로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.";
            case REGISTRATION -> "회원가입 시도가 너무 많습니다. 잠시 후 다시 시도하세요.";
            case API -> "API 요청이 너무 많습니다. 잠시 후 다시 시도하세요.";
        };
        
        logger.warn("Rate limit exceeded - Type: {}, IP: {}", limitType, clientIp);
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Rate Limit 관련 헤더 추가
        response.setHeader("X-RateLimit-Limit", getRateLimitInfo(limitType));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("Retry-After", getRetryAfter(limitType));
        
        JwtResponse errorResponse = new JwtResponse(false, message);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * Rate Limit 정보 헤더값 생성
     */
    private String getRateLimitInfo(RateLimitType limitType) {
        return switch (limitType) {
            case LOGIN -> "10 requests per 15 minutes";
            case REGISTRATION -> "5 requests per 60 minutes";
            case API -> "100 requests per minute";
        };
    }
    
    /**
     * Retry-After 헤더값 생성 (초 단위)
     */
    private String getRetryAfter(RateLimitType limitType) {
        return switch (limitType) {
            case LOGIN -> "900"; // 15 minutes
            case REGISTRATION -> "3600"; // 60 minutes
            case API -> "60"; // 1 minute
        };
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Rate Limit 타입 열거형
     */
    private enum RateLimitType {
        LOGIN, REGISTRATION, API
    }
}