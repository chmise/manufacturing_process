package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.dto.StockChartDTO;
import com.u1mobis.dashboard_backend.dto.StockSummaryDTO;
import com.u1mobis.dashboard_backend.entity.Stock;
import com.u1mobis.dashboard_backend.repository.StockRepository;
import com.u1mobis.dashboard_backend.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")  // 공통 경로
public class StockController {

    private final StockRepository stockRepository;
    private final StockService stockService;

    @Autowired
    public StockController(StockRepository stockRepository, StockService stockService) {
        this.stockRepository = stockRepository;
        this.stockService = stockService;
    }

    // 최근 10개 stock 리스트 반환
    @GetMapping("/stock")
    public List<Stock> getStockList() {
        return stockRepository.findAll(PageRequest.of(0, 10)).getContent();
    }

    // 월별 차트 데이터 반환
    @GetMapping("/stocks/chart")
    public List<StockChartDTO> getChartData() {
        return stockService.getMonthlyChartData();
    }

    // 차량별 재고 요약 데이터 반환 (도넛 차트용)
    @GetMapping("/stocks/summary")
    public List<StockSummaryDTO> getStockSummary() {
        return stockService.getStockSummary();
    }
}
