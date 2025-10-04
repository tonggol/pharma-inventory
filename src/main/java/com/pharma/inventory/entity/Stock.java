package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 在庫エンティティ
 * ロット番号別の在庫情報を管理
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
    private LocalDate receivedDate;  // 入庫日
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockStatus status = StockStatus.AVAILABLE;  // 在庫ステータス
    
    @Column(length = 100)
    private String location; // 保管場所
    
    @Column(name = "supplier_name", length = 100)
    private String supplierName; // 仕入先
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice; // 仕入単価
    
    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice; // 販売単価
    
    @Column(columnDefinition = "TEXT")
    private String remarks; // 備考

    // === コンストラクタ ===
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
    
    // === ビジネスメソッド ===
    
    /**
     * 在庫を増加
     */
    public void increaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("増加量は正の数である必要があります");
        }
        this.quantity += amount;
    }

    /**
     * 在庫調整 (直接数量設定)
     */
    public void adjustQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("調整数量は0以上である必要があります");
        }
        this.quantity = newQuantity;
    }
    
    /**
     * 在庫を減少
     */
    public void decreaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("減少量は正の数である必要があります");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("在庫が不足しています。現在の在庫: " + this.quantity);
        }
        this.quantity -= amount;
    }
    
    /**
     * 在庫ステータス変更
     */
    public void updateStatus(StockStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("在庫ステータスは必須です");
        }
        
        // 期限切れの在庫は再度利用可能状態に変更不可
        if (this.status == StockStatus.EXPIRED && status.isUsable()) {
            throw new IllegalStateException("期限切れの在庫は利用可能状態に変更できません");
        }
        
        this.status = status;
    }
    
    /**
     * 場所を変更
     */
    public void updateLocation(String location) {
        this.location = location;
    }
    
    /**
     * 備考を更新
     */
    public void updateRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * 仕入先情報を設定
     */
    public void setSupplierInfo(String supplierName, BigDecimal purchasePrice) {
        this.supplierName = supplierName;
        this.purchasePrice = purchasePrice;
    }
    
    /**
     * 価格情報を設定
     */
    public void setPriceInfo(BigDecimal purchasePrice, BigDecimal sellingPrice) {
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("仕入価格は0以上である必要があります");
        }
        if (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("販売価格は0以上である必要があります");
        }
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }
    
    /**
     * 備考を追加
     */
    public void addRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * 期限切れかどうかを確認
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate) || status == StockStatus.EXPIRED;
    }

    /**
     * 期限間近かどうかを確認
     */
    public boolean isExpiringSoon(int daysBeforeExpiry) {
        return LocalDate.now().plusDays(daysBeforeExpiry).isAfter(expiryDate);
    }
    
    /**
     * 利用可能かどうかを確認
     */
    public boolean isAvailable() {
        return status == StockStatus.AVAILABLE && !isExpired() && quantity > 0;
    }
    
    /**
     * 在庫価値を計算
     */
    public BigDecimal calculateValue() {
        if (purchasePrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * 期限切れを自動チェックしてステータスを更新
     */
    public void checkAndUpdateExpiry() {
        if (isExpired() && status != StockStatus.EXPIRED && status != StockStatus.DISPOSED) {
            this.status = StockStatus.EXPIRED;
        }
    }

    // === バリデーション ===
    private void validateStock(Medicine medicine, String lotNumber,
                               Integer quantity, LocalDate expiryDate) {
        if (medicine == null) {
            throw new IllegalArgumentException("医薬品情報は必須です");
        }
        if (lotNumber == null || lotNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("ロット番号は必須です");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("数量は0以上である必要があります");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("有効期限は必須です");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("有効期限は現在日付以降である必要があります");
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
