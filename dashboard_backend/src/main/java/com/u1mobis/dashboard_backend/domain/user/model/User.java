package com.u1mobis.dashboard_backend.domain.user.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.user.event.UserCreatedEvent;
import com.u1mobis.dashboard_backend.domain.user.event.UserActivatedEvent;
import com.u1mobis.dashboard_backend.domain.user.event.UserDeactivatedEvent;
import com.u1mobis.dashboard_backend.domain.user.event.UserRoleChangedEvent;
import com.u1mobis.dashboard_backend.domain.user.event.UserLoginEvent;
import com.u1mobis.dashboard_backend.domain.user.event.UserPasswordChangedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User extends AggregateRoot<UserId> {
    private UserId id;
    private CompanyId companyId;
    private String username;
    private Email email;
    private String passwordHash;
    private UserProfile profile;
    private UserRole role;
    private UserStatus status;
    private List<String> permissions;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private String activationToken;
    private String resetPasswordToken;
    private LocalDateTime tokenExpiresAt;
    
    protected User() {
        this.permissions = new ArrayList<>();
    }
    
    public User(UserId id, CompanyId companyId, String username, Email email, 
               String passwordHash, UserProfile profile, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        
        this.id = id;
        this.companyId = companyId;
        this.username = username.trim();
        this.email = email;
        this.passwordHash = passwordHash;
        this.profile = profile;
        this.role = role;
        this.status = UserStatus.PENDING;
        this.permissions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.passwordChangedAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        
        // Generate activation token for new users
        this.activationToken = generateToken();
        this.tokenExpiresAt = LocalDateTime.now().plusDays(7);
        
        // Raise domain event
        raiseEvent(new UserCreatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            username, email.getValue(), role, profile.getFullName()
        ));
    }
    
    public void activate() {
        if (status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        if (status == UserStatus.SUSPENDED || status == UserStatus.LOCKED) {
            throw new IllegalStateException("Cannot activate suspended or locked user");
        }
        
        this.status = UserStatus.ACTIVE;
        this.activationToken = null;
        this.tokenExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserActivatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(), username
        ));
    }
    
    public void deactivate(String reason) {
        if (status == UserStatus.INACTIVE) {
            throw new IllegalStateException("User is already inactive");
        }
        
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserDeactivatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(), username, reason
        ));
    }
    
    public void suspend(String reason) {
        if (status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }
        
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserDeactivatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(), username, 
            "Suspended: " + reason
        ));
    }
    
    public void changeRole(UserRole newRole, String changedBy) {
        if (this.role == newRole) {
            return;
        }
        
        UserRole previousRole = this.role;
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserRoleChangedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            username, previousRole, newRole, changedBy
        ));
    }
    
    public boolean login(String password) {
        if (!canLogin()) {
            return false;
        }
        
        // In real implementation, this would verify the password hash
        boolean passwordValid = verifyPassword(password);
        
        if (passwordValid) {
            this.lastLoginAt = LocalDateTime.now();
            this.failedLoginAttempts = 0;
            this.lockedUntil = null;
            this.updatedAt = LocalDateTime.now();
            
            raiseEvent(new UserLoginEvent(
                LocalDateTime.now(), id.getValue(), companyId.getValue(),
                username, "SUCCESS", null
            ));
            
            return true;
        } else {
            this.failedLoginAttempts++;
            
            // Lock account after 5 failed attempts
            if (failedLoginAttempts >= 5) {
                this.status = UserStatus.LOCKED;
                this.lockedUntil = LocalDateTime.now().plusHours(1);
            }
            
            this.updatedAt = LocalDateTime.now();
            
            raiseEvent(new UserLoginEvent(
                LocalDateTime.now(), id.getValue(), companyId.getValue(),
                username, "FAILED", "Invalid password"
            ));
            
            return false;
        }
    }
    
    public void changePassword(String newPasswordHash, String changedBy) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserPasswordChangedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            username, changedBy
        ));
    }
    
    public void updateProfile(UserProfile newProfile) {
        this.profile = newProfile;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void unlock() {
        if (status != UserStatus.LOCKED) {
            throw new IllegalStateException("User is not locked");
        }
        
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new UserActivatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(), username
        ));
    }
    
    public void addPermission(String permission) {
        if (permission != null && !permission.trim().isEmpty() && !permissions.contains(permission)) {
            permissions.add(permission.trim());
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void removePermission(String permission) {
        if (permissions.remove(permission)) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public boolean canAccess(UserRole requiredRole) {
        return role.canAccess(requiredRole);
    }
    
    public boolean canLogin() {
        return status.canLogin() && !isLocked();
    }
    
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    public boolean isPasswordExpired() {
        return passwordChangedAt != null && 
               passwordChangedAt.isBefore(LocalDateTime.now().minusMonths(3));
    }
    
    public boolean needsPasswordChange() {
        return isPasswordExpired() || passwordChangedAt == null;
    }
    
    private boolean verifyPassword(String password) {
        // In real implementation, this would use a proper password hashing library
        // like BCrypt to verify the password against the stored hash
        return password != null && !password.isEmpty();
    }
    
    private String generateToken() {
        // In real implementation, this would generate a secure random token
        return java.util.UUID.randomUUID().toString();
    }
    
    @Override
    public UserId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public UserProfile getProfile() {
        return profile;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public List<String> getPermissions() {
        return new ArrayList<>(permissions);
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public String getActivationToken() {
        return activationToken;
    }
    
    public String getResetPasswordToken() {
        return resetPasswordToken;
    }
    
    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }
}