package com.u1mobis.dashboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String userName;
    private String companyName;
    private Long userId;
    private Long companyId;
    
    // 로그인 실패시 사용하는 생성자
    public LoginResponseDTO(boolean success) {
        this.success = success;
    }
}