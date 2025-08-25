package com.pharma.inventory.repository;

import com.pharma.inventory.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 의약품 Repository
 * 의약품 데이터 접근을 위한 인터페이스
 */
@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long>, JpaSpecificationExecutor<Medicine> {
    
    /**
     * 의약품 코드로 조회
     */
    Optional<Medicine> findByCode(String code);
    
    /**
     * 의약품명으로 검색 (부분 일치)
     */
    List<Medicine> findByNameContainingIgnoreCase(String name);
    
    /**
     * 제조사로 검색
     */
    List<Medicine> findByManufacturer(String manufacturer);
    
    /**
     * 카테고리로 검색
     */
    List<Medicine> findByCategory(String category);
    
    /**
     * 활성 상태인 의약품만 조회
     */
    List<Medicine> findByIsActiveTrue();
    
    /**
     * 최소 재고 수량 이하인 의약품 조회
     */
    @Query("SELECT m FROM Medicine m WHERE m.minStockQuantity >= :threshold")
    List<Medicine> findMedicinesWithLowStockThreshold(@Param("threshold") Integer threshold);
    
    /**
     * 처방전 필요 여부로 검색
     */
    List<Medicine> findByIsPrescriptionRequired(Boolean isPrescriptionRequired);
    
    /**
     * 보관 조건으로 검색
     */
    List<Medicine> findByStorageCondition(String storageCondition);
    
    /**
     * 코드 중복 체크
     */
    boolean existsByCode(String code);
    
    /**
     * 복합 검색 - 이름, 코드, 제조사로 검색
     */
    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicine> searchMedicines(@Param("keyword") String keyword);
    
    /**
     * 카테고리별 의약품 수 집계
     */
    @Query("SELECT m.category, COUNT(m) FROM Medicine m GROUP BY m.category")
    List<Object[]> countByCategory();
    
    /**
     * 활성 의약품 수 조회
     */
    long countByIsActiveTrue();
    
    /**
     * 재고 부족 의약품 조회
     */
    @Query("""
        SELECT DISTINCT m FROM Medicine m
        LEFT JOIN Stock s ON s.medicine = m
        GROUP BY m
        HAVING COALESCE(SUM(s.quantity), 0) < m.minStockQuantity
    """)
    List<Medicine> findLowStockMedicines();
    
    /**
     * 재고가 없는 의약품 조회
     */
    @Query("""
        SELECT m FROM Medicine m
        WHERE NOT EXISTS (
            SELECT 1 FROM Stock s 
            WHERE s.medicine = m AND s.quantity > 0
        )
    """)
    List<Medicine> findOutOfStockMedicines();
}
