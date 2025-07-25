package com.u1mobis.dashboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userName;
    private String companyName;
    private Long userId;
    private Long companyId;
    private String message;
    
    // 성공 응답용 생성자
    public JwtResponse(boolean success, String accessToken, String refreshToken, 
                       String userName, String companyName, Long userId, Long companyId) {
        this.success = success;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userName = userName;
        this.companyName = companyName;
        this.userId = userId;
        this.companyId = companyId;
        this.tokenType = "Bearer";
    }
    
    // 실패 응답용 생성자
    public JwtResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}