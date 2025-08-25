package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
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
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 사용자 권한 Enum
     */
    public enum UserRole {
        ADMIN("관리자"),        // 전체 시스템 관리
        MANAGER("매니저"),      // 재고 관리 권한
        USER("일반사용자"),     // 조회만 가능
        PHARMACIST("약사"),     // 약사 권한
        DOCTOR("의사");         // 의사 권한
        
        private final String description;
        
        UserRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        passwordChangedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 비밀번호 변경 시 호출
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }
    
    /**
     * 로그인 시 호출
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
