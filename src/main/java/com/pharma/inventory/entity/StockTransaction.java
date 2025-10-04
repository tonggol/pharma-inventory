package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在庫取引エンティティ
 * すべての在庫変動履歴を記録
 */
@Entity
@Table(name = "stock_transactions",
       indexes = {
           @Index(name = "idx_transaction_date", columnList = "transaction_date"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type"),
           @Index(name = "idx_stock_transaction_id", columnList = "medicine_id"),
           @Index(name = "idx_stock_id", columnList = "stock_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTransaction extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;  // 医薬品
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;  // 在庫 (ロット番号別)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;  // 取引タイプ
    
    @Column(nullable = false)
    private Integer quantity;  // 数量
    
    @Column(name = "before_quantity")
    private Integer beforeQuantity;  // 変更前の数量
    
    @Column(name = "after_quantity")
    private Integer afterQuantity;  // 変更後の数量
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;  // 取引日時
    
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;  // 参照番号 (注文番号、処方箋番号など)
    
    @Column(name = "department", length = 50)
    private String department;  // 部署/診療科
    
    @Column(name = "requester_name", length = 50)
    private String requesterName;  // 依頼者
    
    @Column(name = "approver_name", length = 50)
    private String approverName;  // 承認者
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30)
    private TransactionReason reason;  // 入出庫事由
    
    @Column(columnDefinition = "TEXT")
    private String remarks;  // 備考
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;  // 作成者
    
    // === コンストラクタ ===
    public StockTransaction(Medicine medicine, Stock stock, TransactionType type, 
                          Integer quantity, TransactionReason reason, User createdBy) {
        validateTransaction(medicine, type, quantity, createdBy);
        this.medicine = medicine;
        this.stock = stock;
        this.transactionType = type;
        this.quantity = quantity;
        this.reason = reason;
        this.createdBy = createdBy;
        this.transactionDate = LocalDateTime.now();
    }
    
    // === 全フィールドコンストラクタ (Service用) ===
    public StockTransaction(Medicine medicine, Stock stock, TransactionType transactionType,
                          Integer quantity, Integer beforeQuantity, Integer afterQuantity,
                          LocalDateTime transactionDate, TransactionReason reason) {
        this.medicine = medicine;
        this.stock = stock;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
        this.reason = reason;
    }

    public Long getId() {
        return this.id;
    }
    
    // === ビジネスメソッド ===
    
    /**
     * 在庫数量を記録
     */
    public void recordQuantityChange(Integer before, Integer after) {
        this.beforeQuantity = before;
        this.afterQuantity = after;
    }
    
    /**
     * 参照情報を設定
     */
    public void setReferenceInfo(String referenceNumber, String department) {
        this.referenceNumber = referenceNumber;
        this.department = department;
    }
    
    /**
     * 依頼者/承認者を設定
     */
    public void setRequesterInfo(String requesterName, String approverName) {
        this.requesterName = requesterName;
        this.approverName = approverName;
    }
    
    /**
     * 備考を設定
     */
    public void addRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 作成者を設定
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    // === バリデーション ===
    private void validateTransaction(Medicine medicine, TransactionType type, 
                                    Integer quantity, User createdBy) {
        if (medicine == null) {
            throw new IllegalArgumentException("医薬品情報は必須です");
        }
        if (type == null) {
            throw new IllegalArgumentException("取引タイプは必須です");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量は0より大きい必要があります");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("作成者情報は必須です");
        }
    }
    
    // === equals & hashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockTransaction)) return false;
        StockTransaction that = (StockTransaction) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    // === toString ===
    @Override
    public String toString() {
        return String.format("StockTransaction[id=%d, type=%s, quantity=%d, date=%s]",
            id, transactionType, quantity, transactionDate);
    }
}
