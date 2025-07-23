package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
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
}