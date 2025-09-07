package com.pharma.inventory.entity;

/**
 * 사용자 권한
 */
public enum UserRole {
    ADMIN("관리자"),        // 전체 시스템 관리
    MANAGER("매니저"),      // 재고 관리 권한
    STAFF("직원"),          // 입출고 처리 권한
    USER("일반사용자"),     // 조회만 가능
    PHARMACIST("약사"),     // 약사 권한
    DOCTOR("의사");         // 의사 권한
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
