package com.u1mobis.dashboard_backend.config;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.ProductionLine;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.ProductionLineRepository;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CompanyRepository companyRepository;
    private final ProductionLineRepository productionLineRepository;
    private final RobotRepository robotRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        log.info("데이터 초기화 시작...");
        
        // 모든 회사에 대해 초기 데이터 확인 및 생성
        List<Company> companies = companyRepository.findAll();
        
        if (companies.isEmpty()) {
            // 기본 회사가 없으면 생성
            Company defaultCompany = ensureCompanyExists();
            companies = List.of(defaultCompany);
        }
        
        for (Company company : companies) {
            try {
                log.info("회사 ID {} ({}) 초기화 처리 시작", company.getCompanyId(), company.getCompanyName());
                
                // 각 회사별로 독립적인 트랜잭션으로 처리
                initializeCompanyData(company);
                
            } catch (Exception e) {
                log.error("회사 ID {} ({}) 초기화 중 오류 발생: {}", 
                         company.getCompanyId(), company.getCompanyName(), e.getMessage(), e);
            }
        }
        
        log.info("데이터 초기화 완료");
    }

    @Transactional
    public void initializeCompanyData(Company company) {
        // 생산라인 초기화
        initializeProductionLines(company);
        
        // 로봇 초기화
        initializeRobots(company);
    }

    private Company ensureCompanyExists() {
        return companyRepository.findById(1L)
                .orElseGet(() -> {
                    log.info("기본 회사 데이터 생성");
                    return companyRepository.save(
                            Company.builder()
                                    .companyId(1L)
                                    .companyName("Default Company")
                                    .companyCode("DEFAULT")
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                });
    }

    private void initializeProductionLines(Company company) {
        if (productionLineRepository.findByCompanyCompanyId(company.getCompanyId()).isEmpty()) {
            log.info("회사 ID {} - 생산라인 초기 데이터 생성 시작", company.getCompanyId());
            
            // 회사별 고유한 라인 코드 생성 (회사명 사용)
            String companyName = company.getCompanyName();
            
            // 1번라인 생성
            ProductionLine line1 = new ProductionLine(
                    company, 
                    "1번라인", 
                    companyName + "_LINE01", 
                    "도어부착 1번라인"
            );
            productionLineRepository.save(line1);
            log.info("1번라인 생성 완료: {}", line1.getLineName());
            
            // 2번라인 생성
            ProductionLine line2 = new ProductionLine(
                    company, 
                    "2번라인", 
                    companyName + "_LINE02", 
                    "도어부착 2번라인"
            );
            productionLineRepository.save(line2);
            log.info("2번라인 생성 완료: {}", line2.getLineName());
            
        } else {
            log.info("회사 ID {} - 생산라인 데이터가 이미 존재함. 초기화 스킵", company.getCompanyId());
        }
    }

    private void initializeRobots(Company company) {
        if (robotRepository.findByCompanyId(company.getCompanyId()).isEmpty()) {
            log.info("회사 ID {} - 로봇 초기 데이터 생성 시작", company.getCompanyId());
            
            // 회사별 고유한 라인 코드로 조회 (회사명 사용)
            String companyName = company.getCompanyName();
            ProductionLine line1 = productionLineRepository.findByLineCodeAndCompanyCompanyId(companyName + "_LINE01", company.getCompanyId())
                    .orElse(null);
            ProductionLine line2 = productionLineRepository.findByLineCodeAndCompanyCompanyId(companyName + "_LINE02", company.getCompanyId())
                    .orElse(null);
            
            if (line1 == null || line2 == null) {
                log.warn("회사 ID {} - 생산라인이 없어서 로봇 초기화를 스킵합니다", company.getCompanyId());
                return;
            }
            
            // 1번라인 로봇 4대 생성
            for (int i = 1; i <= 4; i++) {
                Robot robot = Robot.builder()
                        .robotId(companyName + "_ROBOT_LINE01_" + String.format("%02d", i))
                        .robotName("도어부착 로봇 " + i + "호기")
                        .companyId(company.getCompanyId())
                        .productionLine(line1)
                        .robotType("DOOR_ATTACHMENT")
                        .statusText("READY")
                        .motorStatus(0)
                        .ledStatus(0)
                        .cycleTime(0)
                        .productionCount(0)
                        .quality(100.0)
                        .temperature(25.0)
                        .powerConsumption(0.0)
                        .build();
                
                robotRepository.save(robot);
                log.info("1번라인 로봇 생성 완료: {}", robot.getRobotName());
            }
            
            // 2번라인 로봇 4대 생성
            for (int i = 1; i <= 4; i++) {
                Robot robot = Robot.builder()
                        .robotId(companyName + "_ROBOT_LINE02_" + String.format("%02d", i))
                        .robotName("도어부착 로봇 " + i + "호기")
                        .companyId(company.getCompanyId())
                        .productionLine(line2)
                        .robotType("DOOR_ATTACHMENT")
                        .statusText("READY")
                        .motorStatus(0)
                        .ledStatus(0)
                        .cycleTime(0)
                        .productionCount(0)
                        .quality(100.0)
                        .temperature(25.0)
                        .powerConsumption(0.0)
                        .build();
                
                robotRepository.save(robot);
                log.info("2번라인 로봇 생성 완료: {}", robot.getRobotName());
            }
            
            log.info("회사 ID {} - 총 8대의 로봇 생성 완료", company.getCompanyId());
            
        } else {
            log.info("회사 ID {} - 로봇 데이터가 이미 존재함. 초기화 스킵", company.getCompanyId());
        }
    }
}