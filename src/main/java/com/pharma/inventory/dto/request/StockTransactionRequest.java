package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.StockTransaction.TransactionReason;
import com.pharma.inventory.entity.StockTransaction.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 트랜잭션 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionRequest {

    @NotNull(message = "의약품 ID는 필수입니다")
    private Long medicineId;

    private Long stockId;  // 특정 로트번호 지정 시

    @NotNull(message = "거래 유형은 필수입니다")
    private TransactionType transactionType;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;

    private LocalDateTime transactionDate;

    @Size(max = 50, message = "참조번호는 50자를 초과할 수 없습니다")
    private String referenceNumber;

    @Size(max = 50, message = "부서명은 50자를 초과할 수 없습니다")
    private String department;

    @Size(max = 50, message = "요청자명은 50자를 초과할 수 없습니다")
    private String requesterName;

    @Size(max = 50, message = "승인자명은 50자를 초과할 수 없습니다")
    private String approverName;

    private TransactionReason reason;

    private String remarks;
}