package com.pharma.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 데이터 내보내기 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotNull(message = "내보내기 유형은 필수입니다")
    private ExportType exportType;

    @NotNull(message = "파일 형식은 필수입니다")
    private FileFormat fileFormat;

    private LocalDate startDate;
    private LocalDate endDate;

    // 필터 조건
    private Long medicineId;
    private String category;
    private String supplierName;
    private Boolean includeExpired;

    public enum ExportType {
        MEDICINE_LIST("의약품 목록"),
        STOCK_REPORT("재고 현황"),
        TRANSACTION_HISTORY("입출고 내역"),
        EXPIRY_REPORT("유효기간 보고서"),
        INVENTORY_VALUATION("재고 평가");

        private final String description;

        ExportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum FileFormat {
        EXCEL("Excel"),
        CSV("CSV"),
        PDF("PDF");

        private final String description;

        FileFormat(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}