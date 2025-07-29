package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "robots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Robot {

    @Id
    @Column(name = "robot_id")
    private String robotId;

    @Column(name = "robot_name", nullable = false)
    private String robotName;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private ProductionLine productionLine;

    // ===== Digital Twin 필드 추가 =====
    
    @Column(name = "robot_type")
    private String robotType;
    
    
    @Column(name = "status_text")
    private String statusText;
    
    @Column(name = "motor_status")
    private Integer motorStatus;
    
    @Column(name = "led_status")
    private Integer ledStatus;
    
    @Column(name = "cycle_time")
    private Integer cycleTime;
    
    @Column(name = "production_count")
    private Integer productionCount;
    
    @Column(name = "quality")
    private Double quality;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "power_consumption")
    private Double powerConsumption;
    
    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    // 기존 외래키 관계 유지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;


    // 기존 생성자 유지
    public Robot(Long companyId, String robotName) {
        this.companyId = companyId;
        this.robotName = robotName;
    }
}