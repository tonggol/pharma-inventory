package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 재고 엔티티
 * 의약품별 재고 현황 관리 (로트번호별 관리)
 */
@Entity
@Table(name = "stocks", 
       indexes = {
           @Index(name = "idx_lot_number", columnList = "lot_number"),
           @Index(name = "idx_expiry_date", columnList = "expiry_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;  // 의약품
    
    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;  // 로트번호
    
    @Column(nullable = false)
    private Integer quantity;  // 현재 수량
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;  // 유효기간
    
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;  // 제조일자
    
    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;  // 입고일자
    
    @Column(name = "supplier_name", length = 100)
    private String supplierName;  // 공급업체명
    
    @Column(name = "purchase_price")
    private Double purchasePrice;  // 구매 단가
    
    // unitPrice 게터 메서드 (purchasePrice와 동일하게 처리)
    public Double getUnitPrice() {
        return purchasePrice != null ? purchasePrice : 0.0;
    }
    
    @Column(length = 255)
    private String location;  // 보관 위치
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockStatus status = StockStatus.AVAILABLE;  // 재고 상태
    
    @Column(columnDefinition = "TEXT")
    private String remarks;  // 비고
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고 상태 Enum
     */
    public enum StockStatus {
        AVAILABLE("사용가능"),
        RESERVED("예약됨"),
        EXPIRED("만료됨"),
        DAMAGED("손상됨"),
        QUARANTINE("격리중");
        
        private final String description;
        
        StockStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 유효기간 잔여일수 계산
     */
    public long getDaysUntilExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    /**
     * 유효기간 만료 여부 확인
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
    
    /**
     * 유효기간 임박 여부 확인 (30일 기준)
     */
    public boolean isExpiringSoon(int daysThreshold) {
        return getDaysUntilExpiry() <= daysThreshold && getDaysUntilExpiry() > 0;
    }
}
