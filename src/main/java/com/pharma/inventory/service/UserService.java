package com.pharma.inventory.service;

import com.pharma.inventory.dto.request.UserRegisterRequest;
import com.pharma.inventory.dto.request.UserUpdateRequest;
import com.pharma.inventory.dto.response.UserResponse;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 Service
 * 사용자 관리 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 목록 조회 (페이징)
     */
    public Page<UserResponse> getUsers(Pageable pageable) {
        log.debug("사용자 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserResponse::from);
    }

    /**
     * 활성 사용자 목록 조회
     */
    public List<UserResponse> getActiveUsers() {
        log.debug("활성 사용자 목록 조회");

        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 상세 조회 (ID)
     */
    public UserResponse getUser(Long id) {
        log.debug("사용자 상세 조회 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        return UserResponse.from(user);
    }

    /**
     * 사용자 상세 조회 (사용자명)
     */
    public UserResponse getUserByUsername(String username) {
        log.debug("사용자 조회 - 사용자명: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. 사용자명: " + username));

        return UserResponse.from(user);
    }

    /**
     * 사용자 상세 조회 (이메일)
     */
    public UserResponse getUserByEmail(String email) {
        log.debug("사용자 조회 - 이메일: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. 이메일: " + email));

        return UserResponse.from(user);
    }

    /**
     * 사용자 등록
     */
    @Transactional
    public UserResponse createUser(UserRegisterRequest request) {
        log.info("사용자 등록 - 사용자명: {}, 이메일: {}", request.getUsername(), request.getEmail());

        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + request.getEmail());
        }

        if (request.getEmployeeId() != null && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("이미 등록된 사원번호입니다: " + request.getEmployeeId());
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
        log.info("사용자 등록 완료 - ID: {}, 사용자명: {}", savedUser.getId(), savedUser.getUsername());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("사용자 수정 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        // 이메일 중복 검사 (변경 시)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 수정 가능한 필드 업데이트
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 수정 완료 - ID: {}", updatedUser.getId());

        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 삭제 (비활성화)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("사용자 비활성화 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("사용자 비활성화 완료 - ID: {}", id);
    }

    /**
     * 사용자 활성화
     */
    @Transactional
    public UserResponse activateUser(Long id) {
        log.info("사용자 활성화 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("사용자 활성화 완료 - ID: {}", id);

        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 - 사용자 ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 설정
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("비밀번호 변경 완료 - 사용자 ID: {}", id);
    }

    /**
     * 비밀번호 초기화
     */
    @Transactional
    public String resetPassword(Long id) {
        log.info("비밀번호 초기화 - 사용자 ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

        // 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        log.info("비밀번호 초기화 완료 - 사용자 ID: {}", id);

        return tempPassword;
    }

    /**
     * 부서별 사용자 조회
     */
    public List<UserResponse> getUsersByDepartment(String department) {
        log.debug("부서별 사용자 조회 - 부서: {}", department);

        List<User> users = userRepository.findByDepartment(department);
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 권한별 사용자 조회
     */
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        log.debug("권한별 사용자 조회 - 권한: {}", role);

        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 검색 (이름)
     */
    public List<UserResponse> searchUsersByName(String name) {
        log.debug("사용자 검색 - 이름: {}", name);

        List<User> users = userRepository.findByFullNameContainingIgnoreCase(name);
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 비밀번호 변경이 필요한 사용자 조회 (90일 이상)
     */
    public List<UserResponse> getUsersNeedPasswordChange() {
        log.debug("비밀번호 변경 필요 사용자 조회");

        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        List<User> users = userRepository.findUsersNeedPasswordChange(threshold);

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 비활성 사용자 조회 (30일 이상 미접속)
     */
    public List<UserResponse> getInactiveUsers(int days) {
        log.debug("비활성 사용자 조회 - 기준: {}일", days);

        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<User> users = userRepository.findInactiveUsers(threshold);

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 통계
     */
    public UserStatistics getUserStatistics() {
        log.debug("사용자 통계 조회");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByIsActiveTrue().size();
        long inactiveUsers = totalUsers - activeUsers;

        // 권한별 사용자 수
        List<Object[]> roleStats = userRepository.countUsersByRole();

        // 부서별 사용자 수
        List<Object[]> deptStats = userRepository.countUsersByDepartment();

        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .usersByRole(convertToMap(roleStats))
                .usersByDepartment(convertToMap(deptStats))
                .build();
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByEmployeeId(String employeeId) {
        return userRepository.existsByEmployeeId(employeeId);
    }

    // === Private Helper Methods ===

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private java.util.Map<String, Long> convertToMap(List<Object[]> stats) {
        java.util.Map<String, Long> map = new java.util.HashMap<>();
        for (Object[] stat : stats) {
            String key = stat[0].toString();
            Long value = ((Number) stat[1]).longValue();
            map.put(key, value);
        }
        return map;
    }

    /**
     * 사용자 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private java.util.Map<String, Long> usersByRole;
        private java.util.Map<String, Long> usersByDepartment;
    }
}