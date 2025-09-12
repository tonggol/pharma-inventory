package com.pharma.inventory.controller;

import com.pharma.inventory.dto.request.MedicineCreateRequest;
import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.dto.request.StockCreateRequest;
import com.pharma.inventory.dto.request.UserRegisterRequest;
import com.pharma.inventory.dto.request.UserSearchRequest;
import com.pharma.inventory.dto.response.UserStats;
import org.springframework.data.domain.Page;
import com.pharma.inventory.dto.response.DashboardSummary;
import com.pharma.inventory.dto.response.MedicineResponse;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.dto.response.UserResponse;
import com.pharma.inventory.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

/**
 * Thymeleaf 뷰를 렌더링하기 위한 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class ViewController {

    private final DashboardService dashboardService;
    private final MedicineService medicineService;
    private final StockService stockService;
    private final StockTransactionService transactionService;
    private final UserService userService;

    // Main Landing Page (홈페이지)
    @GetMapping
    public String home() {
        return "home"; // home.html 템플릿을 반환
    }

    @GetMapping("home")
    public String homePage() {
        return "home";
    }

    // Auth
    @GetMapping("login")
    public String loginPage(Model model) {
        // 이미 로그인된 사용자는 대시보드로 리다이렉트
        // 이 로직은 시큐리티 설정에서도 처리할 수 있습니다
        return "auth/login"; // auth/login.html 템플릿을 반환
    }

    @GetMapping("auth/login")
    public String authLoginPage() {
        return "auth/login"; // auth/login.html 템플릿을 직접 반환
    }

    @GetMapping("register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegisterRequest());
        return "register";
    }

    @GetMapping("auth/register")
    public String authRegisterPage(Model model) {
        return "redirect:/register"; // 기존 경로 호환성을 위한 리다이렉트
    }

    // Dashboard (로그인 후 접근)
    @GetMapping("dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null) {
            // 비로그인 사용자는 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "dashboard");

        // 로그인 사용자에게 실제 데이터 제공
        try {
            model.addAttribute("summary", dashboardService.getSummary());
            model.addAttribute("recentActivities", dashboardService.getRecentActivities(10));
            model.addAttribute("expiringStocks", dashboardService.getExpiringStocks(30));
            model.addAttribute("lowStockMedicines", dashboardService.getLowStockMedicines());
        } catch (Exception e) {
            // 서비스 오류 시 기본값 제공
            model.addAttribute("summary", DashboardSummary.builder()
                    .totalMedicines(0L)
                    .activeMedicines(0L)
                    .totalStockItems(0L)
                    .totalStockValue(0.0)
                    .todayInboundCount(0L)
                    .todayOutboundCount(0L)
                    .expiredCount(0L)
                    .expiringSoonCount(0L)
                    .build());
            model.addAttribute("recentActivities", Collections.emptyList());
            model.addAttribute("expiringStocks", Collections.emptyList());
            model.addAttribute("lowStockMedicines", Collections.emptyList());
        }

        return "dashboard"; // dashboard.html 템플릿을 반환
    }

    @GetMapping("dashboard/index")
    public String dashboardIndex(Model model, Authentication authentication) {
        return "redirect:/dashboard"; // 기존 경로 호환성을 위한 리다이렉트
    }

    // Medicines
    @GetMapping("medicines")
    public String medicineList(Model model, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "medicines");
        model.addAttribute("medicines", medicineService.getMedicines(pageable));
        model.addAttribute("searchRequest", new MedicineSearchRequest());
        return "medicines/list";
    }

    @GetMapping("medicines/new")
    public String newMedicineForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "medicines");
        model.addAttribute("medicine", new MedicineCreateRequest());
        return "medicines/form";
    }

    @GetMapping("medicines/{id}")
    public String medicineDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "medicines");
        model.addAttribute("medicine", medicineService.getMedicine(id));
        model.addAttribute("stocks", stockService.getStocksByMedicine(id, false));
        return "medicines/detail";
    }

    @GetMapping("medicines/{id}/edit")
    public String editMedicineForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "medicines");
        MedicineResponse medicine = medicineService.getMedicine(id);
        model.addAttribute("medicine", medicine);
        return "medicines/form";
    }

    // Stocks
    @GetMapping("stocks")
    public String stockList(Model model, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        model.addAttribute("stocks", stockService.getStocks(pageable));
        return "stocks/list";
    }

    @GetMapping("stocks/inbound")
    public String stockInboundForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        model.addAttribute("pageTitle", "재고 입고");
        model.addAttribute("pageDescription", "새로운 재고를 입고 처리합니다");
        return "stocks/inbound";
    }

    @GetMapping("stocks/outbound")
    public String stockOutboundForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        model.addAttribute("pageTitle", "재고 출고");
        model.addAttribute("pageDescription", "재고를 출고 처리합니다 (FEFO 방식 적용)");
        return "stocks/outbound";
    }

    @GetMapping("stocks/{id}")
    public String stockDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        model.addAttribute("stock", stockService.getStock(id));
        return "stocks/detail";
    }

    @GetMapping("stocks/{id}/edit")
    public String editStockForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        StockResponse stock = stockService.getStock(id);
        model.addAttribute("stock", stock);
        return "stocks/form";
    }

    @GetMapping("stocks/audit")
    public String auditStockForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "stocks");
        return "stocks/audit";
    }

    // Transactions
    @GetMapping("transactions")
    public String transactionList(Model model, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "transactions");
        model.addAttribute("transactions", transactionService.getTransactions(pageable));
        return "transactions/list";
    }

    @GetMapping("transactions/new")
    public String newTransactionForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "transactions");
        return "transactions/form";
    }

    @GetMapping("transactions/{id}")
    public String transactionDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "transactions");
        model.addAttribute("transaction", transactionService.getTransaction(id));
        return "transactions/detail";
    }

    // Users
    @GetMapping("users")
    public String userList(Model model, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "users");

        try {
            // 사용자 목록 조회
            model.addAttribute("users", userService.getUsers(pageable));

            // 사용자 통계 조회 (에러 시 기본값 제공)
            try {
                // model.addAttribute("userStats", userService.getUserStats());
                // 임시로 기본값 제공
                model.addAttribute("userStats", UserStats.builder()
                        .totalUsers(0L)
                        .activeUsers(0L)
                        .adminUsers(0L)
                        .inactiveUsers(0L)
                        .todayRegistrations(0L)
                        .todayLogins(0L)
                        .weeklyRegistrations(0L)
                        .monthlyRegistrations(0L)
                        .build());
            } catch (Exception e) {
                // 통계 조회 실패 시 기본값
                model.addAttribute("userStats", UserStats.builder()
                        .totalUsers(0L)
                        .activeUsers(0L)
                        .adminUsers(0L)
                        .inactiveUsers(0L)
                        .build());
            }

            // 검색 요청 객체 (올바른 타입 사용)
            model.addAttribute("searchRequest", new UserSearchRequest());

        } catch (Exception e) {
            // 전체 조회 실패 시 빈 페이지 객체 제공
            model.addAttribute("users", Page.empty());
            model.addAttribute("userStats", UserStats.builder()
                    .totalUsers(0L)
                    .activeUsers(0L)
                    .adminUsers(0L)
                    .inactiveUsers(0L)
                    .build());
            model.addAttribute("searchRequest", new UserSearchRequest());
            model.addAttribute("error", "사용자 목록을 불러오는 중 오류가 발생했습니다.");
        }

        return "users/list";
    }

    @GetMapping("users/new")
    public String newUserForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "users");
        model.addAttribute("user", new UserRegisterRequest());
        return "users/form";
    }

    @GetMapping("users/{id}")
    public String userDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "users");

        try {
            model.addAttribute("user", userService.getUser(id));
        } catch (Exception e) {
            model.addAttribute("error", "사용자 정보를 불러올 수 없습니다.");
            return "redirect:/users";
        }

        return "users/profile";
    }

    @GetMapping("users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "users");

        try {
            UserResponse user = userService.getUser(id);
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("error", "사용자 정보를 불러올 수 없습니다.");
            return "redirect:/users";
        }

        return "users/form";
    }
    @GetMapping("reports/stock")
    public String stockReport(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "재고 보고서");
        model.addAttribute("pageDescription", "의약품 재고 현황과 분석 데이터를 확인할 수 있습니다.");
        return "reports/stock";
    }

    @GetMapping("reports/expiry")
    public String expiryReport(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "유효기간 보고서");
        model.addAttribute("pageDescription", "의약품 유효기간 현황과 만료 예정 리스트를 확인할 수 있습니다.");
        return "reports/expiry";
    }

    // ViewController.java에 추가할 메서드들

    // System Settings
    @GetMapping("system/settings")
    public String systemSettings(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // ADMIN 권한 체크
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "error/403"; // 403 Forbidden
        }

        model.addAttribute("pageTitle", "시스템 설정");
        model.addAttribute("pageDescription", "시스템 전반적인 설정을 관리할 수 있습니다.");

        // 시스템 설정 정보 로드 (서비스에서 가져올 예정)
        try {
            // model.addAttribute("systemConfig", systemConfigService.getConfig());
            // model.addAttribute("backupInfo", backupService.getLatestBackupInfo());
            // model.addAttribute("systemStatus", systemMonitorService.getSystemStatus());
        } catch (Exception e) {
            // 에러 시 기본값 제공
            model.addAttribute("error", "시스템 설정 정보를 불러오는 중 오류가 발생했습니다.");
        }

        return "system/settings";
    }

    @GetMapping("system/backup")
    public String systemBackup(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // ADMIN 권한 체크
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "error/403";
        }

        model.addAttribute("pageTitle", "백업 관리");
        model.addAttribute("pageDescription", "데이터베이스 백업을 생성하고 관리할 수 있습니다.");

        return "system/backup";
    }

    @GetMapping("system/logs")
    public String systemLogs(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // ADMIN 권한 체크
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "error/403";
        }

        model.addAttribute("pageTitle", "시스템 로그");
        model.addAttribute("pageDescription", "시스템 로그를 조회하고 분석할 수 있습니다.");

        return "system/logs";
    }

    @GetMapping("system/monitor")
    public String systemMonitor(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // ADMIN 권한 체크
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "error/403";
        }

        model.addAttribute("pageTitle", "시스템 모니터링");
        model.addAttribute("pageDescription", "시스템 성능과 상태를 실시간으로 모니터링할 수 있습니다.");

        return "system/monitor";
    }

    // 에러 페이지들
    @GetMapping("error")
    public String errorPage() {
        return "error";
    }

    @GetMapping("403")
    public String accessDeniedPage() {
        return "error/403";
    }

    @GetMapping("404")
    public String notFoundPage() {
        return "error/404";
    }

    @GetMapping("500")
    public String serverErrorPage() {
        return "error/500";
    }
}
