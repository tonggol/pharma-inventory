package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.common.PageRequest;
import com.pharma.inventory.dto.common.PageResponse;
import com.pharma.inventory.dto.request.BatchTransactionRequest;
import com.pharma.inventory.dto.request.StockTransactionRequest;
import com.pharma.inventory.dto.request.TransactionSearchRequest;
import com.pharma.inventory.dto.response.StockTransactionResponse;
import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.service.StockTransactionService;
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
 * 재고 입출고 트랜잭션 REST API Controller
 * 모든 재고 변동 이력을 관리하고 추적
 */
@Tag(name = "StockTransaction", description = "재고 트랜잭션 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    /**
     * 트랜잭션 목록 조회 (페이징)
     */
    @Operation(summary = "트랜잭션 목록 조회", description = "페이징된 트랜잭션 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionResponse>>> getTransactions(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction) {

        log.info("트랜잭션 목록 조회 - page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(org.springframework.data.domain.Sort.Direction.valueOf(direction))
                .build();

        Page<StockTransactionResponse> transactions = stockTransactionService
                .getTransactions(pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(transactions)));
    }

    /**
     * 트랜잭션 상세 조회
     */
    @Operation(summary = "트랜잭션 상세 조회", description = "ID로 트랜잭션 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> getTransaction(
            @Parameter(description = "트랜잭션 ID", required = true) @PathVariable Long id) {

        log.info("트랜잭션 상세 조회 - ID: {}", id);

        StockTransactionResponse transaction = stockTransactionService.getTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    /**
     * 트랜잭션 검색
     */
    @Operation(summary = "트랜잭션 검색", description = "조건에 따라 트랜잭션을 검색합니다")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionResponse>>> searchTransactions(
            @Valid @RequestBody TransactionSearchRequest searchRequest,
            @Parameter(description = "페이지 정보") @ModelAttribute PageRequest pageRequest) {

        log.info("트랜잭션 검색 - 조건: {}", searchRequest);

        Page<StockTransactionResponse> results = stockTransactionService
                .searchTransactions(searchRequest, pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(results),
                String.format("%d개의 트랜잭션이 검색되었습니다", results.getTotalElements())
        ));
    }

    /**
     * 입고 처리
     */
    @Operation(summary = "재고 입고", description = "새로운 재고를 입고 처리합니다")
    @PostMapping("/inbound")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processInbound(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("입고 처리 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        // 트랜잭션 타입을 INBOUND로 설정
        request.setTransactionType(StockTransaction.TransactionType.INBOUND);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "입고 처리가 완료되었습니다"));
    }

    /**
     * 출고 처리
     */
    @Operation(summary = "재고 출고", description = "재고를 출고 처리합니다")
    @PostMapping("/outbound")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processOutbound(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("출고 처리 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        // 트랜잭션 타입을 OUTBOUND로 설정
        request.setTransactionType(StockTransaction.TransactionType.OUTBOUND);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "출고 처리가 완료되었습니다"));
    }

    /**
     * 재고 조정
     */
    @Operation(summary = "재고 조정", description = "재고 수량을 조정합니다")
    @PostMapping("/adjustment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processAdjustment(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("재고 조정 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        request.setTransactionType(StockTransaction.TransactionType.ADJUSTMENT);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "재고 조정이 완료되었습니다"));
    }

    /**
     * 반품 처리
     */
    @Operation(summary = "반품 처리", description = "재고를 반품 처리합니다")
    @PostMapping("/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processReturn(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("반품 처리 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        request.setTransactionType(StockTransaction.TransactionType.RETURN);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "반품 처리가 완료되었습니다"));
    }

    /**
     * 폐기 처리
     */
    @Operation(summary = "재고 폐기", description = "만료되거나 손상된 재고를 폐기 처리합니다")
    @PostMapping("/disposal")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processDisposal(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("폐기 처리 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        request.setTransactionType(StockTransaction.TransactionType.DISPOSAL);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "폐기 처리가 완료되었습니다"));
    }

    /**
     * 재고 이동
     */
    @Operation(summary = "재고 이동", description = "재고를 다른 위치로 이동합니다")
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> processTransfer(
            @Valid @RequestBody StockTransactionRequest request) {

        log.info("재고 이동 - 의약품ID: {}, 수량: {}", request.getMedicineId(), request.getQuantity());

        request.setTransactionType(StockTransaction.TransactionType.TRANSFER);

        StockTransactionResponse transaction = stockTransactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "재고 이동이 완료되었습니다"));
    }

    /**
     * 일괄 트랜잭션 처리
     */
    @Operation(summary = "일괄 트랜잭션", description = "여러 트랜잭션을 일괄 처리합니다")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BatchTransactionResult>> processBatchTransactions(
            @Valid @RequestBody BatchTransactionRequest request) {

        log.info("일괄 트랜잭션 처리 - 개수: {}", request.getTransactions().size());

        BatchTransactionResult result = stockTransactionService.processBatchTransactions(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result,
                        String.format("%d개 성공, %d개 실패", result.getSuccessCount(), result.getFailCount())));
    }

    /**
     * 트랜잭션 취소
     */
    @Operation(summary = "트랜잭션 취소", description = "특정 트랜잭션을 취소하고 원복합니다")
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> cancelTransaction(
            @Parameter(description = "트랜잭션 ID", required = true) @PathVariable Long id,
            @Parameter(description = "취소 사유") @RequestParam String reason) {

        log.info("트랜잭션 취소 - ID: {}, 사유: {}", id, reason);

        StockTransactionResponse cancelled = stockTransactionService.cancelTransaction(id, reason);

        return ResponseEntity.ok(ApiResponse.success(cancelled, "트랜잭션이 취소되었습니다"));
    }

    /**
     * 의약품별 트랜잭션 조회
     */
    @Operation(summary = "의약품별 트랜잭션", description = "특정 의약품의 모든 트랜잭션을 조회합니다")
    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getTransactionsByMedicine(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long medicineId,
            @Parameter(description = "조회 기간 (일)") @RequestParam(defaultValue = "30") int days) {

        log.info("의약품별 트랜잭션 조회 - 의약품ID: {}, 기간: {}일", medicineId, days);

        List<StockTransactionResponse> transactions = stockTransactionService
                .getTransactionsByMedicine(medicineId, days);

        return ResponseEntity.ok(ApiResponse.success(transactions,
                String.format("%d개의 트랜잭션이 조회되었습니다", transactions.size())));
    }

    /**
     * 로트번호별 트랜잭션 조회
     */
    @Operation(summary = "로트번호별 트랜잭션", description = "특정 로트번호의 모든 트랜잭션을 조회합니다")
    @GetMapping("/lot/{lotNumber}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getTransactionsByLotNumber(
            @Parameter(description = "로트번호", required = true) @PathVariable String lotNumber) {

        log.info("로트번호별 트랜잭션 조회 - 로트번호: {}", lotNumber);

        List<StockTransactionResponse> transactions = stockTransactionService
                .getTransactionsByLotNumber(lotNumber);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * 일별 트랜잭션 통계
     */
    @Operation(summary = "일별 트랜잭션 통계", description = "지정 날짜의 트랜잭션 통계를 조회합니다")
    @GetMapping("/statistics/daily")
    public ResponseEntity<ApiResponse<DailyTransactionStatistics>> getDailyStatistics(
            @Parameter(description = "조회 날짜")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("일별 트랜잭션 통계 조회 - 날짜: {}", date);

        DailyTransactionStatistics statistics = stockTransactionService.getDailyStatistics(date);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 기간별 트랜잭션 요약
     */
    @Operation(summary = "기간별 트랜잭션 요약", description = "지정 기간의 트랜잭션 요약을 조회합니다")
    @GetMapping("/statistics/summary")
    public ResponseEntity<ApiResponse<TransactionSummary>> getTransactionSummary(
            @Parameter(description = "시작일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("트랜잭션 요약 조회 - 기간: {} ~ {}", startDate, endDate);

        TransactionSummary summary = stockTransactionService.getTransactionSummary(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 부서별 트랜잭션 통계
     */
    @Operation(summary = "부서별 트랜잭션 통계", description = "부서별 트랜잭션 통계를 조회합니다")
    @GetMapping("/statistics/department")
    public ResponseEntity<ApiResponse<Map<String, DepartmentStatistics>>> getDepartmentStatistics(
            @Parameter(description = "시작일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("부서별 트랜잭션 통계 조회 - 기간: {} ~ {}", startDate, endDate);

        Map<String, DepartmentStatistics> statistics = stockTransactionService
                .getDepartmentStatistics(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 최근 트랜잭션 조회
     */
    @Operation(summary = "최근 트랜잭션", description = "최근 N개의 트랜잭션을 조회합니다")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getRecentTransactions(
            @Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") int limit) {

        log.info("최근 트랜잭션 조회 - 개수: {}", limit);

        List<StockTransactionResponse> transactions = stockTransactionService.getRecentTransactions(limit);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * 일괄 트랜잭션 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchTransactionResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
        private List<Long> successIds;
        private List<String> errors;
    }

    /**
     * 일별 트랜잭션 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyTransactionStatistics {
        private LocalDate date;
        private Long totalTransactions;
        private Long inboundCount;
        private Long outboundCount;
        private Long adjustmentCount;
        private Long returnCount;
        private Long disposalCount;
        private Long transferCount;
        private Long totalInboundQuantity;
        private Long totalOutboundQuantity;
        private Map<String, Long> topMedicines;  // 가장 많이 거래된 의약품 Top 5
        private Map<String, Long> departmentActivity;  // 부서별 활동
    }

    /**
     * 트랜잭션 요약 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalTransactions;
        private Map<StockTransaction.TransactionType, Long> transactionsByType;
        private Map<StockTransaction.TransactionReason, Long> transactionsByReason;
        private Long totalQuantityIn;
        private Long totalQuantityOut;
        private Long netQuantityChange;
        private Double estimatedValue;  // 추정 금액
        private List<TopItem> topInboundItems;
        private List<TopItem> topOutboundItems;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class TopItem {
            private Long medicineId;
            private String medicineName;
            private String medicineCode;
            private Long quantity;
            private Long transactionCount;
        }
    }

    /**
     * 부서별 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepartmentStatistics {
        private String department;
        private Long transactionCount;
        private Long totalQuantity;
        private Map<String, Long> medicineUsage;  // 의약품별 사용량
        private Double estimatedCost;
        private List<String> frequentRequesters;  // 자주 요청하는 사람들
    }
}