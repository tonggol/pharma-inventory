package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 입출고 트랜잭션 엔티티
 * 모든 재고 변동 이력을 기록
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
    private Medicine medicine;  // 의약품
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;  // 재고 (로트번호별)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;  // 거래 유형
    
    @Column(nullable = false)
    private Integer quantity;  // 수량
    
    @Column(name = "before_quantity")
    private Integer beforeQuantity;  // 변경 전 수량
    
    @Column(name = "after_quantity")
    private Integer afterQuantity;  // 변경 후 수량
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;  // 거래 일시
    
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;  // 참조 번호 (주문번호, 처방전번호 등)
    
    @Column(name = "department", length = 50)
    private String department;  // 부서/진료과
    
    @Column(name = "requester_name", length = 50)
    private String requesterName;  // 요청자
    
    @Column(name = "approver_name", length = 50)
    private String approverName;  // 승인자
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30)
    private TransactionReason reason;  // 입출고 사유
    
    @Column(columnDefinition = "TEXT")
    private String remarks;  // 비고
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;  // 작성자
    
    // === 생성자 ===
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
    
    // === 전체 필드 생성자 (Service용) ===
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
    
    // === 비즈니스 메소드 ===
    
    /**
     * 재고 수량 기록
     */
    public void recordQuantityChange(Integer before, Integer after) {
        this.beforeQuantity = before;
        this.afterQuantity = after;
    }
    
    /**
     * 참조 정보 설정
     */
    public void setReferenceInfo(String referenceNumber, String department) {
        this.referenceNumber = referenceNumber;
        this.department = department;
    }
    
    /**
     * 요청자/승인자 설정
     */
    public void setRequesterInfo(String requesterName, String approverName) {
        this.requesterName = requesterName;
        this.approverName = approverName;
    }
    
    /**
     * 비고 설정
     */
    public void addRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    // === Validation ===
    private void validateTransaction(Medicine medicine, TransactionType type, 
                                    Integer quantity, User createdBy) {
        if (medicine == null) {
            throw new IllegalArgumentException("의약품 정보는 필수입니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("작성자 정보는 필수입니다");
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
