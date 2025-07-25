package com.u1mobis.dashboard_backend.init;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitService implements CommandLineRunner {
    
    private final CompanyRepository companyRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Company 데이터가 없으면 기본 데이터 생성
        if (companyRepository.count() == 0) {
            Company company = Company.builder()
                    .companyName("현대자동차")
                    .build();
            companyRepository.save(company);
            log.info("기본 회사 데이터 생성 완료: {}", company.getCompanyName());
        }
    }
}