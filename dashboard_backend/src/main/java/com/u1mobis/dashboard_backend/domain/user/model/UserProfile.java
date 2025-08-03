package com.u1mobis.dashboard_backend.domain.user.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDate;
import java.util.Objects;

public class UserProfile extends ValueObject {
    private final String firstName;
    private final String lastName;
    private final String department;
    private final String position;
    private final String phoneNumber;
    private final LocalDate birthDate;
    private final LocalDate hireDate;
    private final String employeeId;
    private final String profileImageUrl;
    
    public UserProfile(String firstName, String lastName, String department, String position,
                      String phoneNumber, LocalDate birthDate, LocalDate hireDate, 
                      String employeeId, String profileImageUrl) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.department = department != null ? department.trim() : "";
        this.position = position != null ? position.trim() : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : "";
        this.birthDate = birthDate;
        this.hireDate = hireDate != null ? hireDate : LocalDate.now();
        this.employeeId = employeeId != null ? employeeId.trim() : "";
        this.profileImageUrl = profileImageUrl != null ? profileImageUrl.trim() : "";
    }
    
    public static UserProfile createBasic(String firstName, String lastName) {
        return new UserProfile(firstName, lastName, "", "", "", null, LocalDate.now(), "", "");
    }
    
    public UserProfile updateContactInfo(String phoneNumber, String profileImageUrl) {
        return new UserProfile(this.firstName, this.lastName, this.department, this.position,
                             phoneNumber, this.birthDate, this.hireDate, this.employeeId, profileImageUrl);
    }
    
    public UserProfile updateJobInfo(String department, String position, String employeeId) {
        return new UserProfile(this.firstName, this.lastName, department, position,
                             this.phoneNumber, this.birthDate, this.hireDate, employeeId, this.profileImageUrl);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getInitials() {
        return String.valueOf(firstName.isEmpty() ? "" : firstName.charAt(0)) + 
               String.valueOf(lastName.isEmpty() ? "" : lastName.charAt(0));
    }
    
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.isEmpty();
    }
    
    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isEmpty();
    }
    
    public boolean isNewEmployee() {
        return hireDate != null && hireDate.isAfter(LocalDate.now().minusMonths(3));
    }
    
    public long getYearsOfService() {
        return hireDate != null ? 
            java.time.Period.between(hireDate, LocalDate.now()).getYears() : 0;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserProfile that = (UserProfile) obj;
        return Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(department, that.department) &&
               Objects.equals(position, that.position) &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(birthDate, that.birthDate) &&
               Objects.equals(hireDate, that.hireDate) &&
               Objects.equals(employeeId, that.employeeId) &&
               Objects.equals(profileImageUrl, that.profileImageUrl);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, department, position, phoneNumber, 
                          birthDate, hireDate, employeeId, profileImageUrl);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s - %s)", getFullName(), position, department);
    }
}