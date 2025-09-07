package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 개요 통계
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private Long totalMedicines;
    private Long activeMedicines;
    private Long totalStockItems;
    private Double totalStockValue;
    private Long todayInboundCount;
    private Long todayOutboundCount;
    private Long expiredCount;
    private Long expiringSoonCount;
}
