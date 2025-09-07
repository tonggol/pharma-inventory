package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 재고 엔티티
 * 로트번호별 재고 정보 관리
 */
@Entity
@Table(name = "stocks", indexes = {
        @Index(name = "idx_lot_number", columnList = "lot_number", unique = true),
        @Index(name = "idx_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_medicine_id", columnList = "medicine_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "lot_number", nullable = false, unique = true, length = 50)
    private String lotNumber;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Column(name = "received_date")
    private LocalDate receivedDate;  // 입고일
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockStatus status = StockStatus.AVAILABLE;  // 재고 상태
    
    @Column(length = 100)
    private String location; // 보관 위치
    
    @Column(name = "supplier_name", length = 100)
    private String supplierName; // 공급업체
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice; // 구매 단가
    
    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice; // 판매 단가
    
    @Column(columnDefinition = "TEXT")
    private String remarks; // 비고

    // === 생성자 ===
    public Stock(Medicine medicine, String lotNumber, Integer quantity,
                 LocalDate manufactureDate, LocalDate expiryDate) {
        validateStock(medicine, lotNumber, quantity, expiryDate);
        this.medicine = medicine;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.receivedDate = LocalDate.now();
        this.status = StockStatus.AVAILABLE;
    }

    public Long getId() {
        return this.id;
    }
    
    // === 비즈니스 메소드 ===
    
    /**
     * 재고 증가
     */
    public void increaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가량은 양수여야 합니다");
        }
        this.quantity += amount;
    }

    /**
     * 재고 조정 (직접 수량 설정)
     */
    public void adjustQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("조정 수량은 0 이상이어야 합니다");
        }
        this.quantity = newQuantity;
    }
    
    /**
     * 재고 감소
     */
    public void decreaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소량은 양수여야 합니다");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.quantity);
        }
        this.quantity -= amount;
    }
    
    /**
     * 재고 상태 변경
     */
    public void updateStatus(StockStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("재고 상태는 필수입니다");
        }
        
        // 만료된 재고는 다시 사용 가능 상태로 변경 불가
        if (this.status == StockStatus.EXPIRED && status.isUsable()) {
            throw new IllegalStateException("만료된 재고는 사용 가능 상태로 변경할 수 없습니다");
        }
        
        this.status = status;
    }
    
    /**
     * 위치 변경
     */
    public void updateLocation(String location) {
        this.location = location;
    }
    
    /**
     * 비고 업데이트
     */
    public void updateRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * 공급업체 정보 설정
     */
    public void setSupplierInfo(String supplierName, BigDecimal purchasePrice) {
        this.supplierName = supplierName;
        this.purchasePrice = purchasePrice;
    }
    
    /**
     * 가격 정보 설정
     */
    public void setPriceInfo(BigDecimal purchasePrice, BigDecimal sellingPrice) {
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("구매 가격은 0 이상이어야 합니다");
        }
        if (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("판매 가격은 0 이상이어야 합니다");
        }
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }
    
    /**
     * 비고 추가
     */
    public void addRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate) || status == StockStatus.EXPIRED;
    }

    /**
     * 만료 임박 여부 확인
     */
    public boolean isExpiringSoon(int daysBeforeExpiry) {
        return LocalDate.now().plusDays(daysBeforeExpiry).isAfter(expiryDate);
    }
    
    /**
     * 사용 가능 여부 확인
     */
    public boolean isAvailable() {
        return status == StockStatus.AVAILABLE && !isExpired() && quantity > 0;
    }
    
    /**
     * 재고 가치 계산
     */
    public BigDecimal calculateValue() {
        if (purchasePrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * 만료 자동 체크 및 상태 업데이트
     */
    public void checkAndUpdateExpiry() {
        if (isExpired() && status != StockStatus.EXPIRED && status != StockStatus.DISPOSED) {
            this.status = StockStatus.EXPIRED;
        }
    }

    // === Validation ===
    private void validateStock(Medicine medicine, String lotNumber,
                               Integer quantity, LocalDate expiryDate) {
        if (medicine == null) {
            throw new IllegalArgumentException("의약품 정보는 필수입니다");
        }
        if (lotNumber == null || lotNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("로트 번호는 필수입니다");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("유효기간은 필수입니다");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("유효기간은 현재 날짜 이후여야 합니다");
        }
    }

    // === equals & hashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;
        Stock stock = (Stock) o;
        return id != null && id.equals(stock.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // === toString ===
    @Override
    public String toString() {
        return String.format("Stock[id=%d, lotNumber='%s', quantity=%d, status=%s]",
                id, lotNumber, quantity, status);
    }
}
