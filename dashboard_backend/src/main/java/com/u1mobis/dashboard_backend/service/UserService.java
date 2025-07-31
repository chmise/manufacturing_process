package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.JwtResponse;
import com.u1mobis.dashboard_backend.dto.RefreshTokenRequest;
import com.u1mobis.dashboard_backend.dto.UserDTO;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.PasswordHistory;
import com.u1mobis.dashboard_backend.entity.User;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.PasswordHistoryRepository;
import com.u1mobis.dashboard_backend.repository.UserRepository;
import com.u1mobis.dashboard_backend.security.PasswordPolicyService;
import com.u1mobis.dashboard_backend.security.RateLimitService;
import com.u1mobis.dashboard_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordPolicyService passwordPolicyService;
    private final RateLimitService rateLimitService;
    private final SecurityAuditService securityAuditService;
    
    @Autowired
    private HttpServletRequest request;

    @Transactional
    public JwtResponse register(UserDTO userDTO) {
        String clientIp = getClientIpAddress();
        
        try {
            // Rate Limiting 확인은 Filter에서 이미 처리되므로 여기서는 결과만 기록
            
            // 사용자명 중복 확인
            if (userRepository.findByUserName(userDTO.getUsername()) != null) {
                rateLimitService.recordRegistrationAttempt(clientIp, false);
                return new JwtResponse(false, "이미 존재하는 사용자명입니다.");
            }
            
            // 패스워드 정책 검증
            PasswordPolicyService.PasswordValidationResult validationResult = 
                passwordPolicyService.validatePassword(userDTO.getPassword(), userDTO.getUsername());
            
            if (!validationResult.isValid()) {
                String errorMessage = "패스워드 정책 위반: " + String.join(", ", validationResult.getViolations());
                return new JwtResponse(false, errorMessage);
            }
            
            // 회사코드로 회사 찾기
            Company company = null;
            if (userDTO.getCompanyCode() != null && !userDTO.getCompanyCode().trim().isEmpty()) {
                company = companyRepository.findByCompanyCode(userDTO.getCompanyCode().toUpperCase())
                    .orElse(null);
                
                if (company == null) {
                    return new JwtResponse(false, "유효하지 않은 회사코드입니다.");
                }
            } else {
                return new JwtResponse(false, "회사코드가 필요합니다.");
            }
            
            // 새 사용자 생성
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            LocalDateTime now = LocalDateTime.now();
            
            User user = new User(company, userDTO.getUsername(), encodedPassword, 
                               userDTO.getEmail(), userDTO.getBirth(), userDTO.getEmployeeCode(), userDTO.getName());
            
            // 패스워드 보안 필드 설정
            user.setPasswordChangedAt(now);
            user.setPasswordExpiresAt(now.plusDays(90)); // 90일 후 만료
            user.setLoginAttempts(0);
            user.setForcePasswordChange(false);
            
            User savedUser = userRepository.save(user);
            
            // 초기 패스워드를 히스토리에 저장
            PasswordHistory passwordHistory = new PasswordHistory(savedUser, encodedPassword, clientIp);
            passwordHistoryRepository.save(passwordHistory);
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(savedUser.getUserName(), savedUser.getUserId(), savedUser.getCompanyId());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserName());
            
            String companyName = savedUser.getCompany() != null ? savedUser.getCompany().getCompanyName() : "Unknown Company";
            
            log.info("새 사용자 등록 완료: {}", savedUser.getUserName());
            
            // 보안 감사 로깅 - 사용자 등록 성공
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("companyCode", userDTO.getCompanyCode());
            registrationData.put("email", userDTO.getEmail());
            registrationData.put("employeeCode", userDTO.getEmployeeCode());
            
            securityAuditService.logSecurityEvent(
                com.u1mobis.dashboard_backend.entity.SecurityAuditLog.EventType.USER_REGISTRATION,
                com.u1mobis.dashboard_backend.entity.SecurityAuditLog.RiskLevel.LOW,
                "새 사용자 등록: " + savedUser.getUserName(),
                savedUser.getUserId(), savedUser.getCompanyId(), request, true, registrationData
            );
            
            // 성공한 회원가입 기록
            rateLimitService.recordRegistrationAttempt(clientIp, true);
            
            return new JwtResponse(
                true, 
                accessToken, 
                refreshToken,
                savedUser.getUserName(), 
                companyName,
                savedUser.getUserId(),
                savedUser.getCompanyId()
            );
            
        } catch (Exception e) {
            log.error("사용자 등록 중 오류 발생: {}", e.getMessage());
            return new JwtResponse(false, "회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public JwtResponse login(UserDTO userDTO) {
        String clientIp = getClientIpAddress();
        
        try {
            // 사용자 조회
            User user = userRepository.findByUserName(userDTO.getUsername());
            if (user == null) {
                rateLimitService.recordLoginAttempt(clientIp, false);
                
                // 보안 감사 로깅 - 존재하지 않는 사용자 로그인 시도
                securityAuditService.logLoginAttempt(userDTO.getUsername(), clientIp, false, 
                    "존재하지 않는 사용자", request);
                
                return new JwtResponse(false, "사용자를 찾을 수 없습니다.");
            }
            
            // 계정 잠금 확인
            if (isAccountLocked(user)) {
                rateLimitService.recordLoginAttempt(clientIp, false);
                
                // 보안 감사 로깅 - 잠긴 계정 로그인 시도
                securityAuditService.logLoginAttempt(user.getUserName(), clientIp, false, 
                    "계정 잠김", request);
                
                return new JwtResponse(false, "계정이 잠겼습니다. 잠시 후 다시 시도하세요.");
            }
            
            // 패스워드 만료 확인
            if (passwordPolicyService.isPasswordExpired(user.getPasswordChangedAt())) {
                user.setForcePasswordChange(true);
                userRepository.save(user);
                return new JwtResponse(false, "패스워드가 만료되었습니다. 패스워드를 변경해주세요.");
            }
            
            try {
                // Spring Security 인증 매니저를 통한 인증
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword())
                );
                
                // 인증 성공 시 로그인 관련 필드 업데이트
                user.setLastLoginAt(LocalDateTime.now());
                user.setLoginAttempts(0); // 성공 시 실패 횟수 초기화
                user.setAccountLockedUntil(null); // 잠금 해제
                userRepository.save(user);
                
                // JWT 토큰 생성
                String accessToken = jwtUtil.generateToken(user.getUserName(), user.getUserId(), user.getCompanyId());
                String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());
                
                String companyName = user.getCompany() != null ? user.getCompany().getCompanyName() : "Unknown Company";
                
                log.info("사용자 로그인 완료: {}, IP: {}", user.getUserName(), clientIp);
                
                // 성공한 로그인 기록
                rateLimitService.recordLoginAttempt(clientIp, true);
                
                // 보안 감사 로깅 - 로그인 성공
                securityAuditService.logLoginAttempt(user.getUserName(), clientIp, true, null, request);
                
                return new JwtResponse(
                    true, 
                    accessToken, 
                    refreshToken,
                    user.getUserName(), 
                    companyName,
                    user.getUserId(),
                    user.getCompanyId()
                );
                
            } catch (AuthenticationException e) {
                // 인증 실패 시 로그인 시도 횟수 증가 및 Rate Limiting 기록
                handleFailedLogin(user);
                rateLimitService.recordLoginAttempt(clientIp, false);
                
                // 보안 감사 로깅 - 로그인 실패
                securityAuditService.logLoginAttempt(user.getUserName(), clientIp, false, 
                    "인증 실패: " + e.getMessage(), request);
                
                return new JwtResponse(false, "잘못된 사용자명 또는 패스워드입니다.");
            }
            
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 자격 증명: {}", userDTO.getUsername());
            return new JwtResponse(false, "잘못된 사용자명 또는 비밀번호입니다.");
        } catch (AuthenticationException e) {
            log.warn("로그인 실패 - 인증 오류: {}", e.getMessage());
            return new JwtResponse(false, "인증에 실패했습니다.");
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage());
            return new JwtResponse(false, "로그인 처리 중 오류가 발생했습니다.");
        }
    }
    
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            User user = userRepository.findByUserName(username);
            if (user == null) {
                return new JwtResponse(false, "사용자를 찾을 수 없습니다.");
            }
            
            // 리프레시 토큰 유효성 검증
            if (jwtUtil.validateToken(refreshToken, username)) {
                String newAccessToken = jwtUtil.generateToken(user.getUserName(), user.getUserId(), user.getCompanyId());
                String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserName());
                
                String companyName = user.getCompany() != null ? user.getCompany().getCompanyName() : "Unknown Company";
                
                return new JwtResponse(
                    true, 
                    newAccessToken, 
                    newRefreshToken,
                    user.getUserName(), 
                    companyName,
                    user.getUserId(),
                    user.getCompanyId()
                );
            } else {
                return new JwtResponse(false, "유효하지 않은 리프레시 토큰입니다.");
            }
            
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            return new JwtResponse(false, "토큰 갱신 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 계정 잠금 확인
     */
    private boolean isAccountLocked(User user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }
        
        // 잠금 시간이 지났으면 잠금 해제
        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            user.setAccountLockedUntil(null);
            user.setLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }
    
    /**
     * 로그인 실패 처리
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);
        
        // 5회 실패 시 30분 계정 잠금
        if (attempts >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("계정 잠금: {} (IP: {})", user.getUserName(), getClientIpAddress());
        }
        
        userRepository.save(user);
        log.warn("로그인 실패: {} (시도: {}/5, IP: {})", user.getUserName(), attempts, getClientIpAddress());
    }
    
    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }
        
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 여러 IP가 있는 경우 첫 번째 IP 사용
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 패스워드 변경
     */
    @Transactional
    public JwtResponse changePassword(String username, String oldPassword, String newPassword) {
        try {
            User user = userRepository.findByUserName(username);
            if (user == null) {
                return new JwtResponse(false, "사용자를 찾을 수 없습니다.");
            }
            
            // 현재 패스워드 확인
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return new JwtResponse(false, "현재 패스워드가 올바르지 않습니다.");
            }
            
            // 새 패스워드 정책 검증
            PasswordPolicyService.PasswordValidationResult validationResult = 
                passwordPolicyService.validatePassword(newPassword, username);
            
            if (!validationResult.isValid()) {
                String errorMessage = "패스워드 정책 위반: " + String.join(", ", validationResult.getViolations());
                return new JwtResponse(false, errorMessage);
            }
            
            // 패스워드 히스토리 확인
            List<PasswordHistory> passwordHistory = passwordHistoryRepository.findTopNByUserIdOrderByCreatedAtDesc(user.getUserId(), 5);
            List<String> historicalPasswords = passwordHistory.stream()
                .map(PasswordHistory::getPasswordHash)
                .toList();
            
            if (passwordPolicyService.isPasswordInHistory(newPassword, historicalPasswords, passwordEncoder)) {
                return new JwtResponse(false, "최근 사용한 패스워드는 재사용할 수 없습니다.");
            }
            
            // 패스워드 변경
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
            user.setForcePasswordChange(false);
            
            userRepository.save(user);
            
            // 새 패스워드를 히스토리에 추가
            String clientIp = getClientIpAddress();
            PasswordHistory newPasswordHistory = new PasswordHistory(user, encodedNewPassword, clientIp);
            passwordHistoryRepository.save(newPasswordHistory);
            
            // 오래된 패스워드 히스토리 정리 (최근 5개만 유지)
            passwordHistoryRepository.deleteOldPasswordHistory(user.getUserId(), 5);
            
            log.info("패스워드 변경 완료: {}, IP: {}", username, clientIp);
            return new JwtResponse(true, "패스워드가 성공적으로 변경되었습니다.");
            
        } catch (Exception e) {
            log.error("패스워드 변경 중 오류 발생: {}", e.getMessage());
            return new JwtResponse(false, "패스워드 변경 중 오류가 발생했습니다.");
        }
    }
}