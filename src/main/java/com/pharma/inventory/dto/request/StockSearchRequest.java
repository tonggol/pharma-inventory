package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.Stock.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 재고 검색 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSearchRequest {

    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private String lotNumber;
    private StockStatus status;
    private String location;
    private String supplierName;
    private LocalDate expiryDateFrom;
    private LocalDate expiryDateTo;
    private LocalDate receivedDateFrom;
    private LocalDate receivedDateTo;
    private Boolean isExpired;
    private Boolean isExpiringSoon;  // 30일 이내 만료
    private Integer expiryDaysThreshold;  // 만료 임박 기준일 (기본 30일)
}