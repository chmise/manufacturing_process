package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 보안 감사 로그 엔티티
 * 모든 보안 관련 이벤트를 추적하고 기록
 */
@Entity
@Table(name = "security_audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user_id", columnList = "userId"),
    @Index(name = "idx_audit_company_id", columnList = "companyId"),
    @Index(name = "idx_audit_event_type", columnList = "eventType"),
    @Index(name = "idx_audit_risk_level", columnList = "riskLevel")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 이벤트 기본 정보
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    
    @Column(nullable = false, length = 1000)
    private String eventDescription;
    
    // 사용자 및 회사 정보
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(length = 100)
    private String username;
    
    @Column(length = 200)
    private String companyName;
    
    // 요청 정보
    @Column(name = "client_ip", length = 45)
    private String clientIp;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_uri", length = 500)
    private String requestUri;
    
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    // 세션 및 보안 정보
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "risk_score")
    private Integer riskScore;
    
    @Column(name = "device_fingerprint", length = 200)
    private String deviceFingerprint;
    
    @Column(name = "geolocation", length = 100)
    private String geolocation;
    
    // 추가 컨텍스트 정보 (JSON 형태)
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
    
    // 결과 정보
    @Column(nullable = false)
    private Boolean success;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    // 시간 정보
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    // 감사 관련
    @Column(name = "audit_trail_id", length = 100)
    private String auditTrailId;
    
    @Column(name = "correlation_id", length = 100)
    private String correlationId;
    
    /**
     * 이벤트 타입 열거형
     */
    public enum EventType {
        // 인증 관련
        LOGIN_ATTEMPT("로그인 시도"),
        LOGIN_SUCCESS("로그인 성공"),
        LOGIN_FAILURE("로그인 실패"),
        LOGOUT("로그아웃"),
        
        // 회원가입 및 회사 등록
        USER_REGISTRATION("사용자 등록"),
        COMPANY_REGISTRATION("회사 등록"),
        INVITATION_CREATED("초대 생성"),
        INVITATION_USED("초대 사용"),
        
        // 권한 관련
        PERMISSION_GRANTED("권한 부여"),
        PERMISSION_DENIED("권한 거부"),
        ROLE_CHANGED("역할 변경"),
        PRIVILEGE_ESCALATION("권한 상승"),
        
        // 보안 이벤트
        SECURITY_VIOLATION("보안 위반"),
        SUSPICIOUS_ACTIVITY("의심스러운 활동"),
        BRUTE_FORCE_ATTACK("브루트포스 공격"),
        ACCOUNT_LOCKED("계정 잠금"),
        ACCOUNT_UNLOCKED("계정 잠금 해제"),
        
        // 데이터 접근
        DATA_ACCESS("데이터 접근"),
        DATA_MODIFICATION("데이터 수정"),
        SENSITIVE_DATA_ACCESS("민감 데이터 접근"),
        DATA_EXPORT("데이터 내보내기"),
        
        // 시스템 관리
        SYSTEM_CONFIG_CHANGE("시스템 설정 변경"),
        SECURITY_CONFIG_CHANGE("보안 설정 변경"),
        PASSWORD_CHANGE("비밀번호 변경"),
        PASSWORD_RESET("비밀번호 재설정"),
        
        // 디바이스 및 세션
        NEW_DEVICE_LOGIN("새 디바이스 로그인"),
        DEVICE_TRUSTED("디바이스 신뢰 등록"),
        SESSION_EXPIRED("세션 만료"),
        CONCURRENT_SESSION("동시 세션"),
        
        // 기타
        API_ACCESS("API 접근"),
        RATE_LIMIT_EXCEEDED("요청 한도 초과"),
        COMPLIANCE_VIOLATION("규정 위반"),
        SECURITY_SCAN("보안 스캔");
        
        private final String description;
        
        EventType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 위험도 레벨 열거형
     */
    public enum RiskLevel {
        LOW("낮음", 0),
        MEDIUM("보통", 1),
        HIGH("높음", 2),
        CRITICAL("치명적", 3);
        
        private final String description;
        private final int level;
        
        RiskLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    /**
     * 빠른 로그 생성을 위한 빌더 메서드들
     */
    public static SecurityAuditLogBuilder loginAttempt(String username, String clientIp, boolean success) {
        return SecurityAuditLog.builder()
            .eventType(success ? EventType.LOGIN_SUCCESS : EventType.LOGIN_FAILURE)
            .riskLevel(success ? RiskLevel.LOW : RiskLevel.MEDIUM)
            .eventDescription(success ? "사용자 로그인 성공" : "사용자 로그인 실패")
            .username(username)
            .clientIp(clientIp)
            .success(success);
    }
    
    public static SecurityAuditLogBuilder securityViolation(Long userId, String description, RiskLevel riskLevel) {
        return SecurityAuditLog.builder()
            .eventType(EventType.SECURITY_VIOLATION)
            .riskLevel(riskLevel)
            .eventDescription(description)
            .userId(userId)
            .success(false);
    }
    
    public static SecurityAuditLogBuilder dataAccess(Long userId, String resource, boolean success) {
        return SecurityAuditLog.builder()
            .eventType(EventType.DATA_ACCESS)
            .riskLevel(RiskLevel.LOW)
            .eventDescription("데이터 접근: " + resource)
            .userId(userId)
            .success(success);
    }
}