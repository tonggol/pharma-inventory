package com.pharma.inventory.service;

import com.pharma.inventory.controller.DashboardController;
import com.pharma.inventory.dto.response.DashboardResponse;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.dto.response.StockTransactionResponse;
import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.MedicineRepository;
import com.pharma.inventory.repository.StockRepository;
import com.pharma.inventory.repository.StockTransactionRepository;
import com.pharma.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드 Service
 * 대시보드에 필요한 종합 데이터 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MedicineRepository medicineRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * 종합 대시보드 데이터 조회
     */
    public DashboardResponse getDashboardData(String username) {
        log.info("대시보드 데이터 조회 - 사용자: {}", username);
        
        // 요약 정보
        DashboardResponse.DashboardSummary summary = getSummary();
        
        // 재고 알림
        DashboardResponse.StockAlerts alerts = getStockAlerts();
        
        // 최근 트랜잭션
        List<StockTransactionResponse> recentTransactions = getRecentActivities(10);
        
        // 만료 예정 재고
        List<StockResponse> expiringStocks = getExpiringStocks(30);
        
        // 재고 부족 의약품
        List<DashboardResponse.MedicineStockStatus> lowStockMedicines = getLowStockMedicines();
        
        return DashboardResponse.builder()
                .summary(summary)
                .alerts(alerts)
                .recentTransactions(recentTransactions)
                .expiringStocks(expiringStocks)
                .lowStockMedicines(lowStockMedicines)
                .build();
    }

    /**
     * 대시보드 요약 정보
     */
    public DashboardResponse.DashboardSummary getSummary() {
        log.debug("대시보드 요약 정보 조회");
        
        // 총 의약품 수
        long totalMedicines = medicineRepository.count();
        long activeMedicines = medicineRepository.countByIsActiveTrue();
        
        // 총 재고 정보
        long totalStockItems = stockRepository.count();
        double totalStockValue = calculateTotalStockValue();
        
        // 오늘 트랜잭션
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<StockTransaction> todayTransactions = transactionRepository
                .findByTransactionDateBetween(startOfDay, endOfDay);
        
        long todayInboundCount = todayTransactions.stream()
                .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.INBOUND)
                .count();
        
        long todayOutboundCount = todayTransactions.stream()
                .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.OUTBOUND)
                .count();
        
        // 만료 재고
        long expiredCount = stockRepository.findExpiredStocks(LocalDate.now()).size();
        long expiringSoonCount = stockRepository.findExpiringStocks(LocalDate.now().plusDays(30)).size();
        
        return DashboardResponse.DashboardSummary.builder()
                .totalMedicines(totalMedicines)
                .activeMedicines(activeMedicines)
                .totalStockItems(totalStockItems)
                .totalStockValue(totalStockValue)
                .todayInboundCount(todayInboundCount)
                .todayOutboundCount(todayOutboundCount)
                .expiredCount(expiredCount)
                .expiringSoonCount(expiringSoonCount)
                .build();
    }

    /**
     * 재고 알림
     */
    public DashboardResponse.StockAlerts getStockAlerts() {
        log.debug("재고 알림 조회");
        
        // 재고 수준별 카운트
        long criticalStockCount = 0;
        long lowStockCount = 0;
        long quarantineCount = 0;
        
        List<Medicine> allMedicines = medicineRepository.findAll();
        for (Medicine medicine : allMedicines) {
            Integer currentStock = stockRepository.getTotalQuantityByMedicine(medicine);
            if (currentStock == null) currentStock = 0;
            
            if (currentStock == 0) {
                criticalStockCount++;
            } else if (currentStock < medicine.getMinimumStock()) {
                lowStockCount++;
            }
        }
        
        // 격리 재고
        quarantineCount = stockRepository.findByStatus(Stock.StockStatus.QUARANTINE).size();
        
        // 만료 재고
        long expiredCount = stockRepository.findExpiredStocks(LocalDate.now()).size();
        long expiringSoonCount = stockRepository.findExpiringStocks(LocalDate.now().plusDays(30)).size();
        
        return DashboardResponse.StockAlerts.builder()
                .criticalStockCount(criticalStockCount)
                .lowStockCount(lowStockCount)
                .expiredCount(expiredCount)
                .expiringSoonCount(expiringSoonCount)
                .quarantineCount(quarantineCount)
                .build();
    }

    /**
     * 최근 활동 내역
     */
    public List<StockTransactionResponse> getRecentActivities(int limit) {
        log.debug("최근 활동 조회 - 개수: {}", limit);
        
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"));
        
        return transactionRepository.findAllByOrderByTransactionDateDesc(pageable)
                .getContent()
                .stream()
                .map(StockTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 만료 예정 재고
     */
    public List<StockResponse> getExpiringStocks(int days) {
        log.debug("만료 예정 재고 조회 - 기준: {}일", days);
        
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        
        return stockRepository.findExpiringStocks(expiryDate).stream()
                .map(StockResponse::from)
                .sorted(Comparator.comparing(StockResponse::getExpiryDate))
                .collect(Collectors.toList());
    }

    /**
     * 재고 부족 의약품
     */
    public List<DashboardResponse.MedicineStockStatus> getLowStockMedicines() {
        log.debug("재고 부족 의약품 조회");
        
        List<Medicine> medicines = medicineRepository.findLowStockMedicines();
        
        return medicines.stream()
                .map(medicine -> {
                    Integer currentStock = stockRepository.getTotalQuantityByMedicine(medicine);
                    if (currentStock == null) currentStock = 0;
                    
                    int minStock = medicine.getMinimumStock();
                    
                    return DashboardResponse.MedicineStockStatus.builder()
                            .medicineId(medicine.getId())
                            .medicineName(medicine.getName())
                            .medicineCode(medicine.getCode())
                            .currentStock(currentStock)
                            .minStockQuantity(minStock)
                            .stockPercentage(minStock > 0 ? (currentStock * 100 / minStock) : 0)
                            .status(calculateStockStatus(currentStock, minStock))
                            .build();
                })
                .sorted(Comparator.comparing(DashboardResponse.MedicineStockStatus::getStockPercentage))
                .collect(Collectors.toList());
    }

    // === Helper Methods ===

    private double calculateTotalStockValue() {
        return stockRepository.findAll().stream()
                .mapToDouble(s -> s.getQuantity() * s.getUnitPrice())
                .sum();
    }

    private String calculateStockStatus(int current, int minimum) {
        if (current == 0) return "CRITICAL";
        double ratio = minimum > 0 ? (double) current / minimum : 1.0;
        
        if (ratio < 0.5) return "CRITICAL";
        if (ratio < 1.0) return "LOW";
        if (ratio < 2.0) return "NORMAL";
        return "OVERSTOCK";
    }
}
