package com.u1mobis.dashboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryDTO {
    // 차량 모델 이름 (도넛 차트의 레이블로 사용)
    private String carModel;
    // 해당 차량 모델의 재고 수량
    private int count;
}