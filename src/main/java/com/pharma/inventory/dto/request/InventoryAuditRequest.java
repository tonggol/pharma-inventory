package com.pharma.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 재고 실사 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditRequest {

    @NotNull(message = "실사 항목은 필수입니다")
    private List<AuditItem> auditItems;

    private String auditorName;
    private String remarks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditItem {

        @NotNull(message = "재고 ID는 필수입니다")
        private Long stockId;

        @NotNull(message = "실사 수량은 필수입니다")
        private Integer actualQuantity;

        private Integer systemQuantity;  // 시스템 상 수량

        private String discrepancyReason;  // 차이 사유

        private String remarks;
    }
}