package com.u1mobis.dashboard_backend.security;

import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JWT 키 관리 및 로테이션을 담당하는 컴포넌트
 * 보안 강화를 위해 주기적으로 JWT 서명 키를 교체
 */
@Component
public class JwtKeyManager {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtKeyManager.class);
    
    @Value("${jwt.key-rotation.enabled:true}")
    private boolean keyRotationEnabled;
    
    @Value("${jwt.key-rotation.interval-hours:24}")
    private int rotationIntervalHours;
    
    @Value("${jwt.secret:}")
    private String configuredSecret;
    
    // 현재 활성 키 ID
    private final AtomicInteger currentKeyId = new AtomicInteger(0);
    
    // 키 저장소 (키 ID -> SecretKey)
    private final ConcurrentHashMap<Integer, SecretKey> keyStore = new ConcurrentHashMap<>();
    
    // 키 생성 시간 추적
    private final ConcurrentHashMap<Integer, Long> keyGenerationTime = new ConcurrentHashMap<>();
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public JwtKeyManager() {
        initializeKeys();
    }
    
    /**
     * 초기 키 설정
     */
    private void initializeKeys() {
        try {
            SecretKey initialKey;
            
            if (configuredSecret != null && !configuredSecret.isEmpty()) {
                // 설정된 시크릿이 있으면 사용
                initialKey = Keys.hmacShaKeyFor(configuredSecret.getBytes());
                logger.info("JWT key initialized from configuration");
            } else {
                // 없으면 안전한 랜덤 키 생성
                initialKey = generateSecureKey();
                logger.info("JWT key initialized with secure random generation");
            }
            
            int keyId = currentKeyId.get();
            keyStore.put(keyId, initialKey);
            keyGenerationTime.put(keyId, System.currentTimeMillis());
            
            logger.info("JWT Key Manager initialized with key ID: {}", keyId);
            
        } catch (Exception e) {
            logger.error("Failed to initialize JWT keys", e);
            throw new RuntimeException("JWT Key Manager initialization failed", e);
        }
    }
    
    /**
     * 안전한 랜덤 키 생성
     */
    private SecretKey generateSecureKey() {
        byte[] keyBytes = new byte[64]; // 512 bits for HS512
        secureRandom.nextBytes(keyBytes);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 현재 활성 키 반환
     */
    public SecretKey getCurrentKey() {
        int keyId = currentKeyId.get();
        return keyStore.get(keyId);
    }
    
    /**
     * 특정 키 ID로 키 조회 (토큰 검증용)
     */
    public SecretKey getKeyById(int keyId) {
        return keyStore.get(keyId);
    }
    
    /**
     * 현재 키 ID 반환
     */
    public int getCurrentKeyId() {
        return currentKeyId.get();
    }
    
    /**
     * 주기적 키 로테이션 (매일 실행)
     */
    @Scheduled(fixedRateString = "#{${jwt.key-rotation.interval-hours:24} * 60 * 60 * 1000}")
    public void rotateKeys() {
        if (!keyRotationEnabled) {
            return;
        }
        
        try {
            int oldKeyId = currentKeyId.get();
            int newKeyId = oldKeyId + 1;
            
            // 새 키 생성
            SecretKey newKey = generateSecureKey();
            keyStore.put(newKeyId, newKey);
            keyGenerationTime.put(newKeyId, System.currentTimeMillis());
            
            // 현재 키 ID 업데이트
            currentKeyId.set(newKeyId);
            
            logger.info("JWT key rotated: {} -> {}", oldKeyId, newKeyId);
            
            // 오래된 키 정리 (7일 이전 키 삭제)
            cleanupOldKeys();
            
        } catch (Exception e) {
            logger.error("Failed to rotate JWT keys", e);
        }
    }
    
    /**
     * 오래된 키 정리
     */
    private void cleanupOldKeys() {
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        
        keyGenerationTime.entrySet().removeIf(entry -> {
            if (entry.getValue() < sevenDaysAgo) {
                int keyId = entry.getKey();
                keyStore.remove(keyId);
                logger.info("Removed old JWT key with ID: {}", keyId);
                return true;
            }
            return false;
        });
    }
    
    /**
     * 키 상태 정보 반환
     */
    public String getKeyStatus() {
        return String.format("Current Key ID: %d, Total Keys: %d, Rotation Enabled: %s", 
                currentKeyId.get(), keyStore.size(), keyRotationEnabled);
    }
    
    /**
     * 수동 키 로테이션
     */
    public void forceRotation() {
        logger.info("Manual key rotation triggered");
        rotateKeys();
    }
}