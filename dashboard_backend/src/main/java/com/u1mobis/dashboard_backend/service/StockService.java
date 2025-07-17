package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.StockChartDTO;
import com.u1mobis.dashboard_backend.dto.StockSummaryDTO;
import com.u1mobis.dashboard_backend.entity.Stock;
import com.u1mobis.dashboard_backend.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StockService {

    private final StockRepository stockRepository;

    // 생성자 주입
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 📈 월별 차트 데이터
    public List<StockChartDTO> getMonthlyChartData() {
        List<Stock> stocks = stockRepository.findAll();

        Map<String, Map<Integer, Integer>> carMonthStockMap = new HashMap<>();

        for (Stock stock : stocks) {
            String carModel = stock.getStockName();  // 예: 아반떼
            int month = stock.getInboundDate().getMonthValue();  // 입고 월

            carMonthStockMap.putIfAbsent(carModel, new HashMap<>());
            Map<Integer, Integer> monthStock = carMonthStockMap.get(carModel);
            monthStock.put(month, monthStock.getOrDefault(month, 0) + stock.getCurrentStock());
        }

        List<StockChartDTO> result = new ArrayList<>();
        for (String carModel : carMonthStockMap.keySet()) {
            Map<Integer, Integer> monthStock = carMonthStockMap.get(carModel);
            List<Integer> monthlyStockList = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                monthlyStockList.add(monthStock.getOrDefault(m, 0));
            }
            result.add(new StockChartDTO(carModel, monthlyStockList));
        }

        return result;
    }

    // 🍩 도넛 차트용 차량별 재고 요약 데이터
    public List<StockSummaryDTO> getStockSummary() {
        List<Stock> stocks = stockRepository.findAll();

        Map<String, Integer> summaryMap = new HashMap<>();
        for (Stock stock : stocks) {
            String carModel = stock.getStockName();  // 차량 이름
            int count = stock.getCurrentStock();     // 재고 수량
            summaryMap.put(carModel, summaryMap.getOrDefault(carModel, 0) + count);
        }

        List<StockSummaryDTO> summaryList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : summaryMap.entrySet()) {
            summaryList.add(new StockSummaryDTO(entry.getKey(), entry.getValue()));
        }

        return summaryList;
    }
}
