package com.pharma.inventory.repository;

import com.pharma.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository
 * 사용자 데이터 접근을 위한 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 조회
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 사원번호로 조회
     */
    Optional<User> findByEmployeeId(String employeeId);
    
    /**
     * 부서별 사용자 조회
     */
    List<User> findByDepartment(String department);
    
    /**
     * 권한별 사용자 조회
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 활성 사용자만 조회
     */
    List<User> findByIsActiveTrue();
    
    /**
     * 비활성 사용자 조회
     */
    List<User> findByIsActiveFalse();
    
    /**
     * 사용자명 중복 체크
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);
    
    /**
     * 사원번호 중복 체크
     */
    boolean existsByEmployeeId(String employeeId);
    
    /**
     * 이름으로 검색 (부분 일치)
     */
    List<User> findByFullNameContainingIgnoreCase(String fullName);
    
    /**
     * 로그인 시도 - 사용자명과 활성 상태 확인
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    /**
     * 마지막 로그인 시간 업데이트
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * 비밀번호 변경이 필요한 사용자 조회 (90일 이상)
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :dateThreshold")
    List<User> findUsersNeedPasswordChange(@Param("dateThreshold") LocalDateTime dateThreshold);
    
    /**
     * 부서와 권한으로 사용자 조회
     */
    List<User> findByDepartmentAndRole(String department, User.UserRole role);
    
    /**
     * 최근 로그인한 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyLoggedInUsers(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 권한별 사용자 수 집계
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    /**
     * 부서별 사용자 수 집계
     */
    @Query("SELECT u.department, COUNT(u) FROM User u GROUP BY u.department")
    List<Object[]> countUsersByDepartment();
    
    /**
     * 특정 기간 동안 로그인하지 않은 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :inactiveDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("inactiveDate") LocalDateTime inactiveDate);
}
