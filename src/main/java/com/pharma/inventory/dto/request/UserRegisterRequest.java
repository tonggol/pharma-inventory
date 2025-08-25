package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.User.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 등록 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;

    @NotBlank(message = "실명은 필수입니다")
    @Size(max = 100, message = "실명은 100자를 초과할 수 없습니다")
    private String fullName;

    @Size(max = 20, message = "사원번호는 20자를 초과할 수 없습니다")
    private String employeeId;

    @Size(max = 50, message = "부서명은 50자를 초과할 수 없습니다")
    private String department;

    @Size(max = 50, message = "직급은 50자를 초과할 수 없습니다")
    private String position;

    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈만 사용 가능합니다")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    private String phoneNumber;

    @NotNull(message = "권한은 필수입니다")
    private UserRole role;
}