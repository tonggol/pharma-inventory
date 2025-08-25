package com.pharma.inventory.dto.response;

import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.entity.StockTransaction.TransactionReason;
import com.pharma.inventory.entity.StockTransaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 트랜잭션 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private Long stockId;
    private String lotNumber;
    private TransactionType transactionType;
    private String transactionTypeDescription;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private LocalDateTime transactionDate;
    private String referenceNumber;
    private String department;
    private String requesterName;
    private String approverName;
    private TransactionReason reason;
    private String reasonDescription;
    private String remarks;
    private String createdByUsername;
    private LocalDateTime createdAt;

    /**
     * Entity를 DTO로 변환
     */
    public static StockTransactionResponse from(StockTransaction transaction) {
        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .medicineId(transaction.getMedicine().getId())
                .medicineName(transaction.getMedicine().getName())
                .medicineCode(transaction.getMedicine().getCode())
                .stockId(transaction.getStock() != null ? transaction.getStock().getId() : null)
                .lotNumber(transaction.getStock() != null ? transaction.getStock().getLotNumber() : null)
                .transactionType(transaction.getTransactionType())
                .transactionTypeDescription(transaction.getTransactionType().getDescription())
                .quantity(transaction.getQuantity())
                .beforeQuantity(transaction.getBeforeQuantity())
                .afterQuantity(transaction.getAfterQuantity())
                .transactionDate(transaction.getTransactionDate())
                .referenceNumber(transaction.getReferenceNumber())
                .department(transaction.getDepartment())
                .requesterName(transaction.getRequesterName())
                .approverName(transaction.getApproverName())
                .reason(transaction.getReason())
                .reasonDescription(transaction.getReason() != null ? transaction.getReason().getDescription() : null)
                .remarks(transaction.getRemarks())
                .createdByUsername(transaction.getCreatedBy() != null ? transaction.getCreatedBy().getUsername() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}