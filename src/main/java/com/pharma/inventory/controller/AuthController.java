/*
*
* */package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.request.LoginRequest;
import com.pharma.inventory.dto.request.PasswordChangeRequest;
import com.pharma.inventory.dto.request.TokenRefreshRequest;
import com.pharma.inventory.dto.request.UserRegisterRequest;
import com.pharma.inventory.dto.response.TokenResponse;
import com.pharma.inventory.dto.response.UserResponse;
import com.pharma.inventory.service.AuthService;
import com.pharma.inventory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 인증 및 보안 REST API Controller
 * 로그인, 회원가입, 토큰 관리, 비밀번호 관리 등
 */
@Tag(name = "Auth", description = "인증 및 보안 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "사용자명과 비밀번호로 로그인합니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("로그인 시도 - 사용자명: {}", request.getUsername());

        try {
            TokenResponse tokenResponse = authService.login(request);

            log.info("로그인 성공 - 사용자: {}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success(tokenResponse, "로그인에 성공했습니다"));

        } catch (Exception e) {
            log.error("로그인 실패 - 사용자: {}, 사유: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요."));
        }
    }

    /**
     * 로그아웃
     */
    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화합니다")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            Authentication authentication) {

        log.info("로그아웃 - 사용자: {}", authentication.getName());

        String token = extractToken(request);
        authService.logout(token, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃되었습니다"));
    }

    /**
     * 토큰 갱신
     */
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.info("토큰 갱신 요청");

        try {
            TokenResponse tokenResponse = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(tokenResponse, "토큰이 갱신되었습니다"));

        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("토큰 갱신에 실패했습니다"));
        }
    }

    /**
     * 회원가입
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        log.info("회원가입 요청 - 사용자명: {}, 이메일: {}", request.getUsername(), request.getEmail());

        try {
            UserResponse user = authService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(user, "회원가입이 완료되었습니다"));

        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 비밀번호 찾기 (이메일로 임시 비밀번호 발송)
     */
    @Operation(summary = "비밀번호 찾기", description = "등록된 이메일로 임시 비밀번호를 발송합니다")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(
            @Parameter(description = "사용자명 또는 이메일") @RequestParam String usernameOrEmail) {

        log.info("비밀번호 찾기 요청 - 입력값: {}", usernameOrEmail);

        try {
            String email = authService.resetPassword(usernameOrEmail);

            Map<String, String> response = Map.of(
                    "message", "임시 비밀번호가 이메일로 발송되었습니다",
                    "email", maskEmail(email)
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("비밀번호 찾기 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자를 찾을 수 없습니다"));
        }
    }

    /**
     * 비밀번호 재설정
     */
    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 재설정합니다")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "재설정 토큰") @RequestParam String token,
            @Parameter(description = "새 비밀번호") @RequestParam String newPassword) {

        log.info("비밀번호 재설정 요청");

        try {
            authService.resetPasswordWithToken(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 재설정되었습니다"));

        } catch (Exception e) {
            log.error("비밀번호 재설정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("비밀번호 재설정에 실패했습니다"));
        }
    }

    /**
     * 비밀번호 변경
     */
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {

        log.info("비밀번호 변경 요청 - 사용자: {}", authentication.getName());

        // 비밀번호 확인 일치 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("새 비밀번호와 확인 비밀번호가 일치하지 않습니다"));
        }

        try {
            authService.changePassword(
                    authentication.getName(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다"));

        } catch (IllegalArgumentException e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 이메일 인증
     */
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰을 확인합니다")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "인증 토큰") @RequestParam String token) {

        log.info("이메일 인증 요청 - 토큰: {}", token);

        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다"));

        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("이메일 인증에 실패했습니다"));
        }
    }

    /**
     * 현재 사용자 정보 조회
     */
    @Operation(summary = "현재 사용자 정보", description = "로그인한 사용자의 정보를 조회합니다")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {

        log.info("현재 사용자 정보 조회 - 사용자: {}", authentication.getName());

        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 활성 세션 조회
     */
    @Operation(summary = "활성 세션 조회", description = "현재 사용자의 활성 세션 목록을 조회합니다")
    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SessionInfo>>> getActiveSessions(
            Authentication authentication) {

        log.info("활성 세션 조회 - 사용자: {}", authentication.getName());

        List<SessionInfo> sessions = authService.getActiveSessions(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(sessions,
                String.format("%d개의 활성 세션이 있습니다", sessions.size())));
    }

    /**
     * 세션 종료
     */
    @Operation(summary = "세션 종료", description = "특정 세션을 종료합니다")
    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> terminateSession(
            @Parameter(description = "세션 ID", required = true) @PathVariable String sessionId,
            Authentication authentication) {

        log.info("세션 종료 요청 - 사용자: {}, 세션: {}", authentication.getName(), sessionId);

        authService.terminateSession(authentication.getName(), sessionId);

        return ResponseEntity.ok(ApiResponse.success(null, "세션이 종료되었습니다"));
    }

    /**
     * 모든 세션 종료 (현재 세션 제외)
     */
    @Operation(summary = "모든 세션 종료", description = "현재 세션을 제외한 모든 세션을 종료합니다")
    @DeleteMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> terminateAllSessions(
            HttpServletRequest request,
            Authentication authentication) {

        log.info("모든 세션 종료 요청 - 사용자: {}", authentication.getName());

        String currentToken = extractToken(request);
        int terminatedCount = authService.terminateAllSessions(authentication.getName(), currentToken);

        return ResponseEntity.ok(ApiResponse.success(terminatedCount,
                String.format("%d개의 세션이 종료되었습니다", terminatedCount)));
    }

    /**
     * 토큰 유효성 검증
     */
    @Operation(summary = "토큰 검증", description = "액세스 토큰의 유효성을 검증합니다")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            HttpServletRequest request) {

        String token = extractToken(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("토큰이 없습니다"));
        }

        TokenValidationResponse validation = authService.validateToken(token);

        if (validation.isValid()) {
            return ResponseEntity.ok(ApiResponse.success(validation));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("유효하지 않은 토큰입니다"));
        }
    }

    /**
     * 사용자 권한 확인
     */
    @Operation(summary = "권한 확인", description = "현재 사용자의 권한을 확인합니다")
    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPermissions>> getPermissions(Authentication authentication) {

        log.info("권한 확인 - 사용자: {}", authentication.getName());

        UserPermissions permissions = authService.getUserPermissions(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Request에서 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 이메일 마스킹
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];

        if (localPart.length() <= 2) {
            return "**@" + parts[1];
        }

        return localPart.substring(0, 2) + "****@" + parts[1];
    }

    /**
     * 세션 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionInfo {
        private String sessionId;
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime lastAccessedAt;
        private boolean current;
    }

    /**
     * 토큰 검증 응답 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String role;
        private Long expiresIn;  // 남은 시간 (초)
        private java.time.LocalDateTime issuedAt;
        private java.time.LocalDateTime expiresAt;
    }

    /**
     * 사용자 권한 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserPermissions {
        private String username;
        private String role;
        private List<String> authorities;
        private Map<String, Boolean> permissions;

        // 주요 권한 플래그
        private boolean canManageMedicine;
        private boolean canManageStock;
        private boolean canManageUsers;
        private boolean canViewReports;
        private boolean canApproveTransactions;
        private boolean canExport;
    }
}