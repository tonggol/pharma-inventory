package com.pharma.inventory.controller;

import com.pharma.inventory.dto.common.ApiResponse;
import com.pharma.inventory.dto.response.DashboardResponse;
import com.pharma.inventory.dto.response.NotificationResponse;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.dto.response.StockTransactionResponse;
import com.pharma.inventory.service.DashboardService;
import com.pharma.inventory.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.format.annotation.DateTimeFormat;  // TODO: 통계 기능 구현시 활성화
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate;  // TODO: 통계 기능 구현시 활성화
import java.util.List;
// import java.util.Map;  // TODO: 통계 기능 구현시 활성화

/**
 * 대시보드 REST API Controller
 * 홈 화면에 필요한 종합 정보 제공
 */
@Tag(name = "Dashboard", description = "대시보드 API")
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationService notificationService;

    /**
     * 종합 대시보드 데이터 조회
     */
    @Operation(summary = "대시보드 조회", description = "종합 대시보드 정보를 조회합니다")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            Authentication authentication) {

        log.info("대시보드 조회 - 사용자: {}", authentication.getName());

        DashboardResponse dashboard = dashboardService.getDashboardData(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * 대시보드 요약 정보
     */
    @Operation(summary = "요약 정보", description = "대시보드 요약 통계를 조회합니다")
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse.DashboardSummary>> getSummary() {

        log.info("대시보드 요약 조회");

        DashboardResponse.DashboardSummary summary = dashboardService.getSummary();

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 재고 알림 조회
     */
    @Operation(summary = "재고 알림", description = "재고 관련 알림을 조회합니다")
    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse.StockAlerts>> getStockAlerts() {

        log.info("재고 알림 조회");

        DashboardResponse.StockAlerts alerts = dashboardService.getStockAlerts();

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /**
     * 최근 활동 내역
     */
    @Operation(summary = "최근 활동", description = "최근 트랜잭션 내역을 조회합니다")
    @GetMapping("/recent-activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getRecentActivities(
            @Parameter(description = "조회 개수 (기본 10)") @RequestParam(defaultValue = "10") int limit) {

        log.info("최근 활동 조회 - 개수: {}", limit);

        List<StockTransactionResponse> activities = dashboardService.getRecentActivities(limit);

        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    /**
     * 만료 예정 재고 목록
     */
    @Operation(summary = "만료 예정 재고", description = "만료 예정인 재고 목록을 조회합니다")
    @GetMapping("/expiring-stocks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getExpiringStocks(
            @Parameter(description = "기준 일수 (기본 30)") @RequestParam(defaultValue = "30") int days) {

        log.info("만료 예정 재고 조회 - 기준: {}일", days);

        List<StockResponse> stocks = dashboardService.getExpiringStocks(days);

        return ResponseEntity.ok(ApiResponse.success(stocks,
                String.format("%d개의 재고가 %d일 이내 만료 예정입니다", stocks.size(), days)));
    }

    /**
     * 재고 부족 의약품 목록
     */
    @Operation(summary = "재고 부족 의약품", description = "재고가 부족한 의약품 목록을 조회합니다")
    @GetMapping("/low-stock-medicines")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DashboardResponse.MedicineStockStatus>>> getLowStockMedicines() {

        log.info("재고 부족 의약품 조회");

        List<DashboardResponse.MedicineStockStatus> medicines = dashboardService.getLowStockMedicines();

        return ResponseEntity.ok(ApiResponse.success(medicines,
                String.format("%d개의 의약품이 재고 부족 상태입니다", medicines.size())));
    }

    // TODO: 아래 통계 기능들은 추후 구현 예정
    /*
    /**
     * 일별 트랜잭션 통계
     */
    /*
    @Operation(summary = "일별 통계", description = "지정 날짜의 트랜잭션 통계를 조회합니다")
    @GetMapping("/statistics/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<DailyStatistics>> getDailyStatistics(
            @Parameter(description = "조회 날짜 (기본 오늘)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("일별 통계 조회 - 날짜: {}", date);

        DailyStatistics statistics = dashboardService.getDailyStatistics(date);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    */

    /*
    /**
     * 주간 트렌드
     */
    /*
    @Operation(summary = "주간 트렌드", description = "최근 7일간의 트렌드를 조회합니다")
    @GetMapping("/statistics/weekly-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<WeeklyTrend>> getWeeklyTrend() {

        log.info("주간 트렌드 조회");

        WeeklyTrend trend = dashboardService.getWeeklyTrend();

        return ResponseEntity.ok(ApiResponse.success(trend));
    }
    */

    /*
    /**
     * 월간 통계
     */
    /*
    @Operation(summary = "월간 통계", description = "지정 월의 통계를 조회합니다")
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MonthlyStatistics>> getMonthlyStatistics(
            @Parameter(description = "년도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) Integer month) {

        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        log.info("월간 통계 조회 - {}년 {}월", year, month);

        MonthlyStatistics statistics = dashboardService.getMonthlyStatistics(year, month);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    */

    /*
    /**
     * 카테고리별 재고 현황
     */
    /*
    @Operation(summary = "카테고리별 재고", description = "카테고리별 재고 현황을 조회합니다")
    @GetMapping("/stock-by-category")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, CategoryStockInfo>>> getStockByCategory() {

        log.info("카테고리별 재고 현황 조회");

        Map<String, CategoryStockInfo> stockInfo = dashboardService.getStockByCategory();

        return ResponseEntity.ok(ApiResponse.success(stockInfo));
    }
    */

    /*
    /**
     * 부서별 사용량
     */
    /*
    @Operation(summary = "부서별 사용량", description = "부서별 의약품 사용량을 조회합니다")
    @GetMapping("/usage-by-department")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, DepartmentUsage>>> getUsageByDepartment(
            @Parameter(description = "시작일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("부서별 사용량 조회 - 기간: {} ~ {}", startDate, endDate);

        Map<String, DepartmentUsage> usage = dashboardService.getUsageByDepartment(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(usage));
    }
    */

    /**
     * 알림 목록 조회
     */
    @Operation(summary = "알림 목록", description = "사용자의 알림 목록을 조회합니다")
    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            Authentication authentication,
            @Parameter(description = "읽지 않은 알림만") @RequestParam(defaultValue = "false") boolean unreadOnly) {

        log.info("알림 조회 - 사용자: {}, 읽지 않은 것만: {}", authentication.getName(), unreadOnly);

        List<NotificationResponse> notifications = notificationService
                .getUserNotifications(authentication.getName(), unreadOnly);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 알림 읽음 처리
     */
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음으로 표시합니다")
    @PutMapping("/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(
            @Parameter(description = "알림 ID", required = true) @PathVariable Long id,
            Authentication authentication) {

        log.info("알림 읽음 처리 - ID: {}, 사용자: {}", id, authentication.getName());

        notificationService.markAsRead(id, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(null, "알림이 읽음 처리되었습니다"));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Operation(summary = "모든 알림 읽음", description = "모든 알림을 읽음으로 표시합니다")
    @PutMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> markAllNotificationsAsRead(
            Authentication authentication) {

        log.info("모든 알림 읽음 처리 - 사용자: {}", authentication.getName());

        int count = notificationService.markAllAsRead(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(count,
                String.format("%d개의 알림이 읽음 처리되었습니다", count)));
    }

    /*
    /**
     * 퀵 액세스 메뉴
     */
    /*
    @Operation(summary = "퀵 액세스", description = "자주 사용하는 기능 목록을 조회합니다")
    @GetMapping("/quick-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<QuickAccessItem>>> getQuickAccess(
            Authentication authentication) {

        log.info("퀵 액세스 조회 - 사용자: {}", authentication.getName());

        List<QuickAccessItem> items = dashboardService.getQuickAccessItems(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(items));
    }
    */

    // === DTO Classes ===
    // TODO: 아래 DTO들은 통계 기능 구현시 활성화 예정

    /*
    /**
     * 일별 통계 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyStatistics {
        private LocalDate date;
        private Long totalTransactions;
        private Long inboundCount;
        private Long outboundCount;
        private Long newMedicines;
        private Long expiredItems;
        private Double totalValue;
        private Map<String, Long> transactionsByType;
        private List<TopItem> topMedicines;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class TopItem {
            private Long medicineId;
            private String medicineName;
            private Long transactionCount;
            private Long quantity;
        }
    }
    */

    /*
    /**
     * 주간 트렌드 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeeklyTrend {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DailyTrend> dailyTrends;
        private Long totalInbound;
        private Long totalOutbound;
        private Double averageDaily;
        private String trendDirection; // UP, DOWN, STABLE
        private Double changePercentage;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class DailyTrend {
            private LocalDate date;
            private Long inbound;
            private Long outbound;
            private Long netChange;
        }
    }
    */

    /*
    /**
     * 월간 통계 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MonthlyStatistics {
        private Integer year;
        private Integer month;
        private Long totalTransactions;
        private Map<String, Long> transactionsByType;
        private Double totalInboundValue;
        private Double totalOutboundValue;
        private Long uniqueMedicines;
        private Long activeDays;
        private Map<String, Double> categoryPerformance;
        private ComparisonData comparisonWithLastMonth;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ComparisonData {
            private Double transactionChange;
            private Double valueChange;
            private String trend;
        }
    }
    */

    /*
    /**
     * 카테고리별 재고 정보 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryStockInfo {
        private String category;
        private Long totalItems;
        private Long totalQuantity;
        private Double totalValue;
        private Long lowStockItems;
        private Long expiringItems;
        private Double percentageOfTotal;
    }
    */

    /*
    /**
     * 부서별 사용량 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepartmentUsage {
        private String department;
        private Long transactionCount;
        private Long totalQuantity;
        private Double totalValue;
        private Map<String, Long> topMedicines;
        private List<String> frequentUsers;
    }
    */

    /*
    /**
     * 퀵 액세스 아이템 DTO
     */
    /*
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QuickAccessItem {
        private String id;
        private String title;
        private String description;
        private String icon;
        private String url;
        private String category;
        private Integer usageCount;
    }
    */
}