package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 의약품 재고 상태
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineStockStatus {
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private Integer currentStock;
    private Integer minStockQuantity;
    private Integer stockPercentage;  // 최소 재고 대비 현재 재고 비율
    private String status;  // CRITICAL, LOW, NORMAL, OVERSTOCK
}
