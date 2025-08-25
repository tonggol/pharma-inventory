package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대시보드 응답 DTO
 */
@Data
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private Long totalMedicines;
        private Long activeMedicines;
        private Long totalStockItems;
        private Double totalStockValue;
        private Long todayInboundCount;
        private Long todayOutboundCount;
        private Long expiredCount;
        private Long expiringSoonCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAlerts {
        private Long criticalStockCount;  // 재고 매우 부족
        private Long lowStockCount;       // 재고 부족
        private Long expiredCount;        // 만료된 재고
        private Long expiringSoonCount;   // 30일 이내 만료
        private Long quarantineCount;     // 격리 중인 재고
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineStockStatus {
        private Long medicineId;
        private String medicineName;
        private String medicineCode;
        private Integer currentStock;
        private Integer minStockQuantity;
        private Integer stockPercentage;  // 최소 재고 대비 현재 재고 비율
        private String status;  // CRITICAL, LOW, NORMAL, OVERSTOCK
    }
}