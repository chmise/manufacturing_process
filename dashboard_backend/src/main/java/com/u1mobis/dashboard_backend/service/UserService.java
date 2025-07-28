package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.JwtResponse;
import com.u1mobis.dashboard_backend.dto.RefreshTokenRequest;
import com.u1mobis.dashboard_backend.dto.UserDTO;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.User;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.UserRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse register(UserDTO userDTO) {
        try {
            // 사용자명 중복 확인
            if (userRepository.findByUserName(userDTO.getUsername()) != null) {
                return new JwtResponse(false, "이미 존재하는 사용자명입니다.");
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
            User user = new User(company, userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), 
                               userDTO.getEmail(), userDTO.getBirth(), userDTO.getEmployeeCode(), userDTO.getName());
            User savedUser = userRepository.save(user);
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(savedUser.getUserName(), savedUser.getUserId(), savedUser.getCompanyId());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserName());
            
            String companyName = savedUser.getCompany() != null ? savedUser.getCompany().getCompanyName() : "Unknown Company";
            
            log.info("새 사용자 등록 완료: {}", savedUser.getUserName());
            
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

    public JwtResponse login(UserDTO userDTO) {
        try {
            // Spring Security 인증 매니저를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword())
            );
            
            // 인증 성공 시 사용자 정보 조회
            User user = userRepository.findByUserName(userDTO.getUsername());
            if (user == null) {
                return new JwtResponse(false, "사용자를 찾을 수 없습니다.");
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(user.getUserName(), user.getUserId(), user.getCompanyId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());
            
            String companyName = user.getCompany() != null ? user.getCompany().getCompanyName() : "Unknown Company";
            
            log.info("사용자 로그인 완료: {}", user.getUserName());
            
            return new JwtResponse(
                true, 
                accessToken, 
                refreshToken,
                user.getUserName(), 
                companyName,
                user.getUserId(),
                user.getCompanyId()
            );
            
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
}