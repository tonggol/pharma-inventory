/**
package com.pharma.inventory.repository;

import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.QMedicine;
import com.pharma.inventory.entity.QStock;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 의약품 Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 구현

@Repository
@RequiredArgsConstructor
public class MedicineRepositoryImpl implements MedicineRepositoryCustom {
    
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    
    private final QMedicine medicine = QMedicine.medicine;
    private final QStock stock = QStock.stock;
    
    @Override
    public Page<Medicine> searchMedicines(MedicineSearchRequest searchRequest, Pageable pageable) {
        // 동적 쿼리 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        
        // 의약품명 검색
        if (StringUtils.hasText(searchRequest.getName())) {
            builder.and(medicine.name.containsIgnoreCase(searchRequest.getName()));
        }
        
        // 의약품 코드 검색
        if (StringUtils.hasText(searchRequest.getCode())) {
            builder.and(medicine.medicineCode.eq(searchRequest.getCode()));
        }
        
        // 제조사 검색
        if (StringUtils.hasText(searchRequest.getManufacturer())) {
            builder.and(medicine.manufacturer.containsIgnoreCase(searchRequest.getManufacturer()));
        }
        
        // 카테고리 검색
        if (searchRequest.getCategory() != null) {
            builder.and(medicine.category.eq(searchRequest.getCategory()));
        }
        
        // 활성 상태
        if (searchRequest.getIsActive() != null) {
            builder.and(medicine.isActive.eq(searchRequest.getIsActive()));
        }
        
        // 재고 부족 여부
        if (searchRequest.getLowStockOnly() != null && searchRequest.getLowStockOnly()) {
            // 서브쿼리로 현재 재고 확인
            builder.and(medicine.id.in(
                queryFactory.select(stock.medicine.id)
                    .from(stock)
                    .groupBy(stock.medicine.id)
                    .having(stock.quantity.sum().lt(stock.medicine.minStockLevel))
            ));
        }
        
        // 전체 카운트 쿼리
        Long total = queryFactory
            .select(medicine.count())
            .from(medicine)
            .where(builder)
            .fetchOne();
        
        // 데이터 조회 쿼리
        List<Medicine> content = queryFactory
            .selectFrom(medicine)
            .leftJoin(medicine.stocks, stock).fetchJoin()
            .where(builder)
            .distinct()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(medicine.id.desc())
            .fetch();
        
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
    
    @Override
    public List<Medicine> findLowStockMedicines() {
        return queryFactory
            .selectFrom(medicine)
            .leftJoin(medicine.stocks, stock)
            .groupBy(medicine.id)
            .having(
                stock.quantity.sum().lt(medicine.minStockLevel)
                .or(stock.quantity.sum().isNull())
            )
            .fetch();
    }
    
    @Override
    public List<MedicineCategoryStats> getMedicineStatsByCategory() {
        return queryFactory
            .select(Projections.constructor(
                MedicineCategoryStats.class,
                medicine.category.stringValue(),
                medicine.count(),
                medicine.isActive.when(true).then(1L).otherwise(0L).sum(),
                medicine.id.when(
                    medicine.stocks.any().quantity.lt(medicine.minStockLevel)
                ).then(1L).otherwise(0L).sum()
            ))
            .from(medicine)
            .leftJoin(medicine.stocks, stock)
            .groupBy(medicine.category)
            .fetch();
    }
    
    @Override
    public List<Medicine> findMedicinesWithComplexCondition(String manufacturer, 
                                                            Integer minStock, 
                                                            Boolean isActive) {
        return queryFactory
            .selectFrom(medicine)
            .where(
                manufacturerEq(manufacturer),
                minStockGoe(minStock),
                isActiveEq(isActive)
            )
            .fetch();
    }
    
    // === 동적 쿼리 조건 메소드 ===
    
    private BooleanExpression manufacturerEq(String manufacturer) {
        return StringUtils.hasText(manufacturer) ? medicine.manufacturer.eq(manufacturer) : null;
    }
    
    private BooleanExpression minStockGoe(Integer minStock) {
        return minStock != null ? medicine.minStockLevel.goe(minStock) : null;
    }
    
    private BooleanExpression isActiveEq(Boolean isActive) {
        return isActive != null ? medicine.isActive.eq(isActive) : null;
    }
}
*/