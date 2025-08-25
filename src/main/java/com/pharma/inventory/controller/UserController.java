package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.common.PageRequest;
import com.pharma.inventory.dto.common.PageResponse;
import com.pharma.inventory.dto.request.PasswordChangeRequest;
import com.pharma.inventory.dto.request.UserRegisterRequest;
import com.pharma.inventory.dto.request.UserUpdateRequest;
import com.pharma.inventory.dto.response.UserResponse;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 사용자 관리 REST API Controller
 * 사용자 CRUD, 프로필 관리, 권한 관리 등
 */
@Tag(name = "User", description = "사용자 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 목록 조회 (페이징)
     */
    @Operation(summary = "사용자 목록 조회", description = "페이징된 사용자 목록을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction) {

        log.info("사용자 목록 조회 - page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(org.springframework.data.domain.Sort.Direction.valueOf(direction))
                .build();

        Page<UserResponse> users = userService.getUsers(pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }

    /**
     * 활성 사용자 목록 조회
     */
    @Operation(summary = "활성 사용자 목록", description = "활성화된 사용자 목록을 조회합니다")
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {

        log.info("활성 사용자 목록 조회");

        List<UserResponse> users = userService.getActiveUsers();

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%d명의 활성 사용자가 있습니다", users.size())));
    }

    /**
     * 사용자 상세 조회
     */
    @Operation(summary = "사용자 상세 조회", description = "ID로 사용자 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {

        log.info("사용자 상세 조회 - ID: {}", id);

        UserResponse user = userService.getUser(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 사용자 등록
     */
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRegisterRequest request) {

        log.info("사용자 등록 - 사용자명: {}, 이메일: {}", request.getUsername(), request.getEmail());

        UserResponse user = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "사용자가 성공적으로 등록되었습니다"));
    }

    /**
     * 사용자 정보 수정
     */
    @Operation(summary = "사용자 수정", description = "사용자 정보를 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("사용자 수정 - ID: {}", id);

        UserResponse user = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.success(user, "사용자 정보가 수정되었습니다"));
    }

    /**
     * 사용자 삭제 (비활성화)
     */
    @Operation(summary = "사용자 비활성화", description = "사용자를 비활성화합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {

        log.info("사용자 비활성화 - ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "사용자가 비활성화되었습니다"));
    }

    /**
     * 사용자 활성화
     */
    @Operation(summary = "사용자 활성화", description = "비활성화된 사용자를 활성화합니다")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {

        log.info("사용자 활성화 - ID: {}", id);

        UserResponse user = userService.activateUser(id);

        return ResponseEntity.ok(ApiResponse.success(user, "사용자가 활성화되었습니다"));
    }

    /**
     * 내 프로필 조회
     */
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {

        log.info("프로필 조회 - 사용자: {}", authentication.getName());

        UserResponse user = userService.getUserByUsername(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 내 프로필 수정
     */
    @Operation(summary = "내 프로필 수정", description = "로그인한 사용자의 프로필을 수정합니다")
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        log.info("프로필 수정 - 사용자: {}", authentication.getName());

        // 현재 사용자 정보 조회
        UserResponse currentUser = userService.getUserByUsername(authentication.getName());

        // 프로필 수정 (권한 변경 불가)
        request.setRole(null);  // 자신의 권한은 변경할 수 없음
        request.setIsActive(null);  // 활성 상태도 변경할 수 없음

        UserResponse updatedUser = userService.updateUser(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(updatedUser, "프로필이 수정되었습니다"));
    }

    /**
     * 비밀번호 변경
     */
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {

        log.info("비밀번호 변경 - 사용자: {}", authentication.getName());

        // 비밀번호 확인 일치 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("새 비밀번호와 확인 비밀번호가 일치하지 않습니다"));
        }

        UserResponse user = userService.getUserByUsername(authentication.getName());
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다"));
    }

    /**
     * 사용자 비밀번호 초기화 (관리자)
     */
    @Operation(summary = "비밀번호 초기화", description = "사용자의 비밀번호를 초기화합니다")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetUserPassword(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {

        log.info("비밀번호 초기화 - 사용자 ID: {}", id);

        String tempPassword = userService.resetPassword(id);

        Map<String, String> response = Map.of(
                "message", "비밀번호가 초기화되었습니다",
                "tempPassword", tempPassword
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 부서별 사용자 조회
     */
    @Operation(summary = "부서별 사용자 조회", description = "특정 부서의 사용자 목록을 조회합니다")
    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByDepartment(
            @Parameter(description = "부서명", required = true) @PathVariable String department) {

        log.info("부서별 사용자 조회 - 부서: {}", department);

        List<UserResponse> users = userService.getUsersByDepartment(department);

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%s 부서에 %d명의 사용자가 있습니다", department, users.size())));
    }

    /**
     * 권한별 사용자 조회
     */
    @Operation(summary = "권한별 사용자 조회", description = "특정 권한의 사용자 목록을 조회합니다")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @Parameter(description = "권한", required = true) @PathVariable String role) {

        log.info("권한별 사용자 조회 - 권한: {}", role);

        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        List<UserResponse> users = userService.getUsersByRole(userRole);

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%s 권한을 가진 %d명의 사용자가 있습니다", role, users.size())));
    }

    /**
     * 사용자 검색
     */
    @Operation(summary = "사용자 검색", description = "이름으로 사용자를 검색합니다")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "검색어 (이름)") @RequestParam String name) {

        log.info("사용자 검색 - 이름: {}", name);

        List<UserResponse> users = userService.searchUsersByName(name);

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%d명의 사용자가 검색되었습니다", users.size())));
    }

    /**
     * 비밀번호 변경 필요 사용자 조회
     */
    @Operation(summary = "비밀번호 변경 필요 사용자", description = "90일 이상 비밀번호를 변경하지 않은 사용자를 조회합니다")
    @GetMapping("/password-change-required")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersNeedPasswordChange() {

        log.info("비밀번호 변경 필요 사용자 조회");

        List<UserResponse> users = userService.getUsersNeedPasswordChange();

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%d명의 사용자가 비밀번호 변경이 필요합니다", users.size())));
    }

    /**
     * 비활성 사용자 조회
     */
    @Operation(summary = "비활성 사용자 조회", description = "지정 기간 동안 로그인하지 않은 사용자를 조회합니다")
    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers(
            @Parameter(description = "기준 일수 (기본 30일)") @RequestParam(defaultValue = "30") int days) {

        log.info("비활성 사용자 조회 - 기준: {}일", days);

        List<UserResponse> users = userService.getInactiveUsers(days);

        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("%d명의 사용자가 %d일 이상 로그인하지 않았습니다", users.size(), days)));
    }

    /**
     * 사용자 통계
     */
    @Operation(summary = "사용자 통계", description = "사용자 관련 통계 정보를 조회합니다")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserService.UserStatistics>> getUserStatistics() {

        log.info("사용자 통계 조회");

        UserService.UserStatistics statistics = userService.getUserStatistics();

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 사용자 권한 조회
     */
    @Operation(summary = "사용자 권한 조회", description = "특정 사용자의 권한을 조회합니다")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserPermissionInfo>> getUserPermissions(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {

        log.info("사용자 권한 조회 - ID: {}", id);

        UserResponse user = userService.getUser(id);

        UserPermissionInfo permissions = UserPermissionInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .roleDescription(user.getRoleDescription())
                .permissions(getPermissionsByRole(user.getRole()))
                .build();

        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * 사용자 역할 변경
     */
    @Operation(summary = "사용자 역할 변경", description = "사용자의 역할을 변경합니다")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id,
            @Parameter(description = "새 역할") @RequestParam String role) {

        log.info("사용자 역할 변경 - ID: {}, 새 역할: {}", id, role);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .role(User.UserRole.valueOf(role.toUpperCase()))
                .build();

        UserResponse user = userService.updateUser(id, updateRequest);

        return ResponseEntity.ok(ApiResponse.success(user,
                String.format("사용자 역할이 %s로 변경되었습니다", role)));
    }

    /**
     * 역할별 권한 조회
     */
    private Map<String, Boolean> getPermissionsByRole(User.UserRole role) {
        Map<String, Boolean> permissions = new java.util.HashMap<>();

        switch (role) {
            case ADMIN:
                permissions.put("canManageUsers", true);
                permissions.put("canManageMedicine", true);
                permissions.put("canManageStock", true);
                permissions.put("canViewReports", true);
                permissions.put("canExport", true);
                permissions.put("canApproveTransactions", true);
                break;
            case MANAGER:
                permissions.put("canManageUsers", false);
                permissions.put("canManageMedicine", true);
                permissions.put("canManageStock", true);
                permissions.put("canViewReports", true);
                permissions.put("canExport", true);
                permissions.put("canApproveTransactions", true);
                break;
            case PHARMACIST:
                permissions.put("canManageUsers", false);
                permissions.put("canManageMedicine", true);
                permissions.put("canManageStock", true);
                permissions.put("canViewReports", true);
                permissions.put("canExport", false);
                permissions.put("canApproveTransactions", false);
                break;
            default:
                permissions.put("canManageUsers", false);
                permissions.put("canManageMedicine", false);
                permissions.put("canManageStock", false);
                permissions.put("canViewReports", true);
                permissions.put("canExport", false);
                permissions.put("canApproveTransactions", false);
        }

        return permissions;
    }

    /**
     * 사용자 권한 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserPermissionInfo {
        private Long userId;
        private String username;
        private String role;
        private String roleDescription;
        private Map<String, Boolean> permissions;
    }
}