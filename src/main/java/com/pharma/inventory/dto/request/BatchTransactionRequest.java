package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.StockTransaction.TransactionReason;
import com.pharma.inventory.entity.StockTransaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 트랜잭션 일괄 처리 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransactionRequest {

    @NotNull(message = "트랜잭션 항목은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 트랜잭션이 필요합니다")
    private List<TransactionItem> transactions;

    private LocalDateTime transactionDate;
    private String department;
    private String requesterName;
    private String approverName;
    private String remarks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionItem {

        @NotNull(message = "의약품 ID는 필수입니다")
        private Long medicineId;

        private Long stockId;

        @NotNull(message = "거래 유형은 필수입니다")
        private TransactionType transactionType;

        @NotNull(message = "수량은 필수입니다")
        private Integer quantity;

        private TransactionReason reason;
        private String referenceNumber;
        private String remarks;
    }
}