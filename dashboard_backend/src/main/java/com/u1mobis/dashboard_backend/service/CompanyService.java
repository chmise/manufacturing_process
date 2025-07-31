package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.CompanyRegistrationDTO;
import com.u1mobis.dashboard_backend.dto.CompanyResponseDTO;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.security.EnterpriseVerificationService;
import com.u1mobis.dashboard_backend.security.EnterpriseTokenService;
import com.u1mobis.dashboard_backend.security.SmartInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final EnterpriseVerificationService enterpriseVerificationService;
    private final EnterpriseTokenService enterpriseTokenService;
    private final SmartInvitationService smartInvitationService;

    /**
     * 스마트 회사 등록 (기업 검증 + 암호화된 토큰)
     */
    public CompanyResponseDTO registerCompany(CompanyRegistrationDTO registrationDTO) {
        try {
            log.info("Starting smart company registration for: {}", registrationDTO.getCompanyName());
            
            // 1. 기본 유효성 검증
            if (registrationDTO.getCompanyName() == null || registrationDTO.getCompanyName().trim().isEmpty()) {
                return CompanyResponseDTO.builder()
                        .success(false)
                        .message("회사명은 필수입니다.")
                        .build();
            }

            // 2. 회사명 중복 확인
            if (companyRepository.existsByCompanyName(registrationDTO.getCompanyName())) {
                return CompanyResponseDTO.builder()
                        .success(false)
                        .message("이미 등록된 회사명입니다.")
                        .build();
            }

            // 3. 기업 검증 수행
            EnterpriseVerificationService.EnterpriseVerificationRequest verificationRequest = 
                new EnterpriseVerificationService.EnterpriseVerificationRequest();
            verificationRequest.setCompanyName(registrationDTO.getCompanyName());
            verificationRequest.setBusinessNumber(registrationDTO.getBusinessNumber());
            verificationRequest.setOfficialEmail(registrationDTO.getOfficialEmail());
            verificationRequest.setOfficialPhone(registrationDTO.getOfficialPhone());
            verificationRequest.setWebsite(registrationDTO.getWebsite());
            verificationRequest.setAddress(registrationDTO.getAddress());

            EnterpriseVerificationService.EnterpriseVerificationResult verificationResult = 
                enterpriseVerificationService.verifyEnterprise(verificationRequest, null, null, null);

            if (!verificationResult.isValid()) {
                log.warn("Enterprise verification failed for: {}", registrationDTO.getCompanyName());
                return CompanyResponseDTO.builder()
                        .success(false)
                        .message("기업 검증에 실패했습니다: " + verificationResult.getMessage())
                        .verificationScore(verificationResult.getVerificationScore())
                        .verificationDetails(verificationResult.getDetails())
                        .build();
            }

            // 4. 스마트 회사코드 생성 (더 이상 랜덤이 아님)
            String companyCode = generateSmartCompanyCode(registrationDTO.getCompanyName(), 
                                                        registrationDTO.getBusinessNumber());
            
            // 회사코드 중복 확인 (매우 낮은 확률이지만 안전장치)
            if (companyRepository.existsByCompanyCode(companyCode)) {
                companyCode = generateUniqueCompanyCode(); // 충돌 시 fallback
                log.warn("Smart company code collision, using fallback for: {}", registrationDTO.getCompanyName());
            }

            // 5. 확장된 회사 엔티티 생성 및 저장
            Company company = Company.builder()
                    .companyName(registrationDTO.getCompanyName())
                    .companyCode(companyCode)
                    .build();

            Company savedCompany = companyRepository.save(company);

            // 6. 암호화된 기업 토큰 생성
            EnterpriseTokenService.EnterpriseToken enterpriseToken = null;
            try {
                enterpriseToken = enterpriseTokenService.generateEnterpriseToken(
                    savedCompany.getCompanyId(), 
                    savedCompany.getCompanyName(), 
                    savedCompany.getCompanyCode()
                );
                log.info("Generated enterprise token for company: {}", savedCompany.getCompanyName());
            } catch (Exception e) {
                log.error("Failed to generate enterprise token, using fallback", e);
                // 토큰 생성 실패해도 회사 등록은 성공으로 처리
            }

            log.info("Smart company registration successful: {} (코드: {}, 검증점수: {}%)", 
                    savedCompany.getCompanyName(), 
                    savedCompany.getCompanyCode(),
                    verificationResult.getVerificationScore());

            var responseBuilder = CompanyResponseDTO.builder()
                    .success(true)
                    .message(String.format("회사가 성공적으로 등록되었습니다. (검증점수: %.1f%%)", 
                            verificationResult.getVerificationScore()))
                    .companyId(savedCompany.getCompanyId())
                    .companyName(savedCompany.getCompanyName())
                    .companyCode(savedCompany.getCompanyCode())
                    .createdAt(savedCompany.getCreatedAt())
                    .verificationScore(verificationResult.getVerificationScore())
                    .verificationDetails(verificationResult.getDetails());

            // 7. 기업 토큰 정보 추가 (생성된 경우)
            if (enterpriseToken != null) {
                responseBuilder
                    .enterpriseToken(enterpriseToken.getTokenValue())
                    .tokenExpiresAt(enterpriseToken.getExpiresAt())
                    .qrCode(enterpriseToken.getQrCode());
            }

            // 8. 스마트 초대 생성 (직원 등록용)
            try {
                SmartInvitationService.SmartInvitation invitation = smartInvitationService.createInvitation(
                    savedCompany.getCompanyId(),
                    savedCompany.getCompanyName(),
                    savedCompany.getCompanyCode(),
                    SmartInvitationService.InvitationType.EMPLOYEE_REGISTRATION,
                    "SYSTEM" // 시스템 자동 생성
                );
                
                responseBuilder
                    .invitationLink(invitation.getInvitationLink())
                    .nfcData(invitation.getNfcData());
                
                // QR 코드가 없는 경우 초대용 QR 코드 사용
                if (enterpriseToken == null) {
                    responseBuilder.qrCode(invitation.getQrCode());
                }
                
                log.info("Created smart invitation for company: {}", savedCompany.getCompanyName());
                
            } catch (Exception e) {
                log.error("Failed to create smart invitation, continuing without it", e);
                // 초대 생성 실패해도 회사 등록은 성공으로 처리
            }

            return responseBuilder.build();

        } catch (Exception e) {
            log.error("Smart company registration failed for: {}", registrationDTO.getCompanyName(), e);
            return CompanyResponseDTO.builder()
                    .success(false)
                    .message("회사 등록 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 회사코드로 회사 조회
     */
    @Transactional(readOnly = true)
    public Optional<Company> findByCompanyCode(String companyCode) {
        return companyRepository.findByCompanyCode(companyCode.toUpperCase());
    }

    /**
     * 회사코드 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByCompanyCode(String companyCode) {
        return companyRepository.existsByCompanyCode(companyCode.toUpperCase());
    }

    /**
     * 회사 정보 조회
     */
    @Transactional(readOnly = true)
    public CompanyResponseDTO getCompanyInfo(String companyCode) {
        Optional<Company> company = findByCompanyCode(companyCode);
        
        if (company.isPresent()) {
            Company comp = company.get();
            return CompanyResponseDTO.builder()
                    .success(true)
                    .companyId(comp.getCompanyId())
                    .companyName(comp.getCompanyName())
                    .companyCode(comp.getCompanyCode())
                    .createdAt(comp.getCreatedAt())
                    .build();
        } else {
            return CompanyResponseDTO.builder()
                    .success(false)
                    .message("해당 회사코드를 찾을 수 없습니다.")
                    .build();
        }
    }

    /**
     * 회사명 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean existsByCompanyName(String companyName) {
        return companyRepository.existsByCompanyName(companyName);
    }

    /**
     * 모든 회사 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        
        return companies.stream()
                .map(company -> CompanyResponseDTO.builder()
                        .success(true)
                        .companyId(company.getCompanyId())
                        .companyName(company.getCompanyName())
                        .companyCode(company.getCompanyCode())
                        .createdAt(company.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 스마트 회사코드 생성 (의미있는 패턴 기반)
     */
    private String generateSmartCompanyCode(String companyName, String businessNumber) {
        try {
            StringBuilder codeBuilder = new StringBuilder();
            
            // 1. 회사명에서 자음 추출 (최대 3자)
            String consonants = extractConsonants(companyName);
            if (consonants.length() >= 3) {
                codeBuilder.append(consonants.substring(0, 3));
            } else {
                codeBuilder.append(consonants);
                // 부족한 부분은 회사명 첫 글자들로 채우기
                String firstChars = extractFirstChars(companyName, 3 - consonants.length());
                codeBuilder.append(firstChars);
            }
            
            // 2. 사업자등록번호 기반 체크섬 (2자리)
            if (businessNumber != null && !businessNumber.isEmpty()) {
                String cleanBusinessNumber = businessNumber.replaceAll("-", "");
                if (cleanBusinessNumber.length() >= 6) {
                    int checksum = calculateBusinessChecksum(cleanBusinessNumber);
                    codeBuilder.append(String.format("%02d", checksum % 100));
                } else {
                    codeBuilder.append("00");
                }
            } else {
                // 사업자등록번호 없으면 회사명 해시 기반
                int hash = Math.abs(companyName.hashCode()) % 100;
                codeBuilder.append(String.format("%02d", hash));
            }
            
            // 3. 현재 시간 기반 구분자 (3자리)
            long timestamp = System.currentTimeMillis();
            String timeCode = String.format("%03d", (timestamp % 1000));
            codeBuilder.append(timeCode);
            
            return codeBuilder.toString().toUpperCase();
            
        } catch (Exception e) {
            log.warn("Smart code generation failed, falling back to random: {}", e.getMessage());
            return generateUniqueCompanyCode();
        }
    }
    
    /**
     * 회사명에서 자음 추출
     */
    private String extractConsonants(String companyName) {
        StringBuilder consonants = new StringBuilder();
        String cleaned = companyName.replaceAll("[^가-힣a-zA-Z]", "");
        
        for (char c : cleaned.toCharArray()) {
            if (consonants.length() >= 3) break;
            
            if (c >= 'A' && c <= 'Z') {
                consonants.append(c);
            } else if (c >= 'a' && c <= 'z') {
                consonants.append(Character.toUpperCase(c));
            } else if (c >= '가' && c <= '힣') {
                // 한글 초성 추출
                int unicode = c - '가';
                int initial = unicode / 588;
                char[] initials = {'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'};
                String[] initialMappings = {"G","K","N","D","T","R","M","B","P","S","S","","J","J","C","K","T","P","H"};
                if (initial < initialMappings.length && !initialMappings[initial].isEmpty()) {
                    consonants.append(initialMappings[initial]);
                }
            }
        }
        
        return consonants.toString();
    }
    
    /**
     * 회사명에서 첫 글자들 추출
     */
    private String extractFirstChars(String companyName, int count) {
        StringBuilder firstChars = new StringBuilder();
        String cleaned = companyName.replaceAll("[^가-힣a-zA-Z0-9]", "");
        
        for (int i = 0; i < Math.min(cleaned.length(), count); i++) {
            char c = cleaned.charAt(i);
            if (c >= 'a' && c <= 'z') {
                firstChars.append(Character.toUpperCase(c));
            } else if (c >= 'A' && c <= 'Z') {
                firstChars.append(c);
            } else if (c >= '0' && c <= '9') {
                firstChars.append(c);
            } else if (c >= '가' && c <= '힣') {
                // 한글을 영문으로 변환 (간단한 매핑)
                firstChars.append('K');
            }
        }
        
        // 부족한 부분은 'X'로 채우기
        while (firstChars.length() < count) {
            firstChars.append('X');
        }
        
        return firstChars.toString();
    }
    
    /**
     * 사업자등록번호 기반 체크섬 계산
     */
    private int calculateBusinessChecksum(String businessNumber) {
        int sum = 0;
        for (int i = 0; i < businessNumber.length(); i++) {
            if (Character.isDigit(businessNumber.charAt(i))) {
                sum += Character.getNumericValue(businessNumber.charAt(i)) * (i + 1);
            }
        }
        return sum;
    }
    
    /**
     * 유니크한 회사코드 생성 (Fallback용)
     */
    private String generateUniqueCompanyCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String companyCode;
        
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            companyCode = sb.toString();
        } while (companyRepository.existsByCompanyCode(companyCode));
        
        return companyCode;
    }
}