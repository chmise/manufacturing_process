package com.u1mobis.dashboard_backend.infrastructure.security;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.user.model.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class SecurityContext {
    
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    
    public SecurityContext(JwtUtil jwtUtil, HttpServletRequest request) {
        this.jwtUtil = jwtUtil;
        this.request = request;
    }
    
    public Optional<CompanyId> getCurrentCompanyId() {
        return extractTokenFromRequest()
                .map(jwtUtil::getCompanyIdFromToken)
                .map(id -> new CompanyId(String.valueOf(id)));
    }
    
    public Optional<UserId> getCurrentUserId() {
        return extractTokenFromRequest()
                .map(jwtUtil::getUserIdFromToken)
                .map(id -> new UserId(String.valueOf(id)));
    }
    
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }
    
    public CompanyId requireCurrentCompanyId() {
        return getCurrentCompanyId()
                .orElseThrow(() -> new SecurityException("Company ID not found in security context"));
    }
    
    public UserId requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new SecurityException("User ID not found in security context"));
    }
    
    private Optional<String> extractTokenFromRequest() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}