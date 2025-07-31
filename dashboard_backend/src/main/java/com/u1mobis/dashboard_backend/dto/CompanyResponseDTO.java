package com.u1mobis.dashboard_backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {
    private boolean success;
    private String message;
    private Long companyId;
    private String companyName;
    private String companyCode;
    private LocalDateTime createdAt;
    
    // 기업 검증 관련 필드들
    private Double verificationScore;           // 검증 점수 (0-100)
    private Map<String, Object> verificationDetails; // 상세 검증 결과
    
    // 암호화된 기업 토큰 관련 필드들
    private String enterpriseToken;             // 암호화된 기업 토큰
    private LocalDateTime tokenExpiresAt;       // 토큰 만료 시간
    private String qrCode;                      // QR 코드 (Base64 이미지)
    
    // 스마트 초대 관련 필드들
    private String invitationLink;              // 스마트 초대 링크
    private String nfcData;                     // NFC 태그 데이터
}