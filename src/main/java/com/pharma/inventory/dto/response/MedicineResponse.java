package com.pharma.inventory.dto.response;

import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.MedicineCategory;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 의약품 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String code;
    private String name;
    private String nameEn;
    private String description;
    private String manufacturer;
    private String unit;
    private String category;
    private String storageCondition;
    private Integer minStockQuantity;
    private Boolean isPrescriptionRequired;
    private Boolean isActive;
    private Integer currentStock;  // 현재 총 재고량
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static MedicineResponse from(Medicine medicine) {
        if (medicine == null) {
            return null;
        }
        return MedicineResponse.builder()
                .id(medicine.getId())
                .code(medicine.getCode())
                .name(medicine.getName())
                .nameEn(medicine.getNameEn())
                .description(medicine.getDescription())
                .manufacturer(medicine.getManufacturer())
                .unit(medicine.getUnit())
                .category(medicine.getCategory() != null ? medicine.getCategory().getDescription() : null)
                .storageCondition(medicine.getStorageCondition())
                .minStockQuantity(medicine.getMinStockQuantity())
                .isPrescriptionRequired(medicine.getIsPrescriptionRequired())
                .isActive(medicine.getIsActive())
                .createdAt(medicine.getCreatedAt())
                .updatedAt(medicine.getUpdatedAt())
                .build();
    }
}