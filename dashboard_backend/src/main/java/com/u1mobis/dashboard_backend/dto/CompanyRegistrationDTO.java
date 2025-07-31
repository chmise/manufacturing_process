package com.u1mobis.dashboard_backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegistrationDTO {
    private String companyName;
    private String companyCode;
    
    // 기업 검증을 위한 추가 필드들
    private String businessNumber;      // 사업자등록번호
    private String corporationNumber;   // 법인번호
    private String officialEmail;       // 공식 이메일 (도메인 검증용)
    private String officialPhone;       // 공식 전화번호
    private String website;             // 웹사이트
    private String address;             // 주소
    private String industry;            // 업종
    private String description;         // 회사 설명
}