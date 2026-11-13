package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.security.CustomUserDetailsService.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 회사(테넌트) 접근 검사.
 *
 * URL 경로의 {companyName}(예: /api/{companyName}/dashboard)이나 companyName 쿼리 파라미터로
 * 지정한 회사가 로그인한 사용자의 회사와 같은지 확인한다. 다르면 403을 돌려준다.
 */
@Slf4j
@Component
public class TenantAccessInterceptor implements HandlerInterceptor {

    static final String COMPANY_NAME = "companyName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        List<String> requestedCompanies = resolveRequestedCompanies(request);
        if (requestedCompanies.isEmpty()) {
            return true; // 회사를 지정하지 않는 API는 검사 대상이 아니다
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        String userCompany = principal.getCompanyName();
        for (String requested : requestedCompanies) {
            if (userCompany == null || !userCompany.equals(requested)) {
                log.warn("다른 회사 데이터 접근 차단 - 사용자: {}, 소속: {}, 요청: {}",
                        principal.getUsername(), userCompany, requested);
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "해당 회사에 대한 권한이 없습니다.");
                return false;
            }
        }
        return true;
    }

    private List<String> resolveRequestedCompanies(HttpServletRequest request) {
        List<String> companies = new ArrayList<>();

        Object uriVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVariables instanceof Map<?, ?> variables
                && variables.get(COMPANY_NAME) instanceof String fromPath) {
            companies.add(fromPath);
        }

        String fromQuery = request.getParameter(COMPANY_NAME);
        if (fromQuery != null) {
            companies.add(fromQuery);
        }
        return companies;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
