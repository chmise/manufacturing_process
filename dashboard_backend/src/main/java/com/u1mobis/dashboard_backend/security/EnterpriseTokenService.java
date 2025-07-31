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
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 암호화된 기업 토큰 서비스
 * JWT + AES 하이브리드 암호화를 사용한 회전형 기업 인증 토큰
 */
@Service
public class EnterpriseTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnterpriseTokenService.class);
    
    @Value("${enterprise.token.rotation.enabled:true}")
    private boolean tokenRotationEnabled;
    
    @Value("${enterprise.token.rotation.interval-hours:24}")
    private int rotationIntervalHours;
    
    @Value("${enterprise.token.validity-hours:72}")
    private int tokenValidityHours;
    
    @Value("${enterprise.token.encryption.algorithm:AES/CBC/PKCS5Padding}")
    private String encryptionAlgorithm;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 기업별 토큰 저장소
    private final ConcurrentHashMap<Long, EnterpriseTokenInfo> enterpriseTokens = new ConcurrentHashMap<>();
    
    // 암호화 키 저장소 (키 로테이션 지원)
    private final ConcurrentHashMap<String, SecretKey> encryptionKeys = new ConcurrentHashMap<>();
    
    private volatile String currentKeyId;
    
    public EnterpriseTokenService() {
        initializeEncryptionKeys();
    }
    
    /**
     * 암호화 키 초기화
     */
    private void initializeEncryptionKeys() {
        try {
            String keyId = generateKeyId();
            SecretKey key = generateEncryptionKey();
            
            encryptionKeys.put(keyId, key);
            currentKeyId = keyId;
            
            logger.info("Enterprise token encryption initialized with key ID: {}", keyId);
        } catch (Exception e) {
            logger.error("Failed to initialize enterprise token encryption", e);
            throw new RuntimeException("Enterprise token service initialization failed", e);
        }
    }
    
    /**
     * 기업 토큰 생성
     */
    public EnterpriseToken generateEnterpriseToken(Long companyId, String companyName, String companyCode) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusHours(tokenValidityHours);
            
            // 토큰 페이로드 생성
            Map<String, Object> payload = new HashMap<>();
            payload.put("companyId", companyId);
            payload.put("companyName", companyName);
            payload.put("companyCode", companyCode);
            payload.put("issuedAt", now.toEpochSecond(ZoneOffset.UTC));
            payload.put("expiresAt", expiresAt.toEpochSecond(ZoneOffset.UTC));
            payload.put("tokenId", generateTokenId());
            payload.put("keyId", currentKeyId);
            payload.put("version", "2.0");
            
            // JSON 직렬화
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            // AES 암호화
            String encryptedPayload = encryptPayload(jsonPayload, currentKeyId);
            
            // Base64 인코딩
            String tokenValue = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(encryptedPayload.getBytes(StandardCharsets.UTF_8));
            
            // 토큰 정보 저장
            EnterpriseTokenInfo tokenInfo = new EnterpriseTokenInfo(
                companyId, tokenValue, now, expiresAt, currentKeyId
            );
            enterpriseTokens.put(companyId, tokenInfo);
            
            logger.info("Generated enterprise token for company: {} (ID: {})", companyName, companyId);
            
            return new EnterpriseToken(tokenValue, expiresAt, generateQRCode(tokenValue));
            
        } catch (Exception e) {
            logger.error("Failed to generate enterprise token for company ID: {}", companyId, e);
            throw new RuntimeException("토큰 생성에 실패했습니다", e);
        }
    }
    
    /**
     * 기업 토큰 검증
     */
    public TokenValidationResult validateEnterpriseToken(String tokenValue) {
        try {
            if (tokenValue == null || tokenValue.trim().isEmpty()) {
                return TokenValidationResult.invalid("토큰이 비어있습니다");
            }
            
            // Base64 디코딩
            byte[] encryptedBytes = Base64.getUrlDecoder().decode(tokenValue);
            String encryptedPayload = new String(encryptedBytes, StandardCharsets.UTF_8);
            
            // 복호화 시도 (현재 키부터)
            String jsonPayload = null;
            String usedKeyId = null;
            
            // 현재 키로 시도
            try {
                jsonPayload = decryptPayload(encryptedPayload, currentKeyId);
                usedKeyId = currentKeyId;
            } catch (Exception e) {
                // 이전 키들로 시도
                for (Map.Entry<String, SecretKey> entry : encryptionKeys.entrySet()) {
                    if (!entry.getKey().equals(currentKeyId)) {
                        try {
                            jsonPayload = decryptPayload(encryptedPayload, entry.getKey());
                            usedKeyId = entry.getKey();
                            break;
                        } catch (Exception ignored) {
                            // 다음 키로 시도
                        }
                    }
                }
            }
            
            if (jsonPayload == null) {
                return TokenValidationResult.invalid("토큰 복호화에 실패했습니다");
            }
            
            // JSON 파싱
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(jsonPayload, Map.class);
            
            // 만료 시간 확인
            Long expiresAt = ((Number) payload.get("expiresAt")).longValue();
            if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) > expiresAt) {
                return TokenValidationResult.invalid("토큰이 만료되었습니다");
            }
            
            // 토큰 정보 추출
            Long companyId = ((Number) payload.get("companyId")).longValue();
            String companyName = (String) payload.get("companyName");
            String companyCode = (String) payload.get("companyCode");
            
            logger.info("Successfully validated enterprise token for company: {} (ID: {})", companyName, companyId);
            
            return TokenValidationResult.valid(companyId, companyName, companyCode, payload);
            
        } catch (Exception e) {
            logger.warn("Enterprise token validation failed: {}", e.getMessage());
            return TokenValidationResult.invalid("토큰 검증에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 기업 토큰 갱신
     */
    public EnterpriseToken refreshEnterpriseToken(Long companyId) {
        EnterpriseTokenInfo currentToken = enterpriseTokens.get(companyId);
        if (currentToken == null) {
            throw new RuntimeException("갱신할 토큰을 찾을 수 없습니다");
        }
        
        // 기존 토큰 검증
        TokenValidationResult validation = validateEnterpriseToken(currentToken.getTokenValue());
        if (!validation.isValid()) {
            throw new RuntimeException("현재 토큰이 유효하지 않습니다");
        }
        
        // 새 토큰 생성
        return generateEnterpriseToken(companyId, validation.getCompanyName(), validation.getCompanyCode());
    }
    
    /**
     * 주기적 토큰 로테이션
     */
    @Scheduled(fixedRateString = "#{${enterprise.token.rotation.interval-hours:24} * 60 * 60 * 1000}")
    public void rotateTokens() {
        if (!tokenRotationEnabled) {
            return;
        }
        
        try {
            logger.info("Starting enterprise token rotation");
            
            // 새 암호화 키 생성
            String newKeyId = generateKeyId();
            SecretKey newKey = generateEncryptionKey();
            encryptionKeys.put(newKeyId, newKey);
            
            String oldKeyId = currentKeyId;
            currentKeyId = newKeyId;
            
            logger.info("Enterprise token encryption key rotated: {} -> {}", oldKeyId, newKeyId);
            
            // 만료된 토큰 정리
            cleanupExpiredTokens();
            
            // 오래된 키 정리 (7일 이후)
            cleanupOldKeys();
            
        } catch (Exception e) {
            logger.error("Failed to rotate enterprise tokens", e);
        }
    }
    
    /**
     * 만료된 토큰 정리
     */
    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        enterpriseTokens.entrySet().removeIf(entry -> {
            EnterpriseTokenInfo tokenInfo = entry.getValue();
            if (tokenInfo.getExpiresAt().isBefore(now)) {
                logger.debug("Removed expired enterprise token for company ID: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 오래된 키 정리
     */
    private void cleanupOldKeys() {
        // 7일 이전 키 삭제 로직
        // 실제 구현에서는 키 생성 시간을 추적해야 함
        if (encryptionKeys.size() > 3) {
            // 최신 3개 키만 유지
            String[] keyIds = encryptionKeys.keySet().toArray(new String[0]);
            for (int i = 0; i < keyIds.length - 3; i++) {
                String oldKeyId = keyIds[i];
                if (!oldKeyId.equals(currentKeyId)) {
                    encryptionKeys.remove(oldKeyId);
                    logger.info("Removed old encryption key: {}", oldKeyId);
                }
            }
        }
    }
    
    /**
     * 페이로드 암호화
     */
    private String encryptPayload(String payload, String keyId) throws Exception {
        SecretKey key = encryptionKeys.get(keyId);
        if (key == null) {
            throw new RuntimeException("Encryption key not found: " + keyId);
        }
        
        Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
        
        // IV 생성
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
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
    private String decryptPayload(String encryptedPayload, String keyId) throws Exception {
        SecretKey key = encryptionKeys.get(keyId);
        if (key == null) {
            throw new RuntimeException("Decryption key not found: " + keyId);
        }
        
        byte[] combined = Base64.getDecoder().decode(encryptedPayload);
        
        // IV 추출
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, 16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // 암호화된 데이터 추출
        byte[] encryptedBytes = new byte[combined.length - 16];
        System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);
        
        Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 암호화 키 생성
     */
    private SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // AES-256
        return keyGenerator.generateKey();
    }
    
    /**
     * 키 ID 생성
     */
    private String generateKeyId() {
        return "key-" + System.currentTimeMillis() + "-" + secureRandom.nextInt(1000);
    }
    
    /**
     * 토큰 ID 생성
     */
    private String generateTokenId() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * QR 코드 생성
     */
    private String generateQRCode(String tokenValue) {
        // QR 코드 라이브러리를 사용하여 실제 QR 코드 생성
        // 현재는 모의 구현
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
    }
    
    /**
     * 기업 토큰 정보 클래스
     */
    private static class EnterpriseTokenInfo {
        private final Long companyId;
        private final String tokenValue;
        private final LocalDateTime issuedAt;
        private final LocalDateTime expiresAt;
        private final String keyId;
        
        public EnterpriseTokenInfo(Long companyId, String tokenValue, LocalDateTime issuedAt, 
                                 LocalDateTime expiresAt, String keyId) {
            this.companyId = companyId;
            this.tokenValue = tokenValue;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.keyId = keyId;
        }
        
        public Long getCompanyId() { return companyId; }
        public String getTokenValue() { return tokenValue; }
        public LocalDateTime getIssuedAt() { return issuedAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public String getKeyId() { return keyId; }
    }
    
    /**
     * 기업 토큰 클래스
     */
    public static class EnterpriseToken {
        private final String tokenValue;
        private final LocalDateTime expiresAt;
        private final String qrCode;
        
        public EnterpriseToken(String tokenValue, LocalDateTime expiresAt, String qrCode) {
            this.tokenValue = tokenValue;
            this.expiresAt = expiresAt;
            this.qrCode = qrCode;
        }
        
        public String getTokenValue() { return tokenValue; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public String getQrCode() { return qrCode; }
    }
    
    /**
     * 토큰 검증 결과 클래스
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String message;
        private final Long companyId;
        private final String companyName;
        private final String companyCode;
        private final Map<String, Object> payload;
        
        private TokenValidationResult(boolean valid, String message, Long companyId, 
                                    String companyName, String companyCode, Map<String, Object> payload) {
            this.valid = valid;
            this.message = message;
            this.companyId = companyId;
            this.companyName = companyName;
            this.companyCode = companyCode;
            this.payload = payload;
        }
        
        public static TokenValidationResult valid(Long companyId, String companyName, 
                                                String companyCode, Map<String, Object> payload) {
            return new TokenValidationResult(true, "토큰이 유효합니다", companyId, companyName, companyCode, payload);
        }
        
        public static TokenValidationResult invalid(String message) {
            return new TokenValidationResult(false, message, null, null, null, null);
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Long getCompanyId() { return companyId; }
        public String getCompanyName() { return companyName; }
        public String getCompanyCode() { return companyCode; }
        public Map<String, Object> getPayload() { return payload; }
    }
}