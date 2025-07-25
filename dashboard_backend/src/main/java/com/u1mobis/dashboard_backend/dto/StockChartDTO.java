package com.u1mobis.dashboard_backend.dto;

import java.util.List;

public class StockChartDTO {
    private String carModel;
    private List<Integer> monthlyStock;

    public StockChartDTO(String carModel, List<Integer> monthlyStock) {
        this.carModel = carModel;
        this.monthlyStock = monthlyStock;
    }

    public String getCarModel() {
        return carModel;
    }

    public List<Integer> getMonthlyStock() {
        return monthlyStock;
    }
}
