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
    public List<StockChartDTO> getMonthlyChartData(Long companyId) {
        List<Stock> stocks = stockRepository.findByCompanyId(companyId);

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

    // 🍩 도넛 차트용 차량별 재고 요약 데이터 (차 이름 기반)
    public List<StockSummaryDTO> getStockSummary(Long companyId) {
        List<Stock> stocks = stockRepository.findByCompanyId(companyId);

        // ✅ 부품명 → 차량명 매핑 테이블
        Map<String, String> partToCarMap = new HashMap<>();
        partToCarMap.put("운전석 시트", "아반떼");
        partToCarMap.put("조수석 시트", "아반떼");
        partToCarMap.put("타이어", "소나타");
        partToCarMap.put("대시보드", "K5");
        partToCarMap.put("레이더 센서", "아이오닉5");
        partToCarMap.put("도어 패널", "쏘렌토");
        partToCarMap.put("와이어링 하니스", "EV6");
        partToCarMap.put("헤드램프", "팰리세이드");
        partToCarMap.put("배터리 팩", "EV6");
        partToCarMap.put("브레이크 패드", "GV80");
        // 필요한 만큼 계속 추가하세요

        // 차량별 집계
        Map<String, Integer> summaryMap = new HashMap<>();
        for (Stock stock : stocks) {
            String partName = stock.getStockName();
            String carModel = partToCarMap.getOrDefault(partName, "기타");
            int count = stock.getCurrentStock();
            summaryMap.put(carModel, summaryMap.getOrDefault(carModel, 0) + count);
        }

        List<StockSummaryDTO> summaryList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : summaryMap.entrySet()) {
            summaryList.add(new StockSummaryDTO(entry.getKey(), entry.getValue()));
        }

        return summaryList;
    }
}