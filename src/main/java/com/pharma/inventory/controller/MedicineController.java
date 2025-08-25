package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.common.PageRequest;
import com.pharma.inventory.dto.common.PageResponse;
import com.pharma.inventory.dto.request.MedicineCreateRequest;
import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.dto.request.MedicineUpdateRequest;
import com.pharma.inventory.dto.response.MedicineResponse;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.service.MedicineService;
import com.pharma.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 의약품 관리 REST API Controller
 * 의약품의 CRUD 및 검색, 재고 조회 기능 제공
 */
@Tag(name = "Medicine", description = "의약품 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;
    private final StockService stockService;

    /**
     * 의약품 목록 조회 (페이징)
     */
    @Operation(summary = "의약품 목록 조회", description = "페이징된 의약품 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MedicineResponse>>> getMedicines(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction) {

        log.info("의약품 목록 조회 요청 - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(org.springframework.data.domain.Sort.Direction.valueOf(direction))
                .build();

        Page<MedicineResponse> medicines = medicineService.getMedicines(pageRequest.toPageable());
        PageResponse<MedicineResponse> pageResponse = PageResponse.from(medicines);

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * 의약품 검색
     */
    @Operation(summary = "의약품 검색", description = "조건에 따라 의약품을 검색합니다")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<MedicineResponse>>> searchMedicines(
            @Valid @RequestBody MedicineSearchRequest searchRequest,
            @Parameter(description = "페이지 정보") @ModelAttribute PageRequest pageRequest) {

        log.info("의약품 검색 요청 - 검색조건: {}", searchRequest);

        Page<MedicineResponse> results = medicineService.searchMedicines(
                searchRequest, pageRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(results),
                String.format("%d개의 의약품이 검색되었습니다", results.getTotalElements())
        ));
    }

    /**
     * 의약품 상세 조회
     */
    @Operation(summary = "의약품 상세 조회", description = "ID로 의약품 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicine(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long id) {

        log.info("의약품 상세 조회 - ID: {}", id);

        MedicineResponse medicine = medicineService.getMedicine(id);
        return ResponseEntity.ok(ApiResponse.success(medicine));
    }

    /**
     * 의약품 코드로 조회
     */
    @Operation(summary = "의약품 코드로 조회", description = "의약품 코드로 상세 정보를 조회합니다")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineByCode(
            @Parameter(description = "의약품 코드", required = true) @PathVariable String code) {

        log.info("의약품 조회 - 코드: {}", code);

        MedicineResponse medicine = medicineService.getMedicineByCode(code);
        return ResponseEntity.ok(ApiResponse.success(medicine));
    }

    /**
     * 의약품 등록
     */
    @Operation(summary = "의약품 등록", description = "새로운 의약품을 등록합니다")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponse>> createMedicine(
            @Valid @RequestBody MedicineCreateRequest request) {

        log.info("의약품 등록 요청 - 코드: {}, 이름: {}", request.getCode(), request.getName());

        MedicineResponse created = medicineService.createMedicine(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "의약품이 성공적으로 등록되었습니다"));
    }

    /**
     * 의약품 수정
     */
    @Operation(summary = "의약품 수정", description = "의약품 정보를 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MedicineUpdateRequest request) {

        log.info("의약품 수정 요청 - ID: {}", id);

        MedicineResponse updated = medicineService.updateMedicine(id, request);

        return ResponseEntity.ok(ApiResponse.success(updated, "의약품 정보가 수정되었습니다"));
    }

    /**
     * 의약품 삭제 (비활성화)
     */
    @Operation(summary = "의약품 삭제", description = "의약품을 비활성화합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long id) {

        log.info("의약품 삭제 요청 - ID: {}", id);

        medicineService.deleteMedicine(id);

        return ResponseEntity.ok(ApiResponse.success(null, "의약품이 비활성화되었습니다"));
    }

    /**
     * 의약품별 재고 목록 조회
     */
    @Operation(summary = "의약품별 재고 조회", description = "특정 의약품의 모든 재고 정보를 조회합니다")
    @GetMapping("/{id}/stocks")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getMedicineStocks(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long id,
            @Parameter(description = "만료된 재고 포함 여부") @RequestParam(defaultValue = "false") boolean includeExpired) {

        log.info("의약품 재고 조회 - 의약품 ID: {}, 만료 포함: {}", id, includeExpired);

        List<StockResponse> stocks = stockService.getStocksByMedicine(id, includeExpired);

        return ResponseEntity.ok(ApiResponse.success(stocks,
                String.format("%d개의 재고가 조회되었습니다", stocks.size())));
    }

    /**
     * 의약품 재고 요약 정보
     */
    @Operation(summary = "의약품 재고 요약", description = "의약품의 재고 요약 정보를 조회합니다")
    @GetMapping("/{id}/stock-summary")
    public ResponseEntity<ApiResponse<MedicineStockSummary>> getMedicineStockSummary(
            @Parameter(description = "의약품 ID", required = true) @PathVariable Long id) {

        log.info("의약품 재고 요약 조회 - ID: {}", id);

        MedicineStockSummary summary = medicineService.getStockSummary(id);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 재고 부족 의약품 목록
     */
    @Operation(summary = "재고 부족 의약품 조회", description = "최소 재고 미만인 의약품 목록을 조회합니다")
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getLowStockMedicines() {

        log.info("재고 부족 의약품 조회 요청");

        List<MedicineResponse> medicines = medicineService.getLowStockMedicines();

        return ResponseEntity.ok(ApiResponse.success(medicines,
                String.format("%d개의 의약품이 재고 부족 상태입니다", medicines.size())));
    }

    /**
     * 의약품 일괄 등록 (Excel)
     */
    @Operation(summary = "의약품 일괄 등록", description = "Excel 파일로 의약품을 일괄 등록합니다")
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkUploadResult>> bulkCreateMedicines(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        log.info("의약품 일괄 등록 요청 - 파일명: {}", file.getOriginalFilename());

        BulkUploadResult result = medicineService.bulkCreateFromExcel(file);

        return ResponseEntity.ok(ApiResponse.success(result,
                String.format("%d개 성공, %d개 실패", result.getSuccessCount(), result.getFailCount())));
    }

    /**
     * 의약품 재고 요약 정보 DTO (내부 클래스)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MedicineStockSummary {
        private Long medicineId;
        private String medicineName;
        private String medicineCode;
        private Integer totalQuantity;      // 총 재고 수량
        private Integer availableQuantity;  // 사용 가능 수량
        private Integer reservedQuantity;   // 예약된 수량
        private Integer expiredQuantity;    // 만료된 수량
        private Integer expiringQuantity;   // 만료 임박 수량 (30일 이내)
        private Integer minStockQuantity;   // 최소 재고 수량
        private String stockStatus;         // SUFFICIENT, LOW, CRITICAL, OUT_OF_STOCK
        private Integer distinctLots;       // 로트 번호 개수
        private Double totalValue;          // 총 재고 가치
    }

    /**
     * 일괄 업로드 결과 DTO (내부 클래스)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkUploadResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
        private List<String> errors;
        private List<Long> createdIds;
    }
}