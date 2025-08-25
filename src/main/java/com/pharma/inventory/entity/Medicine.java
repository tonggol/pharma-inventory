package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 의약품 엔티티
 * 의약품의 기본 정보를 관리
 */
@Entity
@Table(name = "medicines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;  // 의약품 코드
    
    @Column(nullable = false, length = 100)
    private String name;  // 의약품명
    
    @Column(name = "name_en", length = 100)
    private String nameEn;  // 영문명
    
    @Column(columnDefinition = "TEXT")
    private String description;  // 상세 설명
    
    @Column(nullable = false, length = 50)
    private String manufacturer;  // 제조사
    
    @Column(nullable = false, length = 20)
    private String unit;  // 단위 (정, 앰플, 바이알 등)
    
    @Column(name = "category", length = 50)
    private String category;  // 의약품 분류 (항생제, 진통제 등)
    
    @Column(name = "storage_condition", length = 100)
    private String storageCondition;  // 보관 조건 (냉장, 실온 등)
    
    @Column(name = "min_stock_quantity", nullable = false)
    private Integer minStockQuantity = 10;  // 최소 재고 수량
    
    @Column(name = "reorder_level")
    private Integer reorderLevel = 20;  // 재주문 수준
    
    @Column(name = "is_prescription_required")
    private Boolean isPrescriptionRequired = false;  // 처방전 필요 여부
    
    @Column(name = "is_active")
    private Boolean isActive = true;  // 활성 상태
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    // 헬퍼 메서드
    public Integer getMinimumStock() {
        return this.minStockQuantity;
    }
    
    public Integer getReorderLevel() {
        return this.reorderLevel != null ? this.reorderLevel : this.minStockQuantity * 2;
    }
}
