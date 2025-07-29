package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {
    
    @Id
    @Column(name = "product_id")
    private String productId;  // CurrentProduction과 연결
    
    @Column(name = "door_color")
    private String doorColor;  // Unity에서 문 색상 표시용
    
    @Column(name = "work_progress")
    private Integer workProgress = 0;  // 작업 진행률 (0-100%)
    
    @Column(name = "estimated_completion")
    private LocalDateTime estimatedCompletion;  // 예상 완료 시간
    
    // Unity 3D 좌표
    @Column(name = "position_x")
    private Double positionX = 0.0;
    
    @Column(name = "position_y") 
    private Double positionY = 0.0;
    
    @Column(name = "position_z")
    private Double positionZ = 0.0;
    
    @Column(name = "line_id", nullable = false)
    private Long lineId;  // 어느 라인에 속하는지
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ProductionLine과의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", insertable = false, updatable = false)
    private ProductionLine productionLine;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: Unity 좌표 설정
    public void setUnityPosition(double x, double y, double z) {
        this.positionX = x;
        this.positionY = y;
        this.positionZ = z;
    }
    
    // 편의 메서드: 작업 진행률 업데이트
    public void updateProgress(int progress) {
        this.workProgress = Math.max(0, Math.min(100, progress));
        this.updatedAt = LocalDateTime.now();
    }
}