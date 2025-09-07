package com.pharma.inventory.repository;

import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 의약품 Repository 커스텀 인터페이스
 * QueryDSL을 사용한 동적 쿼리 정의
 */
public interface MedicineRepositoryCustom {
    
    /**
     * 의약품 검색 (동적 쿼리)
     */
    Page<Medicine> searchMedicines(MedicineSearchRequest searchRequest, Pageable pageable);
    
    /**
     * 재고 부족 의약품 조회
     */
    List<Medicine> findLowStockMedicines();
    
    /**
     * 카테고리별 의약품 통계
     */
    List<MedicineCategoryStats> getMedicineStatsByCategory();
    
    /**
     * 복잡한 조건의 의약품 검색
     */
    List<Medicine> findMedicinesWithComplexCondition(String manufacturer, 
                                                     Integer minStock, 
                                                     Boolean isActive);
    
    /**
     * 카테고리별 통계 DTO
     */
    class MedicineCategoryStats {
        private String category;
        private Long count;
        private Long activeCount;
        private Long lowStockCount;
        
        public MedicineCategoryStats(String category, Long count, Long activeCount, Long lowStockCount) {
            this.category = category;
            this.count = count;
            this.activeCount = activeCount;
            this.lowStockCount = lowStockCount;
        }
        
        // Getters
        public String getCategory() { return category; }
        public Long getCount() { return count; }
        public Long getActiveCount() { return activeCount; }
        public Long getLowStockCount() { return lowStockCount; }
    }
}
