package com.u1mobis.dashboard_backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    // 환경변수에서 가져오거나 기본값 사용
    @Value("${jwt.secret:u1mobis-dashboard-very-secure-secret-key-for-jwt-token-generation}")
    private String secret;
    
    @Value("${jwt.expiration:86400}") // 24시간 (초 단위)
    private int jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800}") // 7일 (초 단위)
    private int refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
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
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
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
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 리프레시 토큰 생성
    private String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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