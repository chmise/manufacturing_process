package com.u1mobis.dashboard_backend.config;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.ProductionLine;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.entity.Stock;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.repository.ProductionLineRepository;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CompanyRepository companyRepository;
    private final ProductionLineRepository productionLineRepository;
    private final RobotRepository robotRepository;
    private final StockRepository stockRepository;

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
        
        // 재고 초기화
        initializeStock(company);
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
            
            // 1번라인 로봇 4대 생성 (회사별 구분 + MQTT 토픽 호환 ID 형식)
            for (int i = 1; i <= 4; i++) {
                Robot robot = Robot.builder()
                        .robotId(company.getCompanyId() + "_L1_ROBOT_" + String.format("%02d", i))
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
                log.info("1번라인 로봇 생성 완료: {} (ID: {})", robot.getRobotName(), robot.getRobotId());
            }
            
            // 2번라인 로봇 4대 생성 (회사별 구분 + MQTT 토픽 호환 ID 형식)
            for (int i = 1; i <= 4; i++) {
                Robot robot = Robot.builder()
                        .robotId(company.getCompanyId() + "_L2_ROBOT_" + String.format("%02d", i))
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
                log.info("2번라인 로봇 생성 완료: {} (ID: {})", robot.getRobotName(), robot.getRobotId());
            }
            
            log.info("회사 ID {} - 총 8대의 로봇 생성 완료", company.getCompanyId());
            
        } else {
            log.info("회사 ID {} - 로봇 데이터가 이미 존재함. 초기화 스킵", company.getCompanyId());
        }
    }

    private void initializeStock(Company company) {
        // 기존 null company_id 재고를 현재 회사에 할당
        List<Stock> nullCompanyStocks = stockRepository.findAll().stream()
                .filter(stock -> stock.getCompanyId() == null)
                .collect(Collectors.toList());
        
        if (!nullCompanyStocks.isEmpty()) {
            log.info("회사 ID {} - 기존 재고 {} 개를 할당 중", company.getCompanyId(), nullCompanyStocks.size());
            for (Stock stock : nullCompanyStocks) {
                stock.setCompanyId(company.getCompanyId());
                stockRepository.save(stock);
            }
            log.info("회사 ID {} - 기존 재고 할당 완료", company.getCompanyId());
            return; // 기존 재고를 할당했으면 새로 생성하지 않음
        }
        
        // 해당 회사의 재고가 없으면 새로 생성
        if (stockRepository.findAll().stream().noneMatch(stock -> stock.getCompanyId() != null && stock.getCompanyId().equals(company.getCompanyId()))) {
            log.info("회사 ID {} - 재고 초기 데이터 생성 시작", company.getCompanyId());
            
            // 도어 제조용 재고 데이터 생성
            createDoorInventoryData(company);
            
            log.info("회사 ID {} - 재고 데이터 생성 완료", company.getCompanyId());
        } else {
            log.info("회사 ID {} - 재고 데이터가 이미 존재함. 초기화 스킵", company.getCompanyId());
        }
    }

    private void createDoorInventoryData(Company company) {
        // 도어 제조용 재고 데이터 배열
        Object[][] stockData = {
            // 메인 도어 부품
            {"DOOR-001", "전면 도어 패널", 150, 20, "A-01", "부품공급업체A", "양호", "승용차"},
            {"DOOR-002", "후면 도어 패널", 120, 15, "A-02", "부품공급업체A", "양호", "승용차"},
            {"DOOR-003", "운전석 도어", 80, 10, "B-01", "부품공급업체B", "양호", "SUV"},
            {"DOOR-004", "조수석 도어", 75, 10, "B-02", "부품공급업체B", "양호", "SUV"},
            
            // 도어 하드웨어
            {"DOOR-H01", "도어 힌지", 500, 50, "C-01", "하드웨어공급업체", "양호", "공용"},
            {"DOOR-H02", "도어 핸들", 300, 30, "C-02", "하드웨어공급업체", "양호", "공용"},
            {"DOOR-H03", "도어 락 시스템", 200, 25, "C-03", "전자부품업체", "양호", "공용"},
            {"DOOR-H04", "윈도우 레귤레이터", 180, 20, "C-04", "전자부품업체", "양호", "공용"},
            
            // 도어 글래스
            {"DOOR-G01", "전면 도어 글래스", 100, 15, "D-01", "글래스제조업체", "양호", "승용차"},
            {"DOOR-G02", "후면 도어 글래스", 90, 12, "D-02", "글래스제조업체", "양호", "승용차"},
            
            // 도어 씰 및 개스킷
            {"DOOR-S01", "도어 웨더스트립", 400, 40, "E-01", "씰공급업체", "양호", "공용"},
            {"DOOR-S02", "도어 씰", 350, 35, "E-02", "씰공급업체", "양호", "공용"},
            
            // 도어 내장재
            {"DOOR-I01", "도어 트림 패널", 120, 15, "F-01", "내장재업체", "양호", "승용차"},
            {"DOOR-I02", "도어 암레스트", 100, 12, "F-02", "내장재업체", "양호", "승용차"},
            {"DOOR-I03", "도어 스피커 그릴", 200, 20, "F-03", "내장재업체", "양호", "공용"},
            
            // 전자 부품
            {"DOOR-E01", "파워 윈도우 모터", 80, 10, "G-01", "전자부품업체", "양호", "공용"},
            {"DOOR-E02", "도어 컨트롤 모듈", 60, 8, "G-02", "전자부품업체", "양호", "공용"},
            {"DOOR-E03", "키리스 엔트리 센서", 150, 15, "G-03", "전자부품업체", "양호", "공용"},
            
            // 도어 미러
            {"DOOR-M01", "사이드 미러 (좌측)", 70, 8, "H-01", "미러제조업체", "양호", "승용차"},
            {"DOOR-M02", "사이드 미러 (우측)", 70, 8, "H-02", "미러제조업체", "양호", "승용차"},
            
            // 부착용 소재
            {"DOOR-F01", "도어 접착제", 50, 5, "I-01", "화학소재업체", "미사용", "공용"},
            {"DOOR-F02", "도어 볼트", 1000, 100, "I-02", "패스너업체", "양호", "공용"},
            {"DOOR-F03", "도어 너트", 800, 80, "I-03", "패스너업체", "양호", "공용"}
        };

        for (Object[] data : stockData) {
            Stock stock = new Stock();
            stock.setStockCode((String) data[0]);
            stock.setStockName((String) data[1]);
            stock.setCurrentStock((Integer) data[2]);
            stock.setSafetyStock((Integer) data[3]);
            stock.setStockLocation((String) data[4]);
            stock.setPartnerCompany((String) data[5]);
            stock.setInboundDate(LocalDateTime.now());
            stock.setStockState((String) data[6]);
            stock.setCarModel((String) data[7]);
            stock.setCompanyId(company.getCompanyId());
            
            stockRepository.save(stock);
            log.debug("재고 생성: {} - {}", stock.getStockCode(), stock.getStockName());
        }
    }
}