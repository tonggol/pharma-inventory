package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 재고 보고서 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReportResponse {

    private LocalDate reportDate;
    private ReportPeriod period;

    // 재고 현황
    private StockSummary stockSummary;

    // 입출고 통계
    private TransactionSummary transactionSummary;

    // 카테고리별 통계
    private Map<String, CategoryStats> categoryStatistics;

    // 공급업체별 통계
    private Map<String, SupplierStats> supplierStatistics;

    // Top 10 리스트
    private List<MedicineUsage> topUsedMedicines;
    private List<MedicineUsage> topValueMedicines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportPeriod {
        private LocalDate startDate;
        private LocalDate endDate;
        private String periodType;  // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSummary {
        private Long totalItems;
        private Long totalQuantity;
        private Double totalValue;
        private Long expiredItems;
        private Long expiringSoonItems;
        private Double averageStockLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummary {
        private Long totalInbound;
        private Long totalOutbound;
        private Long totalAdjustment;
        private Long totalDisposal;
        private Double inboundValue;
        private Double outboundValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        private String category;
        private Long itemCount;
        private Long totalQuantity;
        private Double totalValue;
        private Double percentageOfTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierStats {
        private String supplierName;
        private Long orderCount;
        private Long itemCount;
        private Double totalValue;
        private Double averageDeliveryTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineUsage {
        private Long medicineId;
        private String medicineName;
        private String medicineCode;
        private Long usageCount;
        private Long usageQuantity;
        private Double usageValue;
    }
}