package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.security.ContextAwareSecurityService;
import com.u1mobis.dashboard_backend.security.DynamicRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 보안 컨텍스트 관리 API
 * 동적 권한 및 컨텍스트 인식 보안 기능 제공
 */
@RestController
@RequestMapping("/api/security")
public class SecurityContextController {
    
    @Autowired
    private ContextAwareSecurityService contextAwareSecurityService;
    
    @Autowired
    private DynamicRoleService dynamicRoleService;
    
    /**
     * 현재 사용자의 보안 컨텍스트 평가
     */
    @GetMapping("/context/assess")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> assessSecurityContext(
            @RequestParam Long userId,
            @RequestParam(required = false) Long companyId,
            HttpServletRequest request) {
        
        try {
            ContextAwareSecurityService.SecurityContextAssessment assessment = 
                contextAwareSecurityService.assessSecurityContext(userId, companyId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("riskScore", assessment.getRiskScore());
            response.put("securityLevel", assessment.getSecurityLevel());
            response.put("riskDetails", assessment.getRiskDetails());
            response.put("recommendations", assessment.getRecommendations());
            response.put("hasRestrictions", assessment.getContextualRestriction() != null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "보안 컨텍스트 평가 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 권한에 대한 실시간 위험도 확인
     */
    @PostMapping("/context/check-permission")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> checkPermissionRisk(
            @RequestParam Long userId,
            @RequestParam String permissionName,
            HttpServletRequest request) {
        
        try {
            // 권한 찾기
            DynamicRoleService.Permission permission = findPermissionByName(permissionName);
            if (permission == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "존재하지 않는 권한입니다: " + permissionName);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean acceptable = contextAwareSecurityService.isRiskLevelAcceptable(
                userId, permission, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("permission", permissionName);
            response.put("acceptable", acceptable);
            response.put("permissionLevel", permission.getMinimumRole().getDescription());
            
            if (!acceptable) {
                response.put("message", "현재 보안 컨텍스트에서는 이 권한을 사용할 수 없습니다");
                response.put("recommendation", "관리자에게 문의하거나 보안 수준을 낮춰주세요");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "권한 확인 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 사용자 권한 레벨 조회
     */
    @GetMapping("/role/level")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> getUserRoleLevel(
            @RequestParam Long userId,
            @RequestParam Long companyId) {
        
        try {
            DynamicRoleService.RoleLevel roleLevel = dynamicRoleService.getUserRoleLevel(userId, companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("companyId", companyId);
            response.put("roleLevel", roleLevel.getRoleName());
            response.put("description", roleLevel.getDescription());
            response.put("level", roleLevel.getLevel());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "권한 레벨 조회 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 사용자 권한 변경 (관리자만)
     */
    @PostMapping("/role/change")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @RequestParam Long userId,
            @RequestParam Long companyId,
            @RequestParam String newRoleName,
            @RequestParam String changedBy) {
        
        try {
            // 역할 레벨 찾기
            DynamicRoleService.RoleLevel newRole = findRoleByName(newRoleName);
            if (newRole == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "존재하지 않는 권한 레벨입니다: " + newRoleName);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            dynamicRoleService.changeUserRole(userId, companyId, newRole, changedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 권한이 성공적으로 변경되었습니다");
            response.put("userId", userId);
            response.put("newRole", newRole.getDescription());
            response.put("changedBy", changedBy);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "권한 변경 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 회사별 권한 통계 조회 (관리자만)
     */
    @GetMapping("/company/{companyId}/role-statistics")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCompanyRoleStatistics(@PathVariable Long companyId) {
        
        try {
            Map<DynamicRoleService.RoleLevel, Long> statistics = 
                dynamicRoleService.getCompanyRoleStatistics(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("companyId", companyId);
            response.put("statistics", statistics);
            
            // 총 사용자 수 계산
            long totalUsers = statistics.values().stream().mapToLong(Long::longValue).sum();
            response.put("totalUsers", totalUsers);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "권한 통계 조회 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 디바이스 신뢰성 평가
     */
    @GetMapping("/device/trust")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> evaluateDeviceTrust(
            @RequestParam Long userId,
            HttpServletRequest request) {
        
        try {
            String userAgent = request.getHeader("User-Agent");
            String clientIp = extractClientIp(request);
            
            ContextAwareSecurityService.DeviceTrustLevel trustLevel = 
                contextAwareSecurityService.evaluateDeviceTrust(userId, userAgent, clientIp);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("trustLevel", trustLevel);
            response.put("clientIp", clientIp);
            response.put("userAgent", userAgent != null ? userAgent.substring(0, Math.min(100, userAgent.length())) : null);
            
            // 신뢰도 점수 계산
            int trustScore = switch (trustLevel) {
                case TRUSTED -> 90;
                case FAMILIAR -> 70;
                case RECOGNIZED -> 50;
                case UNKNOWN -> 20;
            };
            response.put("trustScore", trustScore);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "디바이스 신뢰성 평가 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // === 헬퍼 메서드들 ===
    
    private DynamicRoleService.Permission findPermissionByName(String permissionName) {
        for (DynamicRoleService.Permission permission : DynamicRoleService.Permission.values()) {
            if (permission.getPermissionName().equals(permissionName)) {
                return permission;
            }
        }
        return null;
    }
    
    private DynamicRoleService.RoleLevel findRoleByName(String roleName) {
        for (DynamicRoleService.RoleLevel role : DynamicRoleService.RoleLevel.values()) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        return null;
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
}