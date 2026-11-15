package com.u1mobis.dashboard_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.User;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.UserRepository;
import com.u1mobis.dashboard_backend.util.JwtUtil;

/**
 * 회사(테넌트) 간 접근 차단 테스트.
 * tenant-a 사용자(alice)가 tenant-b 데이터에 접근하지 못하는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantAccessIntegrationTest {

    private static final String COMPANY_A = "tenant-a";
    private static final String COMPANY_B = "tenant-b";
    private static final String CODE_B = "TENANTBB";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private String accessTokenA;
    private String refreshTokenA;

    @BeforeEach
    void setUp() {
        Company companyA = companyRepository.findByCompanyName(COMPANY_A)
                .orElseGet(() -> companyRepository.save(
                        Company.builder().companyName(COMPANY_A).companyCode("TENANTAA").build()));
        companyRepository.findByCompanyName(COMPANY_B)
                .orElseGet(() -> companyRepository.save(
                        Company.builder().companyName(COMPANY_B).companyCode(CODE_B).build()));

        User alice = userRepository.findByUserName("alice");
        if (alice == null) {
            alice = userRepository.save(new User(companyA, "alice", passwordEncoder.encode("password123"),
                    "alice@example.com", LocalDate.of(1999, 1, 1), "E001", "앨리스"));
        }
        accessTokenA = jwtUtil.generateToken(alice.getUserName(), alice.getUserId(), companyA.getCompanyId());
        refreshTokenA = jwtUtil.generateRefreshToken(alice.getUserName());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("로그인 API는 토큰 없이 쓸 수 있고, 회사 정보를 함께 돌려준다")
    void loginWorksWithoutToken() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.companyName").value(COMPANY_A));
    }

    @Test
    @DisplayName("자기 회사 경로는 접근할 수 있다")
    void ownCompanyIsAllowed() throws Exception {
        mockMvc.perform(get("/api/" + COMPANY_A + "/user/me").header("Authorization", bearer(accessTokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("alice"));
    }

    @Test
    @DisplayName("로그인하지 않으면 401")
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/" + COMPANY_A + "/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("다른 회사 대시보드는 403")
    void otherCompanyDashboardIsForbidden() throws Exception {
        mockMvc.perform(get("/api/" + COMPANY_B + "/dashboard").header("Authorization", bearer(accessTokenA)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 회사 KPI에 값을 쓸 수 없다")
    void otherCompanyKpiWriteIsForbidden() throws Exception {
        mockMvc.perform(post("/api/" + COMPANY_B + "/kpi/process")
                        .header("Authorization", bearer(accessTokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planned_time\":480,\"downtime\":0,\"target_cycle_time\":60,"
                                + "\"good_count\":1,\"total_count\":1,"
                                + "\"first_time_pass_count\":1,\"on_time_delivery_count\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("쿼리 파라미터로 다른 회사를 지정해도 403")
    void otherCompanyQueryParamIsForbidden() throws Exception {
        mockMvc.perform(get("/api/unity/realtime-data")
                        .param("companyName", COMPANY_B)
                        .header("Authorization", bearer(accessTokenA)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("리프레시 토큰으로는 API를 호출할 수 없다")
    void refreshTokenCannotCallApi() throws Exception {
        mockMvc.perform(get("/api/" + COMPANY_A + "/user/me").header("Authorization", bearer(refreshTokenA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("다른 회사 토픽으로 MQTT 메시지를 발행할 수 없다")
    void mqttPublishToOtherCompanyIsForbidden() throws Exception {
        mockMvc.perform(post("/api/mqtt/publish")
                        .header("Authorization", bearer(accessTokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"factory/" + CODE_B + "/environment\",\"message\":\"{}\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("로그인하지 않으면 MQTT 메시지를 발행할 수 없다")
    void mqttPublishRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/mqtt/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"factory/" + CODE_B + "/environment\",\"message\":\"{}\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회사 코드 목록을 돌려주던 API는 없다")
    void companyListIsNotPublic() throws Exception {
        mockMvc.perform(get("/api/company/list"))
                .andExpect(status().isUnauthorized());
    }
}
