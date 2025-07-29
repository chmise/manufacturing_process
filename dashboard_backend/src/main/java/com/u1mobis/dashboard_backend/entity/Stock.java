package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    private String stockCode;
    private String stockName;
    private int currentStock;
    private int safetyStock;
    private String stockLocation;
    private String partnerCompany;
    private LocalDateTime inboundDate;
    private String stockState;

    // ✅ 차량 이름 필드
    @Column(name = "car_model")
    private String carModel;

    // ✅ 회사 ID 필드 추가 (멀티테넌트 지원)
    @Column(name = "company_id")
    private Long companyId;

    // 비즈니스 로직용 생성자
    public Stock(String stockName, int currentStock, int safetyStock) {
        this.stockName = stockName;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
    }
}