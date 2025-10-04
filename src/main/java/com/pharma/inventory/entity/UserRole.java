package com.pharma.inventory.entity;

/**
 * 사용자 권한
 */
public enum UserRole {
    ADMIN("管理者"),        // 전체 시스템 관리
    MANAGER("マネージャー"),      // 재고 관리 권한
    STAFF("スタッフ"),          // 입출고 처리 권한
    USER("ユーザー"),     // 조회만 가능
    PHARMACIST("薬剤師"),     // 약사 권한
    DOCTOR("医者");         // 의사 권한
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
