package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.dto.JwtResponse;
import com.u1mobis.dashboard_backend.dto.RefreshTokenRequest;
import com.u1mobis.dashboard_backend.dto.UserDTO;
import com.u1mobis.dashboard_backend.security.CustomUserDetailsService;
import com.u1mobis.dashboard_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody UserDTO userDTO) {
        JwtResponse response = userService.register(userDTO);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody UserDTO userDTO) {
        JwtResponse response = userService.login(userDTO);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        JwtResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    
    // 현재 사용자 정보 조회 (인증 필요)
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userPrincipal.getUserId());
        userInfo.put("userName", userPrincipal.getUsername());
        userInfo.put("companyId", userPrincipal.getCompanyId());
        
        return ResponseEntity.ok(userInfo);
    }
    
    // 로그아웃 (클라이언트에서 토큰 삭제하면 됨)
    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃되었습니다.");
        return ResponseEntity.ok(response);
    }
}