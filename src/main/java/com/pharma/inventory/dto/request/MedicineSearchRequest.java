package com.pharma.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 의약품 검색 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineSearchRequest {

    private String keyword;  // 통합 검색 (코드, 이름, 제조사)
    private String code;
    private String name;
    private String manufacturer;
    private String category;
    private Boolean isPrescriptionRequired;
    private Boolean isActive;
    private Boolean isBelowMinStock;  // 최소 재고 미만 여부
}