package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 사용자 엔티티
 * 시스템 사용자 정보 및 권한 관리
 */
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_username", columnList = "username", unique = true),
           @Index(name = "idx_email", columnList = "email", unique = true)
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;  // 사용자명 (로그인 ID)
    
    @Column(nullable = false)
    private String password;  // 비밀번호 (암호화)
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;  // 이메일
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;  // 실명
    
    @Column(name = "employee_id", length = 20)
    private String employeeId;  // 사원번호
    
    @Column(length = 50)
    private String department;  // 부서
    
    @Column(length = 50)
    private String position;  // 직급
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;  // 연락처
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;  // 권한
    
    @Column(name = "is_active")
    private Boolean isActive = true;  // 활성 상태
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;  // 비밀번호 변경 시간
    
    // === 생성자 ===
    public User(String username, String password, String email, String fullName, UserRole role) {
        validateUser(username, password, email, fullName);
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role != null ? role : UserRole.USER;
        this.isActive = true;
        this.passwordChangedAt = LocalDateTime.now();
    }

    public Long getId() {
        return this.id;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }
    
    // === 비즈니스 메소드 ===
    
    /**
     * 사용자 정보 업데이트
     */
    public void updateUserInfo(String fullName, String department, String position, String phoneNumber) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
        }
        this.department = department;
        this.position = position;
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * 이메일 업데이트
     */
    public void updateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("유효한 이메일 주소를 입력해주세요");
        }
        this.email = email;
    }
    
    /**
     * 역할 변경
     */
    public void updateRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("역할은 필수입니다");
        }
        this.role = role;
    }
    
    /**
     * 활성/비활성 상태 변경
     */
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * 비밀번호 변경
     */
    public void updatePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }
    
    /**
     * 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    /**
     * 계정 활성화/비활성화
     */
    public void setActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
    
    // === Validation ===
    private void validateUser(String username, String password, String email, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자명은 필수입니다");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("유효한 이메일 주소가 필요합니다");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("실명은 필수입니다");
        }
    }
    
    // === UserDetails 구현 ===
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        // 90일 후 비밀번호 만료
        return passwordChangedAt.plusDays(90).isAfter(LocalDateTime.now());
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }
    
    // === equals & hashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    // === toString ===
    @Override
    public String toString() {
        return String.format("User[id=%d, username='%s', role=%s]", 
            id, username, role);
    }
}
