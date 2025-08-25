package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.Stock.StockStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재고 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    @Min(value = 0, message = "수량은 0 이상이어야 합니다")
    private Integer quantity;

    @Size(max = 255, message = "보관위치는 255자를 초과할 수 없습니다")
    private String location;

    private StockStatus status;

    private String remarks;
}