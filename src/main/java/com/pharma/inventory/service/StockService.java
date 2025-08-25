package com.pharma.inventory.service;

import com.pharma.inventory.controller.StockController;
import com.pharma.inventory.dto.request.*;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.entity.StockTransaction;
import com.pharma.inventory.repository.MedicineRepository;
import com.pharma.inventory.repository.StockRepository;
import com.pharma.inventory.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 재고 Service - 재고 관리 핵심 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository transactionRepository;

    @Value("${inventory.alert.expiry-days-warning:30}")
    private int expiryDaysWarning;

    /**
     * 재고 목록 조회 (페이징)
     */
    public Page<StockResponse> getStocks(Pageable pageable) {
        log.debug("재고 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Stock> stocks = stockRepository.findAll(pageable);
        return stocks.map(StockResponse::from);
    }

    /**
     * 재고 검색
     */
    public Page<StockResponse> searchStocks(StockSearchRequest request, Pageable pageable) {
        log.debug("재고 검색 - 조건: {}", request);
        
        Specification<Stock> spec = createSpecification(request);
        Page<Stock> stocks = stockRepository.findAll(spec, pageable);
        return stocks.map(StockResponse::from);
    }

    /**
     * 재고 상세 조회
     */
    public StockResponse getStock(Long id) {
        log.debug("재고 상세 조회 - ID: {}", id);
        
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + id));
        
        return StockResponse.from(stock);
    }

    /**
     * 재고 입고 (신규 재고 등록)
     */
    @Transactional
    public StockResponse createStock(StockCreateRequest request) {
        log.info("재고 입고 - 의약품ID: {}, 로트번호: {}, 수량: {}", 
                request.getMedicineId(), request.getLotNumber(), request.getQuantity());
        
        // 의약품 확인
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + request.getMedicineId()));
        
        // 로트번호 중복 체크
        if (stockRepository.existsByLotNumber(request.getLotNumber())) {
            throw new IllegalArgumentException("이미 존재하는 로트번호입니다: " + request.getLotNumber());
        }
        
        // 재고 생성
        Stock stock = Stock.builder()
                .medicine(medicine)
                .lotNumber(request.getLotNumber())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .manufactureDate(request.getManufactureDate())
                .receivedDate(request.getReceivedDate())
                .supplierName(request.getSupplierName())
                .purchasePrice(request.getPurchasePrice())
                .location(request.getLocation())
                .status(Stock.StockStatus.AVAILABLE)
                .remarks(request.getRemarks())
                .build();
        
        Stock saved = stockRepository.save(stock);
        
        // 입고 트랜잭션 기록
        StockTransaction transaction = StockTransaction.builder()
                .medicine(medicine)
                .stock(saved)
                .transactionType(StockTransaction.TransactionType.INBOUND)
                .quantity(request.getQuantity())
                .beforeQuantity(0)
                .afterQuantity(request.getQuantity())
                .transactionDate(LocalDateTime.now())
                .reason(StockTransaction.TransactionReason.PURCHASE)
                .build();
        
        transactionRepository.save(transaction);
        
        return StockResponse.from(saved);
    }

    /**
     * 재고 정보 수정
     */
    @Transactional
    public StockResponse updateStock(Long id, StockUpdateRequest request) {
        log.info("재고 수정 - ID: {}", id);
        
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + id));
        
        if (request.getQuantity() != null) {
            stock.setQuantity(request.getQuantity());
        }
        if (request.getLocation() != null) {
            stock.setLocation(request.getLocation());
        }
        if (request.getStatus() != null) {
            stock.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            stock.setRemarks(request.getRemarks());
        }
        
        Stock updated = stockRepository.save(stock);
        return StockResponse.from(updated);
    }

    /**
     * 재고 일괄 수정
     */
    @Transactional
    public StockController.BatchUpdateResult batchUpdateStocks(BatchStockUpdateRequest request) {
        log.info("재고 일괄 수정 - 대상: {}개", request.getStockIds().size());
        
        List<String> errors = new ArrayList<>();
        int updatedCount = 0;
        
        for (Long stockId : request.getStockIds()) {
            try {
                Stock stock = stockRepository.findById(stockId).orElseThrow();
                
                if (request.getStatus() != null) {
                    stock.setStatus(request.getStatus());
                }
                if (request.getLocation() != null) {
                    stock.setLocation(request.getLocation());
                }
                if (request.getRemarks() != null) {
                    stock.setRemarks(request.getRemarks());
                }
                
                stockRepository.save(stock);
                updatedCount++;
                
            } catch (Exception e) {
                errors.add("재고 ID " + stockId + ": " + e.getMessage());
            }
        }
        
        return StockController.BatchUpdateResult.builder()
                .requestedCount(request.getStockIds().size())
                .updatedCount(updatedCount)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    /**
     * 로트번호로 재고 조회
     */
    public StockResponse getStockByLotNumber(String lotNumber) {
        log.debug("로트번호로 재고 조회 - 로트번호: {}", lotNumber);
        
        Stock stock = stockRepository.findByLotNumber(lotNumber)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. 로트번호: " + lotNumber));
        
        return StockResponse.from(stock);
    }

    /**
     * 만료 예정 재고 조회
     */
    public List<StockResponse> getExpiringStocks(int days) {
        log.debug("만료 예정 재고 조회 - {}일 이내", days);
        
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(days);
        
        List<Stock> stocks = stockRepository.findExpiringSoonStocks(today, limitDate);
        return stocks.stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 재고 조회
     */
    public List<StockResponse> getExpiredStocks() {
        log.debug("만료된 재고 조회");
        
        List<Stock> stocks = stockRepository.findExpiredStocks(LocalDate.now());
        return stocks.stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 재고 상태별 통계
     */
    public Map<Stock.StockStatus, StockController.StockStatusStatistics> getStockStatusStatistics() {
        log.debug("재고 상태별 통계 조회");
        
        Map<Stock.StockStatus, StockController.StockStatusStatistics> result = new HashMap<>();
        List<Stock> allStocks = stockRepository.findAll();
        long totalCount = allStocks.size();
        
        Map<Stock.StockStatus, List<Stock>> groupedByStatus = allStocks.stream()
                .collect(Collectors.groupingBy(Stock::getStatus));
        
        for (Map.Entry<Stock.StockStatus, List<Stock>> entry : groupedByStatus.entrySet()) {
            Stock.StockStatus status = entry.getKey();
            List<Stock> stocks = entry.getValue();
            
            long count = stocks.size();
            long totalQuantity = stocks.stream().mapToLong(Stock::getQuantity).sum();
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0;
            
            result.put(status, StockController.StockStatusStatistics.builder()
                    .status(status)
                    .statusDescription(status.getDescription())
                    .count(count)
                    .totalQuantity(totalQuantity)
                    .percentage(percentage)
                    .build());
        }
        
        return result;
    }

    /**
     * 기간별 재고 변동 현황
     */
    public StockController.StockChangeStatistics getStockChanges(LocalDate startDate, LocalDate endDate) {
        log.debug("재고 변동 현황 조회 - 기간: {} ~ {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<StockTransaction> transactions = transactionRepository
                .findByTransactionDateBetween(startDateTime, endDateTime);
        
        long totalInbound = transactions.stream()
                .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.INBOUND)
                .mapToLong(StockTransaction::getQuantity)
                .sum();
        
        long totalOutbound = transactions.stream()
                .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.OUTBOUND)
                .mapToLong(StockTransaction::getQuantity)
                .sum();
        
        long totalAdjustment = transactions.stream()
                .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.ADJUSTMENT)
                .mapToLong(StockTransaction::getQuantity)
                .sum();
        
        // 일별 변동 계산
        Map<String, Long> dailyChanges = new HashMap<>();
        transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().toLocalDate().toString(),
                        Collectors.summingLong(t -> {
                            if (t.getTransactionType() == StockTransaction.TransactionType.INBOUND) {
                                return t.getQuantity();
                            } else if (t.getTransactionType() == StockTransaction.TransactionType.OUTBOUND) {
                                return -t.getQuantity();
                            }
                            return 0;
                        })
                ))
                .forEach(dailyChanges::put);
        
        return StockController.StockChangeStatistics.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalInbound(totalInbound)
                .totalOutbound(totalOutbound)
                .totalAdjustment(totalAdjustment)
                .netChange(totalInbound - totalOutbound)
                .dailyChanges(dailyChanges)
                .build();
    }

    /**
     * 재고 실사
     */
    @Transactional
    public StockController.InventoryAuditResult performInventoryAudit(InventoryAuditRequest request) {
        log.info("재고 실사 시작 - 대상: {}개", request.getAuditItems().size());
        
        List<StockController.InventoryAuditResult.DiscrepancyDetail> discrepancies = new ArrayList<>();
        int adjustedCount = 0;
        
        for (InventoryAuditRequest.AuditItem item : request.getAuditItems()) {
            Stock stock = stockRepository.findById(item.getStockId())
                    .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + item.getStockId()));
            
            int systemQuantity = stock.getQuantity();
            int actualQuantity = item.getActualQuantity();
            int difference = actualQuantity - systemQuantity;
            
            if (difference != 0) {
                // 차이가 있으면 조정
                stock.setQuantity(actualQuantity);
                stockRepository.save(stock);
                
                // 조정 트랜잭션 기록
                StockTransaction transaction = StockTransaction.builder()
                        .medicine(stock.getMedicine())
                        .stock(stock)
                        .transactionType(StockTransaction.TransactionType.ADJUSTMENT)
                        .quantity(Math.abs(difference))
                        .beforeQuantity(systemQuantity)
                        .afterQuantity(actualQuantity)
                        .transactionDate(LocalDateTime.now())
                        .reason(StockTransaction.TransactionReason.INVENTORY_CHECK)
                        .remarks(item.getDiscrepancyReason())
                        .build();
                
                transactionRepository.save(transaction);
                
                discrepancies.add(StockController.InventoryAuditResult.DiscrepancyDetail.builder()
                        .stockId(stock.getId())
                        .lotNumber(stock.getLotNumber())
                        .medicineName(stock.getMedicine().getName())
                        .systemQuantity(systemQuantity)
                        .actualQuantity(actualQuantity)
                        .difference(difference)
                        .reason(item.getDiscrepancyReason())
                        .build());
                
                adjustedCount++;
            }
        }
        
        return StockController.InventoryAuditResult.builder()
                .auditedCount(request.getAuditItems().size())
                .discrepancyCount(discrepancies.size())
                .adjustedCount(adjustedCount)
                .discrepancies(discrepancies)
                .auditDate(LocalDate.now())
                .auditor(request.getAuditorName())
                .build();
    }

    /**
     * 위치별 재고 조회
     */
    public List<StockResponse> getStocksByLocation(String location) {
        log.debug("위치별 재고 조회 - 위치: {}", location);
        
        List<Stock> stocks = stockRepository.findByLocation(location);
        return stocks.stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 재고 가치 평가
     */
    public StockController.StockValuation calculateStockValuation() {
        log.debug("재고 가치 평가 시작");
        
        List<Stock> allStocks = stockRepository.findAll();
        
        double totalValue = 0.0;
        long totalItems = allStocks.size();
        long totalQuantity = 0;
        Map<String, StockController.StockValuation.CategoryValuation> categoryValuations = new HashMap<>();
        
        for (Stock stock : allStocks) {
            if (stock.getPurchasePrice() != null) {
                double stockValue = stock.getQuantity() * stock.getPurchasePrice();
                totalValue += stockValue;
                
                String category = stock.getMedicine().getCategory();
                if (category != null) {
                    categoryValuations.computeIfAbsent(category, k -> 
                            StockController.StockValuation.CategoryValuation.builder()
                                    .category(k)
                                    .value(0.0)
                                    .quantity(0L)
                                    .build()
                    );
                    
                    StockController.StockValuation.CategoryValuation catVal = categoryValuations.get(category);
                    catVal.setValue(catVal.getValue() + stockValue);
                    catVal.setQuantity(catVal.getQuantity() + stock.getQuantity());
                }
            }
            totalQuantity += stock.getQuantity();
        }
        
        // 카테고리별 비율 계산
        for (StockController.StockValuation.CategoryValuation catVal : categoryValuations.values()) {
            catVal.setPercentageOfTotal(totalValue > 0 ? (catVal.getValue() * 100 / totalValue) : 0);
        }
        
        return StockController.StockValuation.builder()
                .totalValue(totalValue)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .categoryValuations(categoryValuations)
                .valuationDate(LocalDate.now())
                .build();
    }

    // === 기존 메서드들 유지 ===
    
    /**
     * 모든 재고 조회
     */
    public List<Stock> getAllStocks() {
        log.debug("모든 재고 조회");
        return stockRepository.findAll();
    }

    /**
     * 사용 가능한 재고 조회
     */
    public List<Stock> getAvailableStocks() {
        log.debug("사용 가능한 재고 조회");
        return stockRepository.findAvailableStocks();
    }

    /**
     * ID로 재고 조회 (Entity 반환)
     */
    public Stock getStockById(Long id) {
        log.debug("재고 조회 - ID: {}", id);
        return stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 의약품별 재고 조회 (Entity 반환)
     */
    public List<Stock> getStocksByMedicine(Long medicineId) {
        log.debug("의약품별 재고 조회 - 의약품 ID: {}", medicineId);
        return stockRepository.findByMedicineId(medicineId);
    }
    
    /**
     * 의약품별 재고 조회 (만료 포함 옵션)
     */
    public List<StockResponse> getStocksByMedicine(Long medicineId, boolean includeExpired) {
        log.debug("의약품별 재고 조회 - 의약품 ID: {}, 만료 포함: {}", medicineId, includeExpired);
        
        List<Stock> stocks = stockRepository.findByMedicineId(medicineId);
        
        if (!includeExpired) {
            stocks = stocks.stream()
                    .filter(stock -> !stock.isExpired())
                    .collect(Collectors.toList());
        }
        
        return stocks.stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 재고 입고 처리 (기존 메서드)
     */
    @Transactional
    public Stock createInboundStock(Stock stock, Integer quantity, String requesterName) {
        log.info("재고 입고 - 로트번호: {}, 수량: {}", stock.getLotNumber(), quantity);

        if (stockRepository.existsByLotNumber(stock.getLotNumber())) {
            throw new IllegalArgumentException("이미 존재하는 로트번호입니다: " + stock.getLotNumber());
        }

        stock.setQuantity(quantity);
        stock.setStatus(Stock.StockStatus.AVAILABLE);
        Stock savedStock = stockRepository.save(stock);

        StockTransaction transaction = StockTransaction.builder()
                .medicine(stock.getMedicine())
                .stock(savedStock)
                .transactionType(StockTransaction.TransactionType.INBOUND)
                .quantity(quantity)
                .beforeQuantity(0)
                .afterQuantity(quantity)
                .transactionDate(LocalDateTime.now())
                .requesterName(requesterName)
                .reason(StockTransaction.TransactionReason.PURCHASE)
                .build();

        transactionRepository.save(transaction);

        return savedStock;
    }

    /**
     * 재고 출고 처리 (FEFO 방식)
     */
    @Transactional
    public void processOutboundStock(Long medicineId, Integer requestedQuantity,
                                     String department, String requesterName) {
        log.info("재고 출고 - 의약품 ID: {}, 요청 수량: {}", medicineId, requestedQuantity);

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + medicineId));

        List<Stock> availableStocks = stockRepository
                .findAvailableStocksByMedicineOrderByExpiryDate(medicine);

        int totalAvailable = availableStocks.stream()
                .mapToInt(Stock::getQuantity)
                .sum();

        if (totalAvailable < requestedQuantity) {
            throw new IllegalStateException(
                    String.format("재고가 부족합니다. 요청: %d, 가용: %d", requestedQuantity, totalAvailable)
            );
        }

        int remainingQuantity = requestedQuantity;
        for (Stock stock : availableStocks) {
            if (remainingQuantity <= 0) break;

            int stockQuantity = stock.getQuantity();
            int deductQuantity = Math.min(stockQuantity, remainingQuantity);

            stock.setQuantity(stockQuantity - deductQuantity);
            stockRepository.save(stock);

            StockTransaction transaction = StockTransaction.builder()
                    .medicine(medicine)
                    .stock(stock)
                    .transactionType(StockTransaction.TransactionType.OUTBOUND)
                    .quantity(deductQuantity)
                    .beforeQuantity(stockQuantity)
                    .afterQuantity(stock.getQuantity())
                    .transactionDate(LocalDateTime.now())
                    .department(department)
                    .requesterName(requesterName)
                    .reason(StockTransaction.TransactionReason.PRESCRIPTION)
                    .build();

            transactionRepository.save(transaction);

            remainingQuantity -= deductQuantity;

            log.debug("출고 처리 - 로트번호: {}, 차감: {}, 잔여: {}",
                    stock.getLotNumber(), deductQuantity, stock.getQuantity());
        }
    }

    /**
     * 유효기간 임박 재고 조회 (기존 메서드)
     */
    public List<Stock> getExpiringSoonStocks() {
        log.debug("유효기간 임박 재고 조회 - {}일 이내", expiryDaysWarning);
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(expiryDaysWarning);
        return stockRepository.findExpiringSoonStocks(today, limitDate);
    }

    /**
     * 검색 조건에 따른 Specification 생성
     */
    private Specification<Stock> createSpecification(StockSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getMedicineId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("medicine").get("id"), request.getMedicineId()));
            }
            
            if (request.getMedicineName() != null && !request.getMedicineName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("medicine").get("name")),
                        "%" + request.getMedicineName().toLowerCase() + "%"
                ));
            }
            
            if (request.getLotNumber() != null && !request.getLotNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lotNumber")),
                        "%" + request.getLotNumber().toLowerCase() + "%"
                ));
            }
            
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"
                ));
            }
            
            if (request.getExpiryDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("expiryDate"), request.getExpiryDateFrom()
                ));
            }
            
            if (request.getExpiryDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("expiryDate"), request.getExpiryDateTo()
                ));
            }
            
            if (request.getIsExpired() != null && request.getIsExpired()) {
                predicates.add(criteriaBuilder.lessThan(
                        root.get("expiryDate"), LocalDate.now()
                ));
            }
            
            if (request.getIsExpiringSoon() != null && request.getIsExpiringSoon()) {
                int days = request.getExpiryDaysThreshold() != null ? request.getExpiryDaysThreshold() : 30;
                predicates.add(criteriaBuilder.between(
                        root.get("expiryDate"),
                        LocalDate.now(),
                        LocalDate.now().plusDays(days)
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}