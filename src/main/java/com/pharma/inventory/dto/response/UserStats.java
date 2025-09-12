package com.pharma.inventory.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 사용자 통계 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    /**
     * 전체 사용자 수
     */
    private Long totalUsers;

    /**
     * 활성 사용자 수
     */
    private Long activeUsers;

    /**
     * 관리자 수
     */
    private Long adminUsers;

    /**
     * 비활성 사용자 수
     */
    private Long inactiveUsers;

    /**
     * 오늘 가입한 사용자 수
     */
    private Long todayRegistrations;

    /**
     * 오늘 로그인한 사용자 수
     */
    private Long todayLogins;

    /**
     * 이번 주 가입한 사용자 수
     */
    private Long weeklyRegistrations;

    /**
     * 이번 달 가입한 사용자 수
     */
    private Long monthlyRegistrations;
}