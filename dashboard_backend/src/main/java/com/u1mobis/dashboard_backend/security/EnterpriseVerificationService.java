package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 기업 검증 서비스
 * 사업자등록번호, 도메인 이메일, 전화번호 등을 통한 실제 기업 검증
 */
@Service
public class EnterpriseVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnterpriseVerificationService.class);
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    @Value("${enterprise.verification.enabled:true}")
    private boolean verificationEnabled;
    
    @Value("${enterprise.verification.strict-mode:false}")
    private boolean strictMode;
    
    @Value("${enterprise.verification.api.business-registry-key:}")
    private String businessRegistryApiKey;
    
    // 사업자등록번호 패턴 (XXX-XX-XXXXX)
    private static final Pattern BUSINESS_NUMBER_PATTERN = Pattern.compile("^\\d{3}-?\\d{2}-?\\d{5}$");
    
    // 법인번호 패턴 (XXXXXX-XXXXXXX)
    private static final Pattern CORPORATION_NUMBER_PATTERN = Pattern.compile("^\\d{6}-?\\d{7}$");
    
    // 도메인 이메일 패턴
    private static final Pattern DOMAIN_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    // 전화번호 패턴 (한국 번호 기준)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+82|0)([0-9]{1,2})-?([0-9]{3,4})-?([0-9]{4})$");
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 종합 기업 검증
     */
    public EnterpriseVerificationResult verifyEnterprise(EnterpriseVerificationRequest request, 
                                                        Long userId, Long companyId, 
                                                        HttpServletRequest httpRequest) {
        logger.info("Starting enterprise verification for: {}", request.getCompanyName());
        
        if (!verificationEnabled) {
            logger.info("Enterprise verification is disabled, skipping verification");
            return EnterpriseVerificationResult.success("검증이 비활성화되어 있습니다.");
        }
        
        var resultBuilder = EnterpriseVerificationResult.builder();
        var details = new HashMap<String, Object>();
        int score = 0;
        int maxScore = 0;
        
        // 1. 사업자등록번호 검증
        if (request.getBusinessNumber() != null && !request.getBusinessNumber().isEmpty()) {
            maxScore += 30;
            var businessVerification = verifyBusinessNumber(request.getBusinessNumber(), request.getCompanyName());
            details.put("businessNumber", businessVerification);
            
            if (businessVerification.isValid()) {
                score += 30;
                logger.info("Business number verification passed for: {}", request.getCompanyName());
            } else {
                logger.warn("Business number verification failed for: {}", request.getCompanyName());
                if (strictMode) {
                    return resultBuilder
                        .valid(false)
                        .message("사업자등록번호 검증에 실패했습니다: " + businessVerification.getMessage())
                        .details(details)
                        .build();
                }
            }
        }
        
        // 2. 도메인 이메일 검증
        if (request.getOfficialEmail() != null && !request.getOfficialEmail().isEmpty()) {
            maxScore += 25;
            var emailVerification = verifyDomainEmail(request.getOfficialEmail(), request.getCompanyName());
            details.put("domainEmail", emailVerification);
            
            if (emailVerification.isValid()) {
                score += 25;
                logger.info("Domain email verification passed for: {}", request.getCompanyName());
            } else {
                logger.warn("Domain email verification failed for: {}", request.getCompanyName());
                if (strictMode) {
                    return resultBuilder
                        .valid(false)
                        .message("도메인 이메일 검증에 실패했습니다: " + emailVerification.getMessage())
                        .details(details)
                        .build();
                }
            }
        }
        
        // 3. 전화번호 검증
        if (request.getOfficialPhone() != null && !request.getOfficialPhone().isEmpty()) {
            maxScore += 20;
            var phoneVerification = verifyPhoneNumber(request.getOfficialPhone());
            details.put("phoneNumber", phoneVerification);
            
            if (phoneVerification.isValid()) {
                score += 20;
                logger.info("Phone number verification passed for: {}", request.getCompanyName());
            } else {
                logger.warn("Phone number verification failed for: {}", request.getCompanyName());
            }
        }
        
        // 4. 웹사이트 검증
        if (request.getWebsite() != null && !request.getWebsite().isEmpty()) {
            maxScore += 15;
            var websiteVerification = verifyWebsite(request.getWebsite(), request.getCompanyName());
            details.put("website", websiteVerification);
            
            if (websiteVerification.isValid()) {
                score += 15;
                logger.info("Website verification passed for: {}", request.getCompanyName());
            }
        }
        
        // 5. 주소 검증
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            maxScore += 10;
            var addressVerification = verifyAddress(request.getAddress());
            details.put("address", addressVerification);
            
            if (addressVerification.isValid()) {
                score += 10;
                logger.info("Address verification passed for: {}", request.getCompanyName());
            }
        }
        
        // 최종 검증 점수 계산
        double verificationScore = maxScore > 0 ? (double) score / maxScore * 100 : 0;
        boolean isValid = verificationScore >= (strictMode ? 80 : 60);
        
        String message = String.format("기업 검증 완료 - 점수: %.1f%% (%d/%d)", 
                                      verificationScore, score, maxScore);
        
        logger.info("Enterprise verification completed for {}: {}", request.getCompanyName(), message);
        
        // 보안 감사 로깅
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("companyName", request.getCompanyName());
        auditData.put("businessNumber", request.getBusinessNumber());
        auditData.put("verificationScore", verificationScore);
        auditData.put("details", details);
        
        securityAuditService.logSecurityEvent(
            com.u1mobis.dashboard_backend.entity.SecurityAuditLog.EventType.COMPANY_REGISTRATION,
            isValid ? com.u1mobis.dashboard_backend.entity.SecurityAuditLog.RiskLevel.LOW : 
                     com.u1mobis.dashboard_backend.entity.SecurityAuditLog.RiskLevel.MEDIUM,
            String.format("기업 검증 %s: %s (점수: %.1f%%)", 
                         isValid ? "성공" : "실패", request.getCompanyName(), verificationScore),
            userId, companyId, httpRequest, isValid, auditData
        );
        
        return resultBuilder
            .valid(isValid)
            .message(message)
            .verificationScore(verificationScore)
            .details(details)
            .build();
    }
    
    /**
     * 사업자등록번호 검증
     */
    private VerificationDetail verifyBusinessNumber(String businessNumber, String companyName) {
        // 형식 검증
        String cleanNumber = businessNumber.replaceAll("-", "");
        if (!BUSINESS_NUMBER_PATTERN.matcher(businessNumber).matches() || cleanNumber.length() != 10) {
            return VerificationDetail.invalid("사업자등록번호 형식이 올바르지 않습니다.");
        }
        
        // 체크섬 검증
        if (!isValidBusinessNumberChecksum(cleanNumber)) {
            return VerificationDetail.invalid("사업자등록번호 체크섬이 일치하지 않습니다.");
        }
        
        // 실제 API 검증 (국세청 사업자등록정보 진위확인 서비스)
        if (businessRegistryApiKey != null && !businessRegistryApiKey.isEmpty()) {
            try {
                boolean isRegistered = verifyBusinessNumberWithAPI(cleanNumber, companyName);
                if (!isRegistered) {
                    return VerificationDetail.invalid("등록되지 않은 사업자등록번호입니다.");
                }
            } catch (Exception e) {
                logger.warn("Business number API verification failed: {}", e.getMessage());
                // API 실패시에도 형식 검증은 통과했으므로 부분 성공
                return VerificationDetail.partial("API 검증 실패, 형식 검증만 통과");
            }
        }
        
        return VerificationDetail.valid("사업자등록번호 검증 완료");
    }
    
    /**
     * 사업자등록번호 체크섬 검증
     */
    private boolean isValidBusinessNumberChecksum(String businessNumber) {
        if (businessNumber.length() != 10) return false;
        
        int[] weights = {1, 3, 7, 1, 3, 7, 1, 3, 5};
        int sum = 0;
        
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(businessNumber.charAt(i)) * weights[i];
        }
        
        sum += (Character.getNumericValue(businessNumber.charAt(8)) * 5) / 10;
        int checkDigit = (10 - (sum % 10)) % 10;
        
        return checkDigit == Character.getNumericValue(businessNumber.charAt(9));
    }
    
    /**
     * 사업자등록번호 실제 API 검증
     */
    private boolean verifyBusinessNumberWithAPI(String businessNumber, String companyName) {
        // 실제 구현에서는 국세청 API를 호출해야 함
        // 현재는 모의 구현
        logger.info("Verifying business number {} with external API", businessNumber);
        
        // TODO: 실제 국세청 사업자등록정보 진위확인 서비스 API 호출
        // return callNTSBusinessVerificationAPI(businessNumber, companyName);
        
        // 모의 구현: 특정 패턴은 유효하다고 가정
        return !businessNumber.startsWith("000");
    }
    
    /**
     * 도메인 이메일 검증
     */
    private VerificationDetail verifyDomainEmail(String email, String companyName) {
        if (!DOMAIN_EMAIL_PATTERN.matcher(email).matches()) {
            return VerificationDetail.invalid("이메일 형식이 올바르지 않습니다.");
        }
        
        String domain = email.substring(email.indexOf("@") + 1);
        
        // 무료 이메일 서비스 체크
        if (isFreeEmailProvider(domain)) {
            return VerificationDetail.invalid("기업용 도메인 이메일이 필요합니다. (Gmail, Naver 등 무료 서비스 불가)");
        }
        
        // DNS MX 레코드 확인
        try {
            if (!hasMXRecord(domain)) {
                return VerificationDetail.invalid("유효하지 않은 도메인입니다. (MX 레코드 없음)");
            }
        } catch (Exception e) {
            logger.warn("MX record check failed for domain {}: {}", domain, e.getMessage());
            return VerificationDetail.partial("DNS 검증 실패, 형식 검증만 통과");
        }
        
        return VerificationDetail.valid("도메인 이메일 검증 완료");
    }
    
    /**
     * 무료 이메일 서비스 체크
     */
    private boolean isFreeEmailProvider(String domain) {
        String[] freeProviders = {
            "gmail.com", "naver.com", "daum.net", "hanmail.net", "nate.com",
            "yahoo.com", "hotmail.com", "outlook.com", "live.com", "msn.com"
        };
        
        for (String provider : freeProviders) {
            if (domain.equalsIgnoreCase(provider)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * MX 레코드 확인
     */
    private boolean hasMXRecord(String domain) {
        try {
            javax.naming.Context context = new javax.naming.directory.InitialDirContext();
            javax.naming.directory.Attributes attrs = 
                ((javax.naming.directory.DirContext) context).getAttributes("dns:/" + domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (Exception e) {
            logger.debug("MX record check failed for domain {}: {}", domain, e.getMessage());
            return false;
        }
    }
    
    /**
     * 전화번호 검증
     */
    private VerificationDetail verifyPhoneNumber(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return VerificationDetail.invalid("전화번호 형식이 올바르지 않습니다.");
        }
        
        // 추가 검증 로직 (통신사 API 등)
        // TODO: 실제 전화번호 유효성 검증 API 연동
        
        return VerificationDetail.valid("전화번호 형식 검증 완료");
    }
    
    /**
     * 웹사이트 검증
     */
    private VerificationDetail verifyWebsite(String website, String companyName) {
        try {
            if (!website.startsWith("http://") && !website.startsWith("https://")) {
                website = "http://" + website;
            }
            
            // HTTP 헤드 요청으로 웹사이트 접근 가능성 확인
            var response = restTemplate.headForHeaders(website);
            
            if (response != null) {
                return VerificationDetail.valid("웹사이트 접근 확인됨");
            }
        } catch (Exception e) {
            logger.debug("Website verification failed for {}: {}", website, e.getMessage());
            return VerificationDetail.invalid("웹사이트에 접근할 수 없습니다: " + e.getMessage());
        }
        
        return VerificationDetail.invalid("웹사이트 검증 실패");
    }
    
    /**
     * 주소 검증
     */
    private VerificationDetail verifyAddress(String address) {
        // 한국 주소 형식 기본 검증
        if (address.length() < 10) {
            return VerificationDetail.invalid("주소가 너무 짧습니다.");
        }
        
        // TODO: 도로명 주소 API 연동하여 실제 주소 검증
        // 현재는 기본적인 형식 검증만 수행
        
        return VerificationDetail.valid("주소 형식 검증 완료");
    }
    
    /**
     * 검증 상세 정보 클래스
     */
    public static class VerificationDetail {
        private final boolean valid;
        private final String message;
        private final String status; // "valid", "invalid", "partial"
        
        private VerificationDetail(boolean valid, String message, String status) {
            this.valid = valid;
            this.message = message;
            this.status = status;
        }
        
        public static VerificationDetail valid(String message) {
            return new VerificationDetail(true, message, "valid");
        }
        
        public static VerificationDetail invalid(String message) {
            return new VerificationDetail(false, message, "invalid");
        }
        
        public static VerificationDetail partial(String message) {
            return new VerificationDetail(true, message, "partial");
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
    
    /**
     * 기업 검증 요청 클래스
     */
    public static class EnterpriseVerificationRequest {
        private String companyName;
        private String businessNumber;
        private String corporationNumber;
        private String officialEmail;
        private String officialPhone;
        private String website;
        private String address;
        
        // Getters and setters
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        
        public String getBusinessNumber() { return businessNumber; }
        public void setBusinessNumber(String businessNumber) { this.businessNumber = businessNumber; }
        
        public String getCorporationNumber() { return corporationNumber; }
        public void setCorporationNumber(String corporationNumber) { this.corporationNumber = corporationNumber; }
        
        public String getOfficialEmail() { return officialEmail; }
        public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }
        
        public String getOfficialPhone() { return officialPhone; }
        public void setOfficialPhone(String officialPhone) { this.officialPhone = officialPhone; }
        
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    /**
     * 기업 검증 결과 클래스
     */
    public static class EnterpriseVerificationResult {
        private final boolean valid;
        private final String message;
        private final double verificationScore;
        private final Map<String, Object> details;
        
        private EnterpriseVerificationResult(Builder builder) {
            this.valid = builder.valid;
            this.message = builder.message;
            this.verificationScore = builder.verificationScore;
            this.details = builder.details;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static EnterpriseVerificationResult success(String message) {
            return builder().valid(true).message(message).verificationScore(100.0).build();
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public double getVerificationScore() { return verificationScore; }
        public Map<String, Object> getDetails() { return details; }
        
        public static class Builder {
            private boolean valid;
            private String message;
            private double verificationScore;
            private Map<String, Object> details = new HashMap<>();
            
            public Builder valid(boolean valid) { this.valid = valid; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder verificationScore(double score) { this.verificationScore = score; return this; }
            public Builder details(Map<String, Object> details) { this.details = details; return this; }
            
            public EnterpriseVerificationResult build() {
                return new EnterpriseVerificationResult(this);
            }
        }
    }
}