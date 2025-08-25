package com.pharma.inventory.dto.request;

import com.pharma.inventory.entity.Stock.StockStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 재고 일괄 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchStockUpdateRequest {

    @NotNull(message = "재고 ID 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 재고를 선택해야 합니다")
    private List<Long> stockIds;

    private StockStatus status;
    private String location;
    private String remarks;

    // 일괄 작업 유형
    private BatchOperation operation;

    public enum BatchOperation {
        UPDATE_STATUS("상태 변경"),
        UPDATE_LOCATION("위치 변경"),
        MARK_EXPIRED("만료 처리"),
        QUARANTINE("격리 처리");

        private final String description;

        BatchOperation(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}