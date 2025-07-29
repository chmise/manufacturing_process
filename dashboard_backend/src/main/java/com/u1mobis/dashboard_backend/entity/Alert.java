package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;
    
    @Column(nullable = false)
    private String alertType;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column
    private String type; // danger, warning, info, success
    
    @Column
    private String value;
    
    @Column
    private String productId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @Column
    private String userId;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}