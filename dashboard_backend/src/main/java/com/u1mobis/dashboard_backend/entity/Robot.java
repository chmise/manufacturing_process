package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

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

    // 외래키 관계 (나중에 설정할 예정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    // 비즈니스 로직용 생성자
    public Robot(Long companyId, String robotName) {
        this.companyId = companyId;
        this.robotName = robotName;
    }
}