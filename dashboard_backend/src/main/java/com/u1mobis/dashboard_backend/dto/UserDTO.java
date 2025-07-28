package com.u1mobis.dashboard_backend.dto;

import java.time.LocalDate;

public class UserDTO {
    private String username;
    private String password;
    private String companyName;
    private String companyCode;
    private String email;
    private LocalDate birth;
    private String employeeCode;
    private String name;

    // 생성자, getter, setter
    public UserDTO() {}

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO(String username, String password, String companyName, String email, LocalDate birth) {
        this.username = username;
        this.password = password;
        this.companyName = companyName;
        this.email = email;
        this.birth = birth;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirth() { return birth; }
    public void setBirth(LocalDate birth) { this.birth = birth; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
