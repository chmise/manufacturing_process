package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 요구사항
@ToString(exclude = {"password"}) // 보안상 password 제외
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // ID만으로 비교
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include // equals/hashCode에 ID만 포함
    private Long userId;

    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 외래키 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    // 패스워드 보안 관련 필드
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "login_attempts", nullable = false, columnDefinition = "integer default 0")
    private int loginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;
    
    @Column(name = "force_password_change", nullable = false, columnDefinition = "boolean default false")
    private boolean forcePasswordChange = false;

    // 편의 메서드: companyId 조회
    public Long getCompanyId() {
        return company != null ? company.getCompanyId() : null;
    }

    // 비즈니스 로직에 필요한 생성자
    public User(Company company, String userName, String password, String email, LocalDate birth, String employeeCode, String name) {
        this.company = company;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.employeeCode = employeeCode;
        this.name = name;
    }

    // 기존 호환성을 위한 생성자
    public User(Company company, String userName, String password, String email, LocalDate birth) {
        this.company = company;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.birth = birth;
    }

    // 기존 코드 호환성을 위한 생성자
    public User(Long companyId, String userName, String password) {
        this.userName = userName;
        this.password = password;
        // company는 별도로 설정 필요
    }
}