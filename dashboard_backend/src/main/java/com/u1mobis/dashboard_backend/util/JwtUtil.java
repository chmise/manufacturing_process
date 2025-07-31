package com.u1mobis.dashboard_backend.util;

import com.u1mobis.dashboard_backend.security.JwtKeyManager;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Autowired
    private JwtKeyManager jwtKeyManager;
    
    @Value("${jwt.expiration:86400}") // 24시간 (초 단위)
    private int jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800}") // 7일 (초 단위)
    private int refreshExpiration;
    
    @Value("${jwt.issuer:u1mobis-dashboard}")
    private String issuer;
    
    private SecretKey getSigningKey() {
        return jwtKeyManager.getCurrentKey();
    }
    
    private SecretKey getSigningKey(int keyId) {
        SecretKey key = jwtKeyManager.getKeyById(keyId);
        if (key == null) {
            logger.warn("Key not found for ID: {}, using current key", keyId);
            return jwtKeyManager.getCurrentKey();
        }
        return key;
    }
    
    // 토큰에서 사용자명 추출
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    // 토큰에서 만료일 추출
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    // 토큰의 모든 클레임 정보 가져오기
    private Claims getAllClaimsFromToken(String token) {
        try {
            // 먼저 현재 키로 시도
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // 현재 키로 실패하면 토큰 헤더에서 키 ID 추출 시도
            try {
                String[] chunks = token.split("\\.");
                if (chunks.length >= 2) {
                    String header = new String(java.util.Base64.getUrlDecoder().decode(chunks[0]));
                    // 간단한 키 ID 추출 (실제로는 JSON 파싱이 필요하지만 여기서는 단순화)
                    // 이전 키들로 재시도하는 로직을 구현할 수 있음
                }
                logger.warn("Token validation failed with current key, token may be signed with older key");
                throw e;
            } catch (Exception ex) {
                logger.error("Failed to parse token header", ex);
                throw e;
            }
        }
    }
    
    // 토큰 만료 확인
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    // 사용자 정보로 토큰 생성
    public String generateToken(String username, Long userId, Long companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        return createToken(claims, username);
    }
    
    // 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createRefreshToken(claims, username);
    }
    
    // 액세스 토큰 생성
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        
        // 보안 클레임 추가
        claims.put("iat", now / 1000);
        claims.put("keyId", jwtKeyManager.getCurrentKeyId());
        claims.put("tokenType", "access");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpiration * 1000L))
                .setId(java.util.UUID.randomUUID().toString()) // JTI for token uniqueness
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // HS512 for stronger security
                .compact();
    }
    
    // 리프레시 토큰 생성
    private String createRefreshToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        
        // 보안 클레임 추가
        claims.put("iat", now / 1000);
        claims.put("keyId", jwtKeyManager.getCurrentKeyId());
        claims.put("tokenType", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpiration * 1000L))
                .setId(java.util.UUID.randomUUID().toString()) // JTI for token uniqueness
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // HS512 for stronger security
                .compact();
    }
    
    // 토큰 유효성 검증
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    // 토큰에서 companyId 추출
    public Long getCompanyIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("companyId", Long.class);
    }
}