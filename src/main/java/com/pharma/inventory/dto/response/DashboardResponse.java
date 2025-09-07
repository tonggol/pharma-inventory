package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대시보드 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // 개요 통계
    private DashboardSummary summary;

    // 재고 알림
    private StockAlerts alerts;

    // 최근 트랜잭션
    private List<StockTransactionResponse> recentTransactions;

    // 만료 예정 재고
    private List<StockResponse> expiringStocks;

    // 재고 부족 의약품
    private List<MedicineStockStatus> lowStockMedicines;
}
