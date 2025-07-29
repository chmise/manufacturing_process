package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.dto.StockChartDTO;
import com.u1mobis.dashboard_backend.dto.StockSummaryDTO;
import com.u1mobis.dashboard_backend.entity.Stock;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.StockRepository;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/{companyName}")  // 멀티테넌트 경로
@Slf4j
public class StockController {

    private final StockRepository stockRepository;
    private final StockService stockService;
    private final CompanyRepository companyRepository;

    public StockController(StockRepository stockRepository, StockService stockService, CompanyRepository companyRepository) {
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.companyRepository = companyRepository;
    }

    // 최근 10개 stock 리스트 반환
    @GetMapping("/stock")
    public List<Stock> getStockList(@PathVariable String companyName) {
        log.info("재고 리스트 요청 - 회사: {}", companyName);
        
        try {
            // 회사명으로 회사 ID 조회
            Long companyId = getCompanyIdByName(companyName);
            log.info("회사 ID 조회 성공 - 회사: {}, ID: {}", companyName, companyId);
            
            return stockRepository.findByCompanyId(companyId, PageRequest.of(0, 10)).getContent();
        } catch (IllegalArgumentException e) {
            log.error("회사 조회 실패 - 회사: {}, 오류: {}", companyName, e.getMessage());
            throw e;
        }
    }
    
    // 회사명으로 회사 ID 조회
    private Long getCompanyIdByName(String companyName) {
        return companyRepository.findByCompanyName(companyName)
                .map(Company::getCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사입니다: " + companyName));
    }

    // 월별 차트 데이터 반환
    @GetMapping("/stocks/chart")
    public List<StockChartDTO> getChartData(@PathVariable String companyName) {
        log.info("재고 차트 데이터 요청 - 회사: {}", companyName);
        Long companyId = getCompanyIdByName(companyName);
        return stockService.getMonthlyChartData(companyId);
    }

    // 차량별 재고 요약 데이터 반환 (도넛 차트용, 차 이름 기반)
    @GetMapping("/stocks/summary")
    public List<StockSummaryDTO> getStockSummary(@PathVariable String companyName) {
        log.info("재고 요약 데이터 요청 - 회사: {}", companyName);
        Long companyId = getCompanyIdByName(companyName);
        return stockService.getStockSummary(companyId);
    }
}