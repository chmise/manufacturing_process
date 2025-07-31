package com.u1mobis.dashboard_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 스마트 초대 시스템
 * QR 코드 및 NFC를 통한 안전한 일회용 초대 링크 생성
 */
@Service
public class SmartInvitationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartInvitationService.class);
    
    @Value("${smart.invitation.base-url:http://localhost:3000}")
    private String baseUrl;
    
    @Value("${smart.invitation.validity-minutes:30}")
    private int validityMinutes;
    
    @Value("${smart.invitation.max-uses:1}")
    private int maxUses;
    
    @Value("${smart.invitation.cleanup-interval-minutes:15}")
    private int cleanupIntervalMinutes;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 초대 정보 저장소
    private final ConcurrentHashMap<String, InvitationInfo> invitations = new ConcurrentHashMap<>();
    
    // 암호화 키 (서비스 시작시 생성)
    private final SecretKey encryptionKey;
    
    public SmartInvitationService() {
        try {
            this.encryptionKey = generateEncryptionKey();
            logger.info("Smart invitation service initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize smart invitation service", e);
            throw new RuntimeException("Smart invitation service initialization failed", e);
        }
    }
    
    /**
     * 스마트 초대 생성
     */
    public SmartInvitation createInvitation(Long companyId, String companyName, String companyCode, 
                                          InvitationType type, String createdBy) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(validityMinutes);
            
            // 고유한 초대 ID 생성
            String invitationId = generateInvitationId();
            
            // 초대 페이로드 생성
            Map<String, Object> payload = new HashMap<>();
            payload.put("invitationId", invitationId);
            payload.put("companyId", companyId);
            payload.put("companyName", companyName);
            payload.put("companyCode", companyCode);
            payload.put("type", type.name());
            payload.put("createdBy", createdBy);
            payload.put("createdAt", now.toEpochSecond(ZoneOffset.UTC));
            payload.put("expiresAt", expiresAt.toEpochSecond(ZoneOffset.UTC));
            payload.put("maxUses", maxUses);
            payload.put("version", "1.0");
            
            // JSON 직렬화 및 암호화
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String encryptedPayload = encryptPayload(jsonPayload);
            
            // Base64 URL 안전 인코딩
            String invitationToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(encryptedPayload.getBytes(StandardCharsets.UTF_8));
            
            // 초대 링크 생성
            String invitationLink = generateInvitationLink(invitationToken, type);
            
            // QR 코드 생성
            String qrCode = generateQRCode(invitationLink);
            
            // NFC 데이터 생성
            String nfcData = generateNFCData(invitationToken);
            
            // 초대 정보 저장
            InvitationInfo invitationInfo = new InvitationInfo(
                invitationId, companyId, companyName, companyCode, type, 
                createdBy, now, expiresAt, maxUses, 0, invitationToken
            );
            invitations.put(invitationId, invitationInfo);
            
            logger.info("Created smart invitation for company: {} (type: {}, expires: {})", 
                       companyName, type, expiresAt);
            
            return new SmartInvitation(
                invitationId, invitationLink, qrCode, nfcData, 
                expiresAt, maxUses, type
            );
            
        } catch (Exception e) {
            logger.error("Failed to create smart invitation for company ID: {}", companyId, e);
            throw new RuntimeException("초대 생성에 실패했습니다", e);
        }
    }
    
    /**
     * 초대 검증 및 사용
     */
    public InvitationValidationResult validateAndUseInvitation(String invitationToken) {
        try {
            if (invitationToken == null || invitationToken.trim().isEmpty()) {
                return InvitationValidationResult.invalid("초대 토큰이 비어있습니다");
            }
            
            // Base64 디코딩
            byte[] encryptedBytes = Base64.getUrlDecoder().decode(invitationToken);
            String encryptedPayload = new String(encryptedBytes, StandardCharsets.UTF_8);
            
            // 복호화
            String jsonPayload = decryptPayload(encryptedPayload);
            
            // JSON 파싱
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(jsonPayload, Map.class);
            
            String invitationId = (String) payload.get("invitationId");
            Long expiresAt = ((Number) payload.get("expiresAt")).longValue();
            
            // 만료 시간 확인
            if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) > expiresAt) {
                return InvitationValidationResult.invalid("초대가 만료되었습니다");
            }
            
            // 초대 정보 조회
            InvitationInfo invitationInfo = invitations.get(invitationId);
            if (invitationInfo == null) {
                return InvitationValidationResult.invalid("유효하지 않은 초대입니다");
            }
            
            // 사용 횟수 확인
            if (invitationInfo.getUsedCount() >= invitationInfo.getMaxUses()) {
                return InvitationValidationResult.invalid("초대 사용 횟수가 초과되었습니다");
            }
            
            // 사용 횟수 증가
            invitationInfo.incrementUsedCount();
            
            // 모든 사용 완료시 초대 제거
            if (invitationInfo.getUsedCount() >= invitationInfo.getMaxUses()) {
                invitations.remove(invitationId);
                logger.info("Invitation fully used and removed: {}", invitationId);
            }
            
            logger.info("Successfully validated invitation for company: {} (uses: {}/{})", 
                       invitationInfo.getCompanyName(), 
                       invitationInfo.getUsedCount(), 
                       invitationInfo.getMaxUses());
            
            return InvitationValidationResult.valid(
                invitationInfo.getCompanyId(),
                invitationInfo.getCompanyName(),
                invitationInfo.getCompanyCode(),
                invitationInfo.getType(),
                payload
            );
            
        } catch (Exception e) {
            logger.warn("Invitation validation failed: {}", e.getMessage());
            return InvitationValidationResult.invalid("초대 검증에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 회사의 활성 초대 목록 조회
     */
    public java.util.List<SmartInvitation> getActiveInvitations(Long companyId) {
        LocalDateTime now = LocalDateTime.now();
        
        return invitations.values().stream()
            .filter(invitation -> invitation.getCompanyId().equals(companyId))
            .filter(invitation -> invitation.getExpiresAt().isAfter(now))
            .filter(invitation -> invitation.getUsedCount() < invitation.getMaxUses())
            .map(invitation -> new SmartInvitation(
                invitation.getInvitationId(),
                generateInvitationLink(invitation.getInvitationToken(), invitation.getType()),
                generateQRCode(generateInvitationLink(invitation.getInvitationToken(), invitation.getType())),
                generateNFCData(invitation.getInvitationToken()),
                invitation.getExpiresAt(),
                invitation.getMaxUses() - invitation.getUsedCount(),
                invitation.getType()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 초대 취소
     */
    public boolean cancelInvitation(String invitationId, Long companyId) {
        InvitationInfo invitation = invitations.get(invitationId);
        if (invitation != null && invitation.getCompanyId().equals(companyId)) {
            invitations.remove(invitationId);
            logger.info("Cancelled invitation: {} for company: {}", invitationId, invitation.getCompanyName());
            return true;
        }
        return false;
    }
    
    /**
     * 주기적 만료된 초대 정리
     */
    @Scheduled(fixedRateString = "#{${smart.invitation.cleanup-interval-minutes:15} * 60 * 1000}")
    public void cleanupExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now();
        
        invitations.entrySet().removeIf(entry -> {
            InvitationInfo invitation = entry.getValue();
            if (invitation.getExpiresAt().isBefore(now)) {
                logger.debug("Removed expired invitation: {} for company: {}", 
                           entry.getKey(), invitation.getCompanyName());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 페이로드 암호화
     */
    private String encryptPayload(String payload) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        // IV 생성
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        // IV + 암호화된 데이터 결합
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    /**
     * 페이로드 복호화
     */
    private String decryptPayload(String encryptedPayload) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedPayload);
        
        // IV 추출
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, 16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // 암호화된 데이터 추출
        byte[] encryptedBytes = new byte[combined.length - 16];
        System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivSpec);
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 암호화 키 생성
     */
    private SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
    
    /**
     * 초대 ID 생성
     */
    private String generateInvitationId() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * 초대 링크 생성
     */
    private String generateInvitationLink(String invitationToken, InvitationType type) {
        String endpoint = switch (type) {
            case EMPLOYEE_REGISTRATION -> "/register";
            case GUEST_ACCESS -> "/guest";
            case ADMIN_INVITATION -> "/admin-join";
            case DEMO_ACCESS -> "/demo";
        };
        
        return String.format("%s%s?invitation=%s", baseUrl, endpoint, invitationToken);
    }
    
    /**
     * QR 코드 생성
     */
    private String generateQRCode(String data) {
        // 실제 구현에서는 QR 코드 라이브러리 사용 (예: ZXing)
        // 현재는 모의 QR 코드 데이터 반환
        try {
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
            return String.format("data:image/png;base64,QR_%s", encodedData.substring(0, Math.min(50, encodedData.length())));
        } catch (Exception e) {
            logger.warn("QR code generation failed: {}", e.getMessage());
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        }
    }
    
    /**
     * NFC 데이터 생성
     */
    private String generateNFCData(String invitationToken) {
        // NFC NDEF 레코드 형식으로 데이터 생성
        Map<String, Object> nfcData = new HashMap<>();
        nfcData.put("type", "smart-invitation");
        nfcData.put("token", invitationToken);
        nfcData.put("timestamp", System.currentTimeMillis());
        
        try {
            return objectMapper.writeValueAsString(nfcData);
        } catch (Exception e) {
            logger.warn("NFC data generation failed: {}", e.getMessage());
            return "{\"type\":\"smart-invitation\",\"token\":\"" + invitationToken + "\"}";
        }
    }
    
    /**
     * 초대 타입 열거형
     */
    public enum InvitationType {
        EMPLOYEE_REGISTRATION,  // 직원 회원가입
        GUEST_ACCESS,          // 게스트 접근
        ADMIN_INVITATION,      // 관리자 초대
        DEMO_ACCESS           // 데모 접근
    }
    
    /**
     * 초대 정보 클래스
     */
    private static class InvitationInfo {
        private final String invitationId;
        private final Long companyId;
        private final String companyName;
        private final String companyCode;
        private final InvitationType type;
        private final String createdBy;
        private final LocalDateTime createdAt;
        private final LocalDateTime expiresAt;
        private final int maxUses;
        private volatile int usedCount;
        private final String invitationToken;
        
        public InvitationInfo(String invitationId, Long companyId, String companyName, String companyCode,
                            InvitationType type, String createdBy, LocalDateTime createdAt, LocalDateTime expiresAt,
                            int maxUses, int usedCount, String invitationToken) {
            this.invitationId = invitationId;
            this.companyId = companyId;
            this.companyName = companyName;
            this.companyCode = companyCode;
            this.type = type;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.maxUses = maxUses;
            this.usedCount = usedCount;
            this.invitationToken = invitationToken;
        }
        
        public synchronized void incrementUsedCount() {
            this.usedCount++;
        }
        
        // Getters
        public String getInvitationId() { return invitationId; }
        public Long getCompanyId() { return companyId; }
        public String getCompanyName() { return companyName; }
        public String getCompanyCode() { return companyCode; }
        public InvitationType getType() { return type; }
        public String getCreatedBy() { return createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public int getMaxUses() { return maxUses; }
        public int getUsedCount() { return usedCount; }
        public String getInvitationToken() { return invitationToken; }
    }
    
    /**
     * 스마트 초대 클래스
     */
    public static class SmartInvitation {
        private final String invitationId;
        private final String invitationLink;
        private final String qrCode;
        private final String nfcData;
        private final LocalDateTime expiresAt;
        private final int remainingUses;
        private final InvitationType type;
        
        public SmartInvitation(String invitationId, String invitationLink, String qrCode, String nfcData,
                             LocalDateTime expiresAt, int remainingUses, InvitationType type) {
            this.invitationId = invitationId;
            this.invitationLink = invitationLink;
            this.qrCode = qrCode;
            this.nfcData = nfcData;
            this.expiresAt = expiresAt;
            this.remainingUses = remainingUses;
            this.type = type;
        }
        
        // Getters
        public String getInvitationId() { return invitationId; }
        public String getInvitationLink() { return invitationLink; }
        public String getQrCode() { return qrCode; }
        public String getNfcData() { return nfcData; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public int getRemainingUses() { return remainingUses; }
        public InvitationType getType() { return type; }
    }
    
    /**
     * 초대 검증 결과 클래스
     */
    public static class InvitationValidationResult {
        private final boolean valid;
        private final String message;
        private final Long companyId;
        private final String companyName;
        private final String companyCode;
        private final InvitationType type;
        private final Map<String, Object> payload;
        
        private InvitationValidationResult(boolean valid, String message, Long companyId, String companyName,
                                         String companyCode, InvitationType type, Map<String, Object> payload) {
            this.valid = valid;
            this.message = message;
            this.companyId = companyId;
            this.companyName = companyName;
            this.companyCode = companyCode;
            this.type = type;
            this.payload = payload;
        }
        
        public static InvitationValidationResult valid(Long companyId, String companyName, String companyCode,
                                                     InvitationType type, Map<String, Object> payload) {
            return new InvitationValidationResult(true, "초대가 유효합니다", companyId, companyName, companyCode, type, payload);
        }
        
        public static InvitationValidationResult invalid(String message) {
            return new InvitationValidationResult(false, message, null, null, null, null, null);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Long getCompanyId() { return companyId; }
        public String getCompanyName() { return companyName; }
        public String getCompanyCode() { return companyCode; }
        public InvitationType getType() { return type; }
        public Map<String, Object> getPayload() { return payload; }
    }
}