package com.pharma.inventory.controller;

import com.pharma.inventory.dto.request.MedicineCreateRequest;
import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.dto.request.StockCreateRequest;
import com.pharma.inventory.dto.request.UserRegisterRequest;
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

    @GetMapping
    public String index() {
        return "redirect:/dashboard/index";
    }

    // Auth
    @GetMapping("auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("auth/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegisterRequest());
        return "auth/register";
    }

    // Dashboard
    @GetMapping("dashboard/index")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null) {
            // 비로그인 사용자에게 빈 데이터 또는 기본값 제공
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
        } else {
            // 로그인 사용자에게 실제 데이터 제공
            model.addAttribute("summary", dashboardService.getSummary());
            model.addAttribute("recentActivities", dashboardService.getRecentActivities(10));
            model.addAttribute("expiringStocks", dashboardService.getExpiringStocks(30));
            model.addAttribute("lowStockMedicines", dashboardService.getLowStockMedicines());
        }
        return "dashboard/index";
    }

    // Medicines
    @GetMapping("medicines")
    public String medicineList(Model model, Pageable pageable) {
        model.addAttribute("medicines", medicineService.getMedicines(pageable));
        model.addAttribute("searchRequest", new MedicineSearchRequest());
        return "medicines/list";
    }

    @GetMapping("medicines/new")
    public String newMedicineForm(Model model) {
        model.addAttribute("medicine", new MedicineCreateRequest());
        return "medicines/form";
    }

    @GetMapping("medicines/{id}")
    public String medicineDetail(@PathVariable Long id, Model model) {
        model.addAttribute("medicine", medicineService.getMedicine(id));
        model.addAttribute("stocks", stockService.getStocksByMedicine(id, false));
        return "medicines/detail";
    }

    @GetMapping("medicines/{id}/edit")
    public String editMedicineForm(@PathVariable Long id, Model model) {
        MedicineResponse medicine = medicineService.getMedicine(id);
        model.addAttribute("medicine", medicine);
        return "medicines/form";
    }

    // Stocks
    @GetMapping("stocks")
    public String stockList(Model model, Pageable pageable) {
        model.addAttribute("stocks", stockService.getStocks(pageable));
        return "stocks/list";
    }

    @GetMapping("stocks/new")
    public String newStockForm(Model model) {
        model.addAttribute("stock", new StockCreateRequest());
        return "stocks/form";
    }

    @GetMapping("stocks/{id}")
    public String stockDetail(@PathVariable Long id, Model model) {
        model.addAttribute("stock", stockService.getStock(id));
        return "stocks/detail";
    }

    @GetMapping("stocks/{id}/edit")
    public String editStockForm(@PathVariable Long id, Model model) {
        StockResponse stock = stockService.getStock(id);
        model.addAttribute("stock", stock);
        return "stocks/form";
    }

    @GetMapping("stocks/audit")
    public String auditStockForm() {
        return "stocks/audit";
    }

    // Transactions
    @GetMapping("transactions")
    public String transactionList(Model model, Pageable pageable) {
        model.addAttribute("transactions", transactionService.getTransactions(pageable));
        return "transactions/list";
    }

    @GetMapping("transactions/new")
    public String newTransactionForm() {
        return "transactions/form";
    }

    @GetMapping("transactions/{id}")
    public String transactionDetail(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.getTransaction(id));
        return "transactions/detail";
    }

    // Users
    @GetMapping("users")
    public String userList(Model model, Pageable pageable) {
        model.addAttribute("users", userService.getUsers(pageable));
        return "users/list";
    }

    @GetMapping("users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new UserRegisterRequest());
        return "users/form";
    }

    @GetMapping("users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUser(id));
        return "profile";
    }

    @GetMapping("users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserResponse user = userService.getUser(id);
        model.addAttribute("user", user);
        return "users/form";
    }
}
