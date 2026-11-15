package com.u1mobis.dashboard_backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    // HS256은 256비트(32바이트) 이상의 키가 필요하다
    private static final int MIN_SECRET_BYTES = 32;

    // 기본값 없음: JWT_SECRET 환경변수(jwt.secret)로 반드시 넣어야 한다
    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400}") // 24시간 (초 단위)
    private int jwtExpiration;

    @Value("${jwt.refresh-expiration:604800}") // 7일 (초 단위)
    private int refreshExpiration;

    private SecretKey signingKey;

    @PostConstruct
    void initSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT 서명 키가 없습니다. JWT_SECRET 환경변수에 32바이트 이상의 무작위 문자열을 넣어주세요.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET이 너무 짧습니다. 32바이트 이상이어야 합니다. (현재 " + keyBytes.length + "바이트)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
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

    // 토큰의 모든 클레임 정보 가져오기 (서명이 틀리거나 만료되면 예외)
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 액세스 토큰 생성
    public String generateToken(String username, Long userId, Long companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put(CLAIM_TYPE, TYPE_ACCESS);
        return buildToken(claims, username, jwtExpiration);
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, TYPE_REFRESH);
        return buildToken(claims, username, refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, int expirationSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationSeconds * 1000L))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * API 호출용 토큰 검증. 리프레시 토큰은 API 호출에 쓸 수 없다.
     */
    public boolean validateAccessToken(String token, String username) {
        return validate(token, username, false);
    }

    /**
     * 토큰 갱신용 검증. 리프레시 토큰만 허용한다.
     */
    public boolean validateRefreshToken(String token, String username) {
        return validate(token, username, true);
    }

    private boolean validate(String token, String username, boolean expectRefresh) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            boolean isRefresh = TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
            return username != null
                    && username.equals(claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date())
                    && isRefresh == expectRefresh;
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
