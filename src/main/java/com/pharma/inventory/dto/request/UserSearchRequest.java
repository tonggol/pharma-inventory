package com.pharma.inventory.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 사용자 검색 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {

    /**
     * 통합 검색 키워드 (사용자ID, 이름, 이메일)
     */
    private String keyword;

    /**
     * 권한 필터
     */
    private String role;

    /**
     * 부서 필터
     */
    private String department;

    /**
     * 상태 필터 (ACTIVE, INACTIVE, LOCKED)
     */
    private String status;

    /**
     * 등록일 시작
     */
    private String startDate;

    /**
     * 등록일 끝
     */
    private String endDate;

    /**
     * 정렬 필드
     */
    private String sortBy = "createdAt";

    /**
     * 정렬 방향 (asc, desc)
     */
    private String sortDirection = "desc";
}