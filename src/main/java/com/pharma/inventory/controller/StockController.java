package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.common.PageRequest;
import com.pharma.inventory.dto.common.PageResponse;
import com.pharma.inventory.dto.request.BatchStockUpdateRequest;
import com.pharma.inventory.dto.request.InventoryAuditRequest;
import com.pharma.inventory.dto.request.StockCreateRequest;
import com.pharma.inventory.dto.request.StockSearchRequest;
import com.pharma.inventory.dto.request.StockUpdateRequest;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 재고 관리 REST API Controller
 * 재고의 입고, 조회, 수정, 실사 등 재고 관련 기능 제공
 */
@Tag(name = "Stock", description = "재고 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 재고 목록 조회 (페이징)
     */
    @Operation(summary = "재고 목록 조회", description = "페이징된 재고 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockResponse>>> getStocks(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction) {

        log.info("재고 목록 조회 요청 - page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(org.springframework.data.domain.Sort.Direction.valueOf(direction))
                .build();

        Page<StockResponse> stocks = stockService.getStocks(pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(stocks)));
    }

    /**
     * 재고 검색
     */
    @Operation(summary = "재고 검색", description = "조건에 따라 재고를 검색합니다")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<StockResponse>>> searchStocks(
            @Valid @RequestBody StockSearchRequest searchRequest,
            @Parameter(description = "페이지 정보") @ModelAttribute PageRequest pageRequest) {

        log.info("재고 검색 요청 - 검색조건: {}", searchRequest);

        Page<StockResponse> results = stockService.searchStocks(
                searchRequest, pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(results),
                String.format("%d개의 재고가 검색되었습니다", results.getTotalElements())
        ));
    }

    /**
     * 재고 상세 조회
     */
    @Operation(summary = "재고 상세 조회", description = "ID로 재고 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockResponse>> getStock(
            @Parameter(description = "재고 ID", required = true) @PathVariable Long id) {

        log.info("재고 상세 조회 - ID: {}", id);

        StockResponse stock = stockService.getStock(id);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    /**
     * 재고 입고 (신규 재고 등록)
     */
    @Operation(summary = "재고 입고", description = "새로운 재고를 입고 처리합니다")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<StockResponse>> createStock(
            @Valid @RequestBody StockCreateRequest request) {

        log.info("재고 입고 요청 - 의약품ID: {}, 로트번호: {}, 수량: {}",
                request.getMedicineId(), request.getLotNumber(), request.getQuantity());

        StockResponse created = stockService.createStock(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "재고가 성공적으로 입고되었습니다"));
    }

    /**
     * 재고 정보 수정
     */
    @Operation(summary = "재고 수정", description = "재고 정보를 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockResponse>> updateStock(
            @Parameter(description = "재고 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        log.info("재고 수정 요청 - ID: {}", id);

        StockResponse updated = stockService.updateStock(id, request);

        return ResponseEntity.ok(ApiResponse.success(updated, "재고 정보가 수정되었습니다"));
    }

    /**
     * 재고 일괄 수정
     */
    @Operation(summary = "재고 일괄 수정", description = "여러 재고를 일괄 수정합니다")
    @PutMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BatchUpdateResult>> batchUpdateStocks(
            @Valid @RequestBody BatchStockUpdateRequest request) {

        log.info("재고 일괄 수정 요청 - 대상 개수: {}, 작업: {}",
                request.getStockIds().size(), request.getOperation());

        BatchUpdateResult result = stockService.batchUpdateStocks(request);

        return ResponseEntity.ok(ApiResponse.success(result,
                String.format("%d개 재고가 수정되었습니다", result.getUpdatedCount())));
    }

    /**
     * 로트번호로 재고 조회
     */
    @Operation(summary = "로트번호로 재고 조회", description = "로트번호로 재고를 조회합니다")
    @GetMapping("/lot/{lotNumber}")
    public ResponseEntity<ApiResponse<StockResponse>> getStockByLotNumber(
            @Parameter(description = "로트번호", required = true) @PathVariable String lotNumber) {

        log.info("로트번호로 재고 조회 - 로트번호: {}", lotNumber);

        StockResponse stock = stockService.getStockByLotNumber(lotNumber);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    /**
     * 만료 예정 재고 조회
     */
    @Operation(summary = "만료 예정 재고 조회", description = "지정 기간 내 만료 예정인 재고를 조회합니다")
    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getExpiringStocks(
            @Parameter(description = "만료 기준 일수 (기본 30일)")
            @RequestParam(defaultValue = "30") int days) {

        log.info("만료 예정 재고 조회 - 기준 일수: {}", days);

        List<StockResponse> stocks = stockService.getExpiringStocks(days);

        return ResponseEntity.ok(ApiResponse.success(stocks,
                String.format("%d개의 재고가 %d일 내에 만료 예정입니다", stocks.size(), days)));
    }

    /**
     * 만료된 재고 조회
     */
    @Operation(summary = "만료된 재고 조회", description = "이미 만료된 재고를 조회합니다")
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getExpiredStocks() {

        log.info("만료된 재고 조회 요청");

        List<StockResponse> stocks = stockService.getExpiredStocks();

        return ResponseEntity.ok(ApiResponse.success(stocks,
                String.format("%d개의 만료된 재고가 있습니다", stocks.size())));
    }

    /**
     * 재고 상태별 통계
     */
    @Operation(summary = "재고 상태별 통계", description = "재고 상태별 수량 통계를 조회합니다")
    @GetMapping("/statistics/status")
    public ResponseEntity<ApiResponse<Map<Stock.StockStatus, StockStatusStatistics>>> getStockStatusStatistics() {

        log.info("재고 상태별 통계 조회 요청");

        Map<Stock.StockStatus, StockStatusStatistics> statistics = stockService.getStockStatusStatistics();

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 기간별 재고 변동 현황
     */
    @Operation(summary = "기간별 재고 변동 현황", description = "지정 기간의 재고 변동을 조회합니다")
    @GetMapping("/statistics/changes")
    public ResponseEntity<ApiResponse<StockChangeStatistics>> getStockChanges(
            @Parameter(description = "시작일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("재고 변동 현황 조회 - 기간: {} ~ {}", startDate, endDate);

        StockChangeStatistics statistics = stockService.getStockChanges(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 재고 실사
     */
    @Operation(summary = "재고 실사", description = "재고 실사를 수행하고 차이를 조정합니다")
    @PostMapping("/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<InventoryAuditResult>> performInventoryAudit(
            @Valid @RequestBody InventoryAuditRequest request) {

        log.info("재고 실사 요청 - 대상 재고: {}개", request.getAuditItems().size());

        InventoryAuditResult result = stockService.performInventoryAudit(request);

        return ResponseEntity.ok(ApiResponse.success(result,
                "재고 실사가 완료되었습니다"));
    }

    /**
     * 재고 위치별 조회
     */
    @Operation(summary = "재고 위치별 조회", description = "특정 위치의 재고를 조회합니다")
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStocksByLocation(
            @Parameter(description = "보관 위치", required = true) @PathVariable String location) {

        log.info("위치별 재고 조회 - 위치: {}", location);

        List<StockResponse> stocks = stockService.getStocksByLocation(location);

        return ResponseEntity.ok(ApiResponse.success(stocks,
                String.format("%s 위치에 %d개의 재고가 있습니다", location, stocks.size())));
    }

    /**
     * 재고 가치 평가
     */
    @Operation(summary = "재고 가치 평가", description = "전체 재고의 금액적 가치를 평가합니다")
    @GetMapping("/valuation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockValuation>> getStockValuation() {

        log.info("재고 가치 평가 요청");

        StockValuation valuation = stockService.calculateStockValuation();

        return ResponseEntity.ok(ApiResponse.success(valuation));
    }

    /**
     * 일괄 수정 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchUpdateResult {
        private Integer requestedCount;
        private Integer updatedCount;
        private Integer failedCount;
        private List<String> errors;
    }

    /**
     * 재고 상태별 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockStatusStatistics {
        private Stock.StockStatus status;
        private String statusDescription;
        private Long count;
        private Long totalQuantity;
        private Double percentage;
    }

    /**
     * 재고 변동 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockChangeStatistics {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalInbound;
        private Long totalOutbound;
        private Long totalAdjustment;
        private Long netChange;
        private Map<String, Long> dailyChanges;
    }

    /**
     * 재고 실사 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InventoryAuditResult {
        private Integer auditedCount;
        private Integer discrepancyCount;
        private Integer adjustedCount;
        private List<DiscrepancyDetail> discrepancies;
        private LocalDate auditDate;
        private String auditor;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class DiscrepancyDetail {
            private Long stockId;
            private String lotNumber;
            private String medicineName;
            private Integer systemQuantity;
            private Integer actualQuantity;
            private Integer difference;
            private String reason;
        }
    }

    /**
     * 재고 가치 평가 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockValuation {
        private Double totalValue;
        private Long totalItems;
        private Long totalQuantity;
        private Map<String, CategoryValuation> categoryValuations;
        private Map<String, Double> monthlyValueTrend;
        private LocalDate valuationDate;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class CategoryValuation {
            private String category;
            private Double value;
            private Long quantity;
            private Double percentageOfTotal;
        }
    }
}