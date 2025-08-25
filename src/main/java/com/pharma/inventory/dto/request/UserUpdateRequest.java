package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.User.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;

    @Size(max = 100, message = "실명은 100자를 초과할 수 없습니다")
    private String fullName;

    @Size(max = 50, message = "부서명은 50자를 초과할 수 없습니다")
    private String department;

    @Size(max = 50, message = "직급은 50자를 초과할 수 없습니다")
    private String position;

    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈만 사용 가능합니다")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    private String phoneNumber;

    private UserRole role;

    private Boolean isActive;
}