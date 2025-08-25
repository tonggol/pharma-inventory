package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.StockTransaction.TransactionReason;
import com.pharma.inventory.entity.StockTransaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 트랜잭션 검색 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {

    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private Long stockId;
    private String lotNumber;
    private TransactionType transactionType;
    private TransactionReason reason;
    private String referenceNumber;
    private String department;
    private String requesterName;
    private String approverName;
    private LocalDateTime transactionDateFrom;
    private LocalDateTime transactionDateTo;
    private String createdByUsername;
}