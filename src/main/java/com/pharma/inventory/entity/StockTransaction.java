package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
           @Index(name = "idx_transaction_type", columnList = "transaction_type")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {
    
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
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 거래 유형 Enum
     */
    public enum TransactionType {
        INBOUND("입고"),
        OUTBOUND("출고"),
        ADJUSTMENT("조정"),
        RETURN("반품"),
        DISPOSAL("폐기"),
        TRANSFER("이동");
        
        private final String description;
        
        TransactionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 거래 사유 Enum
     */
    public enum TransactionReason {
        PURCHASE("구매"),
        SALES("판매"),
        PRESCRIPTION("처방"),
        INVENTORY_CHECK("재고실사"),
        EXPIRED("유효기간만료"),
        DAMAGED("파손"),
        LOST("분실"),
        SAMPLE("샘플"),
        DONATION("기부"),
        OTHER("기타");
        
        private final String description;
        
        TransactionReason(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}
