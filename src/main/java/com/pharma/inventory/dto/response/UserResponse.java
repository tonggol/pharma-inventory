package com.pharma.inventory.dto.response;

import com.pharma.inventory.entity.User;
import com.pharma.inventory.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String employeeId;
    private String department;
    private String departmentDescription;
    private String position;
    private String phoneNumber;
    private UserRole role;
    private String roleDescription;

    // 활성 상태 관련 필드 추가
    private Boolean isActive;
    private String statusDescription;

    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .departmentDescription(getDepartmentDescription(user.getDepartment()))
                .position(user.getPosition())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .roleDescription(user.getRole() != null ? user.getRole().getDescription() : "알 수 없음")

                // 활성 상태 매핑
                .isActive(user.getIsActive())
                .statusDescription(getStatusDescription(user.getIsActive()))

                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 부서 설명 생성 헬퍼 메서드
     */
    private static String getDepartmentDescription(String department) {
        if (department == null || department.trim().isEmpty()) {
            return "-";
        }

        // 부서 코드를 한글명으로 변환
        switch (department.toUpperCase()) {
            case "PHARMACY": return "약국";
            case "WAREHOUSE": return "창고";
            case "ADMIN": return "관리부서";
            case "IT": return "IT부서";
            case "MEDICAL": return "의료부서";
            case "FINANCE": return "재무부서";
            default: return department; // 원본 그대로 반환
        }
    }

    /**
     * 상태 설명 생성 헬퍼 메서드
     */
    private static String getStatusDescription(Boolean isActive) {
        if (isActive == null) {
            return "알 수 없음";
        }
        return isActive ? "활성" : "비활성";
    }

    /**
     * 온라인 상태 확인 (최근 30분 이내 로그인)
     */
    public boolean isOnline() {
        if (lastLoginAt == null || !Boolean.TRUE.equals(isActive)) {
            return false;
        }
        return lastLoginAt.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    /**
     * 계정 상태 enum 반환
     */
    public UserStatus getStatus() {
        if (!Boolean.TRUE.equals(isActive)) {
            return UserStatus.INACTIVE;
        }
        if (isOnline()) {
            return UserStatus.ONLINE;
        }
        return UserStatus.OFFLINE;
    }

    /**
     * 사용자 상태 enum
     */
    public enum UserStatus {
        ONLINE("온라인"),
        OFFLINE("오프라인"),
        INACTIVE("비활성");

        private final String description;

        UserStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}