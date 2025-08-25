package com.pharma.inventory.service;

import com.pharma.inventory.controller.AuthController;
import com.pharma.inventory.dto.request.LoginRequest;
import com.pharma.inventory.dto.request.TokenRefreshRequest;
import com.pharma.inventory.dto.request.UserRegisterRequest;
import com.pharma.inventory.dto.response.TokenResponse;
import com.pharma.inventory.dto.response.UserResponse;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.UserRepository;
import com.pharma.inventory.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인증 Service (간소화 버전)
 * 로그인, 토큰 관리, 비밀번호 관리 등 인증 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:86400}")
    private long refreshTokenValiditySeconds;

    // 메모리 기반 임시 저장소 (실제 운영에서는 Redis 권장)
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 처리 - 사용자: {}", request.getUsername());

        // 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 활성 상태 확인
        if (!user.getIsActive()) {
            throw new IllegalStateException("비활성화된 계정입니다");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // 리프레시 토큰 저장
        refreshTokenStore.put(user.getUsername(), refreshToken);

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user);

        // 사용자 정보 포함 응답 생성
        TokenResponse.UserInfo userInfo = TokenResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .build();

        return TokenResponse.withUserInfo(
                accessToken,
                refreshToken,
                accessTokenValiditySeconds,
                userInfo
        );
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String token, String username) {
        log.info("로그아웃 처리 - 사용자: {}", username);

        if (token != null) {
            // 토큰 블랙리스트에 추가
            blacklistedTokens.add(token);
        }

        // 리프레시 토큰 삭제
        refreshTokenStore.remove(username);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    /**
     * 토큰 갱신
     */
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        log.debug("토큰 갱신 처리");

        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다");
        }

        // 사용자 정보 추출
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 저장된 리프레시 토큰과 비교
        String savedToken = refreshTokenStore.get(username);
        if (!refreshToken.equals(savedToken)) {
            throw new IllegalArgumentException("일치하지 않는 리프레시 토큰입니다");
        }

        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 새 액세스 토큰 생성
        Authentication authentication = createAuthentication(user);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);

        return TokenResponse.of(newAccessToken, refreshToken, accessTokenValiditySeconds);
    }

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        log.info("회원가입 처리 - 사용자명: {}", request.getUsername());

        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .position(request.getPosition())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    /**
     * 비밀번호 재설정 (임시 비밀번호 생성)
     */
    @Transactional
    public String resetPassword(String usernameOrEmail) {
        log.info("비밀번호 재설정 요청 - 입력: {}", usernameOrEmail);

        // 사용자 조회
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

        // 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // 실제로는 이메일로 발송해야 하지만, 간소화를 위해 로그로 출력
        log.info("임시 비밀번호 발급 - 사용자: {}, 임시 비밀번호: {}", user.getUsername(), tempPassword);

        return user.getEmail();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 - 사용자: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 설정
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 토큰 검증
     */
    public AuthController.TokenValidationResponse validateToken(String token) {
        boolean isValid = jwtTokenProvider.validateToken(token) && !blacklistedTokens.contains(token);

        if (isValid) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);

            long expiresIn = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            User user = userRepository.findByUsername(username).orElse(null);
            String role = user != null ? user.getRole().name() : null;

            return AuthController.TokenValidationResponse.builder()
                    .valid(true)
                    .username(username)
                    .role(role)
                    .expiresIn(expiresIn)
                    .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                    .build();
        }

        return AuthController.TokenValidationResponse.builder()
                .valid(false)
                .build();
    }

    /**
     * 사용자 권한 조회
     */
    public AuthController.UserPermissions getUserPermissions(String username) {
        log.debug("사용자 권한 조회 - 사용자: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        List<String> authorities = new ArrayList<>();
        authorities.add("ROLE_" + user.getRole().name());

        Map<String, Boolean> permissions = new HashMap<>();

        // 역할별 권한 설정
        switch (user.getRole()) {
            case ADMIN:
                permissions.put("ALL", true);
                return AuthController.UserPermissions.builder()
                        .username(username)
                        .role(user.getRole().name())
                        .authorities(authorities)
                        .permissions(permissions)
                        .canManageMedicine(true)
                        .canManageStock(true)
                        .canManageUsers(true)
                        .canViewReports(true)
                        .canApproveTransactions(true)
                        .canExport(true)
                        .build();

            case MANAGER:
                return AuthController.UserPermissions.builder()
                        .username(username)
                        .role(user.getRole().name())
                        .authorities(authorities)
                        .permissions(permissions)
                        .canManageMedicine(true)
                        .canManageStock(true)
                        .canManageUsers(false)
                        .canViewReports(true)
                        .canApproveTransactions(true)
                        .canExport(true)
                        .build();

            case PHARMACIST:
                return AuthController.UserPermissions.builder()
                        .username(username)
                        .role(user.getRole().name())
                        .authorities(authorities)
                        .permissions(permissions)
                        .canManageMedicine(true)
                        .canManageStock(true)
                        .canManageUsers(false)
                        .canViewReports(true)
                        .canApproveTransactions(false)
                        .canExport(false)
                        .build();

            default:
                return AuthController.UserPermissions.builder()
                        .username(username)
                        .role(user.getRole().name())
                        .authorities(authorities)
                        .permissions(permissions)
                        .canManageMedicine(false)
                        .canManageStock(false)
                        .canManageUsers(false)
                        .canViewReports(true)
                        .canApproveTransactions(false)
                        .canExport(false)
                        .build();
        }
    }

    /**
     * 간단한 세션 정보 (메모리 기반)
     */
    public List<AuthController.SessionInfo> getActiveSessions(String username) {
        // 간소화된 버전 - 단일 세션만 반환
        List<AuthController.SessionInfo> sessions = new ArrayList<>();

        if (refreshTokenStore.containsKey(username)) {
            sessions.add(AuthController.SessionInfo.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .current(true)
                    .build());
        }

        return sessions;
    }

    /**
     * 세션 종료 (간소화)
     */
    @Transactional
    public void terminateSession(String username, String sessionId) {
        log.info("세션 종료 - 사용자: {}", username);
        refreshTokenStore.remove(username);
    }

    /**
     * 모든 세션 종료 (간소화)
     */
    @Transactional
    public int terminateAllSessions(String username, String exceptToken) {
        log.info("모든 세션 종료 - 사용자: {}", username);

        if (refreshTokenStore.containsKey(username)) {
            refreshTokenStore.remove(username);
            return 1;
        }
        return 0;
    }

    // 스텁 메서드들 (EmailService 없이)
    public void resetPasswordWithToken(String token, String newPassword) {
        log.warn("비밀번호 재설정 토큰 기능은 구현되지 않았습니다");
        throw new UnsupportedOperationException("이 기능은 현재 지원되지 않습니다");
    }

    public void verifyEmail(String token) {
        log.warn("이메일 인증 기능은 구현되지 않았습니다");
        throw new UnsupportedOperationException("이 기능은 현재 지원되지 않습니다");
    }

    // === Private Helper Methods ===

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private Authentication createAuthentication(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                authorities
        );
    }
}