package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 패스워드 변경 이력 엔티티
 * 사용자의 이전 패스워드를 추적하여 재사용 방지
 */
@Entity
@Table(name = "password_history", indexes = {
    @Index(name = "idx_password_history_user_id", columnList = "user_id"),
    @Index(name = "idx_password_history_created_at", columnList = "created_at")
})
public class PasswordHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by_ip", length = 45)
    private String createdByIp;
    
    public PasswordHistory() {
        this.createdAt = LocalDateTime.now();
    }
    
    public PasswordHistory(User user, String passwordHash, String createdByIp) {
        this();
        this.user = user;
        this.passwordHash = passwordHash;
        this.createdByIp = createdByIp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedByIp() {
        return createdByIp;
    }
    
    public void setCreatedByIp(String createdByIp) {
        this.createdByIp = createdByIp;
    }
}