package com.u1mobis.dashboard_backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 패스워드 정책 검증 및 관리 서비스
 * NIST 가이드라인과 기업 보안 정책을 준수
 */
@Service
public class PasswordPolicyService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordPolicyService.class);
    
    @Value("${password.policy.min-length:12}")
    private int minLength;
    
    @Value("${password.policy.max-length:128}")
    private int maxLength;
    
    @Value("${password.policy.require-uppercase:true}")
    private boolean requireUppercase;
    
    @Value("${password.policy.require-lowercase:true}")
    private boolean requireLowercase;
    
    @Value("${password.policy.require-digits:true}")
    private boolean requireDigits;
    
    @Value("${password.policy.require-special:true}")
    private boolean requireSpecialChars;
    
    @Value("${password.policy.history-count:5}")
    private int passwordHistoryCount;
    
    @Value("${password.policy.expiry-days:90}")
    private int passwordExpiryDays;
    
    @Value("${password.policy.block-common:true}")
    private boolean blockCommonPasswords;
    
    // 일반적으로 사용되는 약한 패스워드 목록 (실제로는 더 큰 사전이 필요)
    private static final List<String> COMMON_PASSWORDS = List.of(
        "password", "123456", "password123", "admin", "qwerty", "abc123", 
        "welcome", "login", "master", "secret", "root", "user", "test",
        "12345678", "111111", "123123", "dragon", "pass", "monkey"
    );
    
    // 특수문자 패턴
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGITS = Pattern.compile("[0-9]");
    
    /**
     * 패스워드 정책 검증
     */
    public PasswordValidationResult validatePassword(String password, String username) {
        List<String> violations = new ArrayList<>();
        
        // 길이 검증
        if (password.length() < minLength) {
            violations.add(String.format("패스워드는 최소 %d자 이상이어야 합니다.", minLength));
        }
        
        if (password.length() > maxLength) {
            violations.add(String.format("패스워드는 최대 %d자 이하여야 합니다.", maxLength));
        }
        
        // 복잡도 검증
        if (requireUppercase && !UPPERCASE.matcher(password).find()) {
            violations.add("대문자를 포함해야 합니다.");
        }
        
        if (requireLowercase && !LOWERCASE.matcher(password).find()) {
            violations.add("소문자를 포함해야 합니다.");
        }
        
        if (requireDigits && !DIGITS.matcher(password).find()) {
            violations.add("숫자를 포함해야 합니다.");
        }
        
        if (requireSpecialChars && !SPECIAL_CHARS.matcher(password).find()) {
            violations.add("특수문자(!@#$%^&*등)를 포함해야 합니다.");
        }
        
        // 사용자명과 유사성 검증
        if (username != null && !username.isEmpty()) {
            if (password.toLowerCase().contains(username.toLowerCase())) {
                violations.add("패스워드에 사용자명을 포함할 수 없습니다.");
            }
        }
        
        // 일반적인 패스워드 검증
        if (blockCommonPasswords && isCommonPassword(password.toLowerCase())) {
            violations.add("너무 일반적인 패스워드입니다. 다른 패스워드를 선택하세요.");
        }
        
        // 반복 문자 검증
        if (hasRepeatingChars(password)) {
            violations.add("연속된 동일 문자가 너무 많습니다.");
        }
        
        // 순차적 문자 검증
        if (hasSequentialChars(password)) {
            violations.add("순차적인 문자나 숫자는 사용할 수 없습니다.");
        }
        
        boolean isValid = violations.isEmpty();
        int strength = calculatePasswordStrength(password);
        
        if (isValid) {
            logger.info("Password validation successful for user: {}", username);
        } else {
            logger.warn("Password validation failed for user: {}, violations: {}", username, violations.size());
        }
        
        return new PasswordValidationResult(isValid, violations, strength);
    }
    
    /**
     * 패스워드 히스토리 검증
     */
    public boolean isPasswordInHistory(String newPassword, List<String> passwordHistory, PasswordEncoder passwordEncoder) {
        if (passwordHistory == null || passwordHistory.isEmpty()) {
            return false;
        }
        
        for (String historicalPassword : passwordHistory) {
            if (passwordEncoder.matches(newPassword, historicalPassword)) {
                logger.warn("Password matches historical password");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 패스워드 만료 확인
     */
    public boolean isPasswordExpired(LocalDateTime lastPasswordChange) {
        if (lastPasswordChange == null) {
            return true; // 변경 이력이 없으면 만료로 간주
        }
        
        LocalDateTime expiryDate = lastPasswordChange.plusDays(passwordExpiryDays);
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * 패스워드 만료까지 남은 일수
     */
    public long getDaysUntilExpiry(LocalDateTime lastPasswordChange) {
        if (lastPasswordChange == null) {
            return 0;
        }
        
        LocalDateTime expiryDate = lastPasswordChange.plusDays(passwordExpiryDays);
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toDays();
    }
    
    /**
     * 일반적인 패스워드 확인
     */
    private boolean isCommonPassword(String password) {
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }
    
    /**
     * 반복 문자 확인 (3개 이상 연속)
     */
    private boolean hasRepeatingChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i + 1) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 순차적 문자 확인 (abc, 123 등)
     */
    private boolean hasSequentialChars(String password) {
        String seq = password.toLowerCase();
        for (int i = 0; i < seq.length() - 2; i++) {
            char c1 = seq.charAt(i);
            char c2 = seq.charAt(i + 1);
            char c3 = seq.charAt(i + 2);
            
            // 순차적 증가
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
            
            // 순차적 감소
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 패스워드 강도 계산 (0-100)
     */
    private int calculatePasswordStrength(String password) {
        int score = 0;
        
        // 길이 점수 (최대 25점)
        score += Math.min(password.length() * 2, 25);
        
        // 문자 종류별 점수
        if (UPPERCASE.matcher(password).find()) score += 15;
        if (LOWERCASE.matcher(password).find()) score += 15;
        if (DIGITS.matcher(password).find()) score += 15;
        if (SPECIAL_CHARS.matcher(password).find()) score += 15;
        
        // 다양성 보너스
        long uniqueChars = password.chars().distinct().count();
        if (uniqueChars > password.length() * 0.7) {
            score += 15;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * 패스워드 검증 결과 클래스
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> violations;
        private final int strength;
        
        public PasswordValidationResult(boolean valid, List<String> violations, int strength) {
            this.valid = valid;
            this.violations = violations;
            this.strength = strength;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getViolations() {
            return violations;
        }
        
        public int getStrength() {
            return strength;
        }
        
        public String getStrengthLabel() {
            if (strength >= 80) return "매우 강함";
            if (strength >= 60) return "강함";
            if (strength >= 40) return "보통";
            if (strength >= 20) return "약함";
            return "매우 약함";
        }
    }
}