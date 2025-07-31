package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 동적 권한 관리 서비스
 * 5단계 계층형 권한 체계 및 컨텍스트 기반 권한 제어
 */
@Service
public class DynamicRoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicRoleService.class);
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    // 권한 레벨 정의 (높은 숫자 = 높은 권한)
    public enum RoleLevel {
        PLATFORM_ADMIN(100, "PLATFORM_ADMIN", "플랫폼 관리자"),
        COMPANY_OWNER(80, "COMPANY_OWNER", "회사 소유자"),
        COMPANY_ADMIN(60, "COMPANY_ADMIN", "회사 관리자"),
        DEPARTMENT_HEAD(40, "DEPARTMENT_HEAD", "부서장"),
        EMPLOYEE(20, "EMPLOYEE", "일반 직원"),
        READONLY(10, "READONLY", "읽기 전용"),
        GUEST(0, "GUEST", "게스트");
        
        private final int level;
        private final String roleName;
        private final String description;
        
        RoleLevel(int level, String roleName, String description) {
            this.level = level;
            this.roleName = roleName;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getRoleName() { return roleName; }
        public String getDescription() { return description; }
        
        public boolean isHigherThan(RoleLevel other) {
            return this.level > other.level;
        }
        
        public boolean isEqualOrHigherThan(RoleLevel other) {
            return this.level >= other.level;
        }
    }
    
    // 권한별 허용 작업 정의
    public enum Permission {
        // 시스템 관리
        MANAGE_PLATFORM("MANAGE_PLATFORM", "플랫폼 관리", RoleLevel.PLATFORM_ADMIN),
        MANAGE_ALL_COMPANIES("MANAGE_ALL_COMPANIES", "모든 회사 관리", RoleLevel.PLATFORM_ADMIN),
        
        // 회사 관리
        MANAGE_COMPANY("MANAGE_COMPANY", "회사 관리", RoleLevel.COMPANY_OWNER),
        MANAGE_COMPANY_SETTINGS("MANAGE_COMPANY_SETTINGS", "회사 설정 관리", RoleLevel.COMPANY_OWNER),
        MANAGE_COMPANY_USERS("MANAGE_COMPANY_USERS", "회사 사용자 관리", RoleLevel.COMPANY_ADMIN),
        
        // 부서 관리
        MANAGE_DEPARTMENT("MANAGE_DEPARTMENT", "부서 관리", RoleLevel.DEPARTMENT_HEAD),
        MANAGE_DEPARTMENT_USERS("MANAGE_DEPARTMENT_USERS", "부서 사용자 관리", RoleLevel.DEPARTMENT_HEAD),
        
        // 데이터 접근
        READ_ALL_DATA("READ_ALL_DATA", "모든 데이터 읽기", RoleLevel.COMPANY_ADMIN),
        READ_DEPARTMENT_DATA("READ_DEPARTMENT_DATA", "부서 데이터 읽기", RoleLevel.DEPARTMENT_HEAD),
        READ_OWN_DATA("READ_OWN_DATA", "자신 데이터 읽기", RoleLevel.EMPLOYEE),
        
        // 시뮬레이터 및 생산 관리
        MANAGE_SIMULATOR("MANAGE_SIMULATOR", "시뮬레이터 관리", RoleLevel.COMPANY_ADMIN),
        CONTROL_PRODUCTION("CONTROL_PRODUCTION", "생산 제어", RoleLevel.DEPARTMENT_HEAD),
        VIEW_PRODUCTION("VIEW_PRODUCTION", "생산 현황 보기", RoleLevel.EMPLOYEE),
        
        // 보고서 및 분석
        GENERATE_REPORTS("GENERATE_REPORTS", "보고서 생성", RoleLevel.DEPARTMENT_HEAD),
        VIEW_ANALYTICS("VIEW_ANALYTICS", "분석 데이터 보기", RoleLevel.EMPLOYEE),
        
        // 기본 권한
        VIEW_DASHBOARD("VIEW_DASHBOARD", "대시보드 보기", RoleLevel.READONLY);
        
        private final String permissionName;
        private final String description;
        private final RoleLevel minimumRole;
        
        Permission(String permissionName, String description, RoleLevel minimumRole) {
            this.permissionName = permissionName;
            this.description = description;
            this.minimumRole = minimumRole;
        }
        
        public String getPermissionName() { return permissionName; }
        public String getDescription() { return description; }
        public RoleLevel getMinimumRole() { return minimumRole; }
    }
    
    // 사용자별 동적 권한 정보 캐시
    private final ConcurrentHashMap<String, UserRoleInfo> userRoleCache = new ConcurrentHashMap<>();
    
    // 컨텍스트별 권한 제한 정보
    private final ConcurrentHashMap<String, ContextualRestriction> contextualRestrictions = new ConcurrentHashMap<>();
    
    /**
     * 사용자의 현재 권한 레벨 확인
     */
    public RoleLevel getUserRoleLevel(Long userId, Long companyId) {
        String userKey = generateUserKey(userId, companyId);
        UserRoleInfo roleInfo = userRoleCache.get(userKey);
        
        if (roleInfo == null) {
            // 데이터베이스에서 사용자 권한 조회 (실제 구현 필요)
            roleInfo = loadUserRoleFromDatabase(userId, companyId);
            userRoleCache.put(userKey, roleInfo);
        }
        
        return roleInfo.getRoleLevel();
    }
    
    /**
     * 권한 확인
     */
    public boolean hasPermission(Long userId, Long companyId, Permission permission) {
        RoleLevel userRole = getUserRoleLevel(userId, companyId);
        return userRole.isEqualOrHigherThan(permission.getMinimumRole());
    }
    
    /**
     * 컨텍스트 기반 권한 확인
     */
    public boolean hasPermissionWithContext(Long userId, Long companyId, Permission permission, 
                                          String clientIp, String userAgent, HttpServletRequest request) {
        // 기본 권한 확인
        boolean hasBasicPermission = hasPermission(userId, companyId, permission);
        if (!hasBasicPermission) {
            // 권한 거부 로깅
            securityAuditService.logPermissionEvent(userId, companyId, permission.name(), 
                false, "기본 권한 없음", request);
            return false;
        }
        
        // 컨텍스트 제한 확인
        String userKey = generateUserKey(userId, companyId);
        ContextualRestriction restriction = contextualRestrictions.get(userKey);
        
        if (restriction != null) {
            // IP 제한 확인
            if (restriction.getAllowedIps() != null && !restriction.getAllowedIps().isEmpty()) {
                if (!isIpAllowed(clientIp, restriction.getAllowedIps())) {
                    logger.warn("IP access denied for user {} from IP: {}", userId, clientIp);
                    securityAuditService.logPermissionEvent(userId, companyId, permission.name(), 
                        false, "IP 제한 위반: " + clientIp, request);
                    return false;
                }
            }
            
            // 시간 제한 확인
            if (restriction.getWorkingHours() != null) {
                if (!isWithinWorkingHours(restriction.getWorkingHours())) {
                    logger.warn("Time-based access denied for user {} at: {}", userId, LocalTime.now());
                    securityAuditService.logPermissionEvent(userId, companyId, permission.name(), 
                        false, "시간 제한 위반: " + LocalTime.now(), request);
                    return false;
                }
            }
            
            // 디바이스 제한 확인
            if (restriction.getAllowedDevices() != null && !restriction.getAllowedDevices().isEmpty()) {
                if (!isDeviceAllowed(userAgent, restriction.getAllowedDevices())) {
                    logger.warn("Device access denied for user {} with user agent: {}", userId, userAgent);
                    securityAuditService.logPermissionEvent(userId, companyId, permission.name(), 
                        false, "디바이스 제한 위반", request);
                    return false;
                }
            }
        }
        
        // 권한 허용 로깅
        securityAuditService.logPermissionEvent(userId, companyId, permission.name(), 
            true, "컨텍스트 기반 권한 허용", request);
        
        return true;
    }
    
    /**
     * 사용자 권한 레벨 변경
     */
    public void changeUserRole(Long userId, Long companyId, RoleLevel newRole, String changedBy) {
        String userKey = generateUserKey(userId, companyId);
        UserRoleInfo currentInfo = userRoleCache.get(userKey);
        
        RoleLevel oldRole = currentInfo != null ? currentInfo.getRoleLevel() : RoleLevel.GUEST;
        
        UserRoleInfo newInfo = new UserRoleInfo(userId, companyId, newRole, changedBy, LocalDateTime.now());
        userRoleCache.put(userKey, newInfo);
        
        // 데이터베이스 업데이트 (실제 구현 필요)
        updateUserRoleInDatabase(userId, companyId, newRole, changedBy);
        
        logger.info("User role changed: userId={}, companyId={}, {} -> {}, changedBy={}", 
                   userId, companyId, oldRole, newRole, changedBy);
    }
    
    /**
     * 컨텍스트 제한 설정
     */
    public void setContextualRestriction(Long userId, Long companyId, ContextualRestriction restriction) {
        String userKey = generateUserKey(userId, companyId);
        contextualRestrictions.put(userKey, restriction);
        
        logger.info("Contextual restriction set for user: userId={}, companyId={}", userId, companyId);
    }
    
    /**
     * 사용자의 모든 권한 목록 반환
     */
    public Set<GrantedAuthority> getUserAuthorities(Long userId, Long companyId) {
        RoleLevel userRole = getUserRoleLevel(userId, companyId);
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // 기본 역할 권한 추가
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRoleName()));
        
        // 해당 권한 레벨에서 사용 가능한 모든 퍼미션 추가
        for (Permission permission : Permission.values()) {
            if (userRole.isEqualOrHigherThan(permission.getMinimumRole())) {
                authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
            }
        }
        
        return authorities;
    }
    
    /**
     * 권한 상속 체계 확인
     */
    public boolean canDelegate(RoleLevel delegator, RoleLevel targetRole) {
        // 자신보다 낮은 권한만 부여 가능
        return delegator.isHigherThan(targetRole);
    }
    
    /**
     * 회사별 권한 통계
     */
    public Map<RoleLevel, Long> getCompanyRoleStatistics(Long companyId) {
        return userRoleCache.values().stream()
            .filter(info -> info.getCompanyId().equals(companyId))
            .collect(Collectors.groupingBy(
                UserRoleInfo::getRoleLevel,
                Collectors.counting()
            ));
    }
    
    /**
     * 권한 변경 이력 조회 (실제 구현에서는 별도 테이블 필요)
     */
    public List<RoleChangeHistory> getRoleChangeHistory(Long userId, Long companyId, int limit) {
        // 모의 구현 - 실제로는 데이터베이스에서 조회
        return new ArrayList<>();
    }
    
    // === 헬퍼 메서드들 ===
    
    private String generateUserKey(Long userId, Long companyId) {
        return userId + ":" + companyId;
    }
    
    private UserRoleInfo loadUserRoleFromDatabase(Long userId, Long companyId) {
        // 실제 구현에서는 데이터베이스에서 조회
        // 현재는 기본값 반환
        return new UserRoleInfo(userId, companyId, RoleLevel.EMPLOYEE, "SYSTEM", LocalDateTime.now());
    }
    
    private void updateUserRoleInDatabase(Long userId, Long companyId, RoleLevel newRole, String changedBy) {
        // 실제 구현에서는 데이터베이스 업데이트
        logger.debug("Would update user role in database: userId={}, companyId={}, role={}", 
                    userId, companyId, newRole);
    }
    
    private boolean isIpAllowed(String clientIp, Set<String> allowedIps) {
        if (clientIp == null) return false;
        
        for (String allowedIp : allowedIps) {
            if (allowedIp.contains("/")) {
                // CIDR 표기법 지원
                if (isIpInCidr(clientIp, allowedIp)) {
                    return true;
                }
            } else {
                // 정확한 IP 매치
                if (clientIp.equals(allowedIp)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isIpInCidr(String ip, String cidr) {
        // CIDR 범위 확인 구현 (간단한 버전)
        // 실제 구현에서는 더 정교한 IP 범위 확인 필요
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            // 간단한 구현 - 실제로는 비트 연산 필요
            if (prefixLength >= 24) {
                String[] ipParts = ip.split("\\.");
                String[] networkParts = networkIp.split("\\.");
                
                for (int i = 0; i < 3; i++) {
                    if (!ipParts[i].equals(networkParts[i])) {
                        return false;
                    }
                }
                return true;
            }
            
            return ip.equals(networkIp);
        } catch (Exception e) {
            logger.warn("CIDR parsing failed for: {}", cidr);
            return false;
        }
    }
    
    private boolean isWithinWorkingHours(WorkingHours workingHours) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(workingHours.getStartTime()) && !now.isAfter(workingHours.getEndTime());
    }
    
    private boolean isDeviceAllowed(String userAgent, Set<String> allowedDevices) {
        if (userAgent == null) return false;
        
        return allowedDevices.stream()
            .anyMatch(allowedDevice -> userAgent.toLowerCase().contains(allowedDevice.toLowerCase()));
    }
    
    // === 내부 클래스들 ===
    
    /**
     * 사용자 권한 정보
     */
    private static class UserRoleInfo {
        private final Long userId;
        private final Long companyId;
        private final RoleLevel roleLevel;
        private final String assignedBy;
        private final LocalDateTime assignedAt;
        
        public UserRoleInfo(Long userId, Long companyId, RoleLevel roleLevel, String assignedBy, LocalDateTime assignedAt) {
            this.userId = userId;
            this.companyId = companyId;
            this.roleLevel = roleLevel;
            this.assignedBy = assignedBy;
            this.assignedAt = assignedAt;
        }
        
        public Long getUserId() { return userId; }
        public Long getCompanyId() { return companyId; }
        public RoleLevel getRoleLevel() { return roleLevel; }
        public String getAssignedBy() { return assignedBy; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
    }
    
    /**
     * 컨텍스트 제한 정보
     */
    public static class ContextualRestriction {
        private Set<String> allowedIps;
        private WorkingHours workingHours;
        private Set<String> allowedDevices;
        private LocalDateTime validUntil;
        
        public ContextualRestriction() {}
        
        public Set<String> getAllowedIps() { return allowedIps; }
        public void setAllowedIps(Set<String> allowedIps) { this.allowedIps = allowedIps; }
        
        public WorkingHours getWorkingHours() { return workingHours; }
        public void setWorkingHours(WorkingHours workingHours) { this.workingHours = workingHours; }
        
        public Set<String> getAllowedDevices() { return allowedDevices; }
        public void setAllowedDevices(Set<String> allowedDevices) { this.allowedDevices = allowedDevices; }
        
        public LocalDateTime getValidUntil() { return validUntil; }
        public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    }
    
    /**
     * 근무 시간 정보
     */
    public static class WorkingHours {
        private LocalTime startTime;
        private LocalTime endTime;
        private Set<java.time.DayOfWeek> workingDays;
        
        public WorkingHours() {}
        
        public WorkingHours(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.workingDays = EnumSet.of(
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY
            );
        }
        
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        
        public Set<java.time.DayOfWeek> getWorkingDays() { return workingDays; }
        public void setWorkingDays(Set<java.time.DayOfWeek> workingDays) { this.workingDays = workingDays; }
    }
    
    /**
     * 권한 변경 이력
     */
    public static class RoleChangeHistory {
        private final LocalDateTime timestamp;
        private final RoleLevel oldRole;
        private final RoleLevel newRole;
        private final String changedBy;
        private final String reason;
        
        public RoleChangeHistory(LocalDateTime timestamp, RoleLevel oldRole, RoleLevel newRole, 
                               String changedBy, String reason) {
            this.timestamp = timestamp;
            this.oldRole = oldRole;
            this.newRole = newRole;
            this.changedBy = changedBy;
            this.reason = reason;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public RoleLevel getOldRole() { return oldRole; }
        public RoleLevel getNewRole() { return newRole; }
        public String getChangedBy() { return changedBy; }
        public String getReason() { return reason; }
    }
}