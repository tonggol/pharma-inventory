package com.pharma.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 의약품 등록 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCreateRequest {

    @NotBlank(message = "의약품 코드는 필수입니다")
    @Size(max = 50, message = "의약품 코드는 50자를 초과할 수 없습니다")
    private String code;

    @NotBlank(message = "의약품명은 필수입니다")
    @Size(max = 100, message = "의약품명은 100자를 초과할 수 없습니다")
    private String name;

    @Size(max = 100, message = "영문명은 100자를 초과할 수 없습니다")
    private String nameEn;

    private String description;

    @NotBlank(message = "제조사는 필수입니다")
    @Size(max = 50, message = "제조사명은 50자를 초과할 수 없습니다")
    private String manufacturer;

    @NotBlank(message = "단위는 필수입니다")
    @Size(max = 20, message = "단위는 20자를 초과할 수 없습니다")
    private String unit;

    @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
    private String category;

    @Size(max = 100, message = "보관조건은 100자를 초과할 수 없습니다")
    private String storageCondition;

    @NotNull(message = "최소 재고 수량은 필수입니다")
    @Min(value = 0, message = "최소 재고 수량은 0 이상이어야 합니다")
    private Integer minStockQuantity;

    private Boolean isPrescriptionRequired = false;

    private Boolean isActive = true;
}