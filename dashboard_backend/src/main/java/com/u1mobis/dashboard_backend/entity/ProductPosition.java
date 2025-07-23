package com.u1mobis.dashboard_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_position")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long positionId;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "product_id", nullable = false)
    private String productId;             // 제품 ID
    
    @Column(name = "station_code", nullable = false)
    private String stationCode;           // 현재 위치한 공정 코드 (A01, B02 등)
    
    @Column(name = "x_position")
    private Double xPosition;             // X 좌표
    
    @Column(name = "y_position")
    private Double yPosition;             // Y 좌표
    
    @Column(name = "z_position")
    private Double zPosition;             // Z 좌표
    
    @Column(name = "status")
    private String status;                // 제품 상태 (PROCESSING, WAITING, COMPLETED, ERROR)
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;      // 위치 업데이트 시각
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}