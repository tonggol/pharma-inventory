package com.pharma.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 의약품 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineUpdateRequest {

    @Size(max = 100, message = "의약품명은 100자를 초과할 수 없습니다")
    private String name;

    @Size(max = 100, message = "영문명은 100자를 초과할 수 없습니다")
    private String nameEn;

    private String description;

    @Size(max = 50, message = "제조사명은 50자를 초과할 수 없습니다")
    private String manufacturer;

    @Size(max = 20, message = "단위는 20자를 초과할 수 없습니다")
    private String unit;

    @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
    private String category;

    @Size(max = 100, message = "보관조건은 100자를 초과할 수 없습니다")
    private String storageCondition;

    @Min(value = 0, message = "최소 재고 수량은 0 이상이어야 합니다")
    private Integer minStockQuantity;

    private Boolean isPrescriptionRequired;

    private Boolean isActive;
}