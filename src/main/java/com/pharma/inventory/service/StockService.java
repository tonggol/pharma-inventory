package com.pharma.inventory.service;

import com.pharma.inventory.controller.StockController;
import com.pharma.inventory.dto.request.*;
import com.pharma.inventory.dto.response.StockResponse;
import com.pharma.inventory.entity.*;
import com.pharma.inventory.repository.MedicineRepository;
import com.pharma.inventory.repository.StockRepository;
import com.pharma.inventory.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 재고 관리 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository transactionRepository;

    // ===================================================================================
    // CRUD 및 기본 조회 메서드
    // ===================================================================================

    public Page<StockResponse> getStocks(Pageable pageable) {
        Page<Stock> stocks = stockRepository.findAll(pageable);
        return stocks.map(StockResponse::from);
    }

    public StockResponse getStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + id));
        return StockResponse.from(stock);
    }

    public Page<StockResponse> searchStocks(StockSearchRequest request, Pageable pageable) {
        Specification<Stock> spec = createSpecification(request);
        Page<Stock> stocks = stockRepository.findAll(spec, pageable);
        return stocks.map(StockResponse::from);
    }

    @Transactional
    public StockResponse createStock(StockCreateRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + request.getMedicineId()));

        if (stockRepository.existsByLotNumber(request.getLotNumber())) {
            throw new IllegalArgumentException("이미 존재하는 로트번호입니다: " + request.getLotNumber());
        }

        Stock stock = new Stock(
                medicine,
                request.getLotNumber(),
                request.getQuantity(),
                request.getManufactureDate(),
                request.getExpiryDate()
        );
        stock.setSupplierInfo(request.getSupplierName(), request.getPurchasePrice() != null ? BigDecimal.valueOf(request.getPurchasePrice()) : null);
        stock.updateLocation(request.getLocation());
        stock.addRemarks(request.getRemarks());

        Stock savedStock = stockRepository.save(stock);

        StockTransaction transaction = new StockTransaction(
                medicine, savedStock, TransactionType.INBOUND, request.getQuantity(), 0, request.getQuantity(),
                LocalDateTime.now(), TransactionReason.PURCHASE
        );
        transactionRepository.save(transaction);

        return StockResponse.from(savedStock);
    }

    @Transactional
    public StockResponse updateStock(Long id, StockUpdateRequest request) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + id));

        if (request.getQuantity() != null) stock.adjustQuantity(request.getQuantity());
        if (request.getLocation() != null) stock.updateLocation(request.getLocation());
        if (request.getStatus() != null) stock.updateStatus(request.getStatus());
        if (request.getRemarks() != null) stock.updateRemarks(request.getRemarks());

        return StockResponse.from(stockRepository.save(stock));
    }

    @Transactional
    public StockController.BatchUpdateResult batchUpdateStocks(BatchStockUpdateRequest request) {
        List<String> errors = new ArrayList<>();
        int updatedCount = 0;

        for (Long stockId : request.getStockIds()) {
            try {
                Stock stock = stockRepository.findById(stockId)
                        .orElseThrow(() -> new NoSuchElementException("ID: " + stockId));

                if (request.getStatus() != null) stock.updateStatus(request.getStatus());
                if (request.getLocation() != null) stock.updateLocation(request.getLocation());
                if (request.getRemarks() != null) stock.updateRemarks(request.getRemarks());

                stockRepository.save(stock);
                updatedCount++;
            } catch (Exception e) {
                errors.add(String.format("재고 ID %d 수정 실패: %s", stockId, e.getMessage()));
            }
        }

        return new StockController.BatchUpdateResult(request.getStockIds().size(), updatedCount, errors.size(), errors);
    }

    // ===================================================================================
    // 핵심 비즈니스 로직
    // ===================================================================================

    /**
     * 의약품 출고를 처리합니다. (FEFO: First-Expired, First-Out)
     * 유효기간이 가장 임박한 재고부터 출고 처리하고, 트랜잭션을 기록합니다.
     *
     * @param medicineId        출고할 의약품 ID
     * @param requestedQuantity 출고 요청 수량
     * @param department        출고 요청 부서
     * @param requesterName     출고 요청자 이름
     * @throws IllegalArgumentException 의약품을 찾을 수 없을 경우
     * @throws IllegalStateException    재고가 부족할 경우
     */
    @Transactional
    public void processOutboundStock(Long medicineId, Integer requestedQuantity, String department, String requesterName) {
        log.info("재고 출고 처리 시작 - 의약품 ID: {}, 요청 수량: {}", medicineId, requestedQuantity);

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + medicineId));

        List<Stock> availableStocks = stockRepository.findAvailableStocksByMedicineOrderByExpiryDate(medicine);

        int totalAvailable = availableStocks.stream().mapToInt(Stock::getQuantity).sum();

        if (totalAvailable < requestedQuantity) {
            throw new IllegalStateException(
                    String.format("재고가 부족합니다. 요청 수량: %d, 현재 가용 재고: %d", requestedQuantity, totalAvailable)
            );
        }

        int remainingQuantity = requestedQuantity;
        for (Stock stock : availableStocks) {
            if (remainingQuantity <= 0) break;

            int stockQuantity = stock.getQuantity();
            int deductQuantity = Math.min(stockQuantity, remainingQuantity);

            stock.decreaseQuantity(deductQuantity);
            stockRepository.save(stock);

            StockTransaction transaction = new StockTransaction(
                    medicine, stock, TransactionType.OUTBOUND, deductQuantity, stockQuantity, stock.getQuantity(),
                    LocalDateTime.now(), TransactionReason.PRESCRIPTION
            );
            transaction.setReferenceInfo(null, department);
            transaction.setRequesterInfo(requesterName, null);
            transactionRepository.save(transaction);

            remainingQuantity -= deductQuantity;
        }
    }

    @Transactional
    public StockController.InventoryAuditResult performInventoryAudit(InventoryAuditRequest request) {
        List<StockController.InventoryAuditResult.DiscrepancyDetail> discrepancies = new ArrayList<>();
        int adjustedCount = 0;

        for (InventoryAuditRequest.AuditItem item : request.getAuditItems()) {
            Stock stock = stockRepository.findById(item.getStockId())
                    .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. ID: " + item.getStockId()));

            int systemQuantity = stock.getQuantity();
            int actualQuantity = item.getActualQuantity();
            int difference = actualQuantity - systemQuantity;

            if (difference != 0) {
                stock.adjustQuantity(actualQuantity);
                stockRepository.save(stock);

                StockTransaction transaction = new StockTransaction(stock.getMedicine(), stock, TransactionType.ADJUSTMENT, Math.abs(difference), systemQuantity, actualQuantity, LocalDateTime.now(), TransactionReason.INVENTORY_CHECK);
                transactionRepository.save(transaction);

                discrepancies.add(new StockController.InventoryAuditResult.DiscrepancyDetail(stock.getId(), stock.getLotNumber(), stock.getMedicine().getName(), systemQuantity, actualQuantity, difference, item.getDiscrepancyReason()));
                adjustedCount++;
            }
        }

        return new StockController.InventoryAuditResult(request.getAuditItems().size(), discrepancies.size(), adjustedCount, discrepancies, LocalDate.now(), request.getAuditorName());
    }

    // ===================================================================================
    // 특정 조건 조회 메서드
    // ===================================================================================

    public StockResponse getStockByLotNumber(String lotNumber) {
        Stock stock = stockRepository.findByLotNumber(lotNumber)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. 로트번호: " + lotNumber));
        return StockResponse.from(stock);
    }

    public List<StockResponse> getStocksByMedicine(Long medicineId, boolean includeExpired) {
        List<Stock> stocks = stockRepository.findByMedicineId(medicineId);
        Stream<Stock> stockStream = stocks.stream();

        if (!includeExpired) {
            stockStream = stockStream.filter(stock -> !stock.isExpired());
        }

        return stockStream.map(StockResponse::from).toList();
    }

    public List<StockResponse> getStocksByLocation(String location) {
        List<Stock> stocks = stockRepository.findByLocation(location);
        return stocks.stream().map(StockResponse::from).toList();
    }

    public List<StockResponse> getExpiringStocks(int days) {
        LocalDate limitDate = LocalDate.now().plusDays(days);
        List<Stock> stocks = stockRepository.findExpiringSoonStocks(LocalDate.now(), limitDate);
        return stocks.stream().map(StockResponse::from).toList();
    }

    public List<StockResponse> getExpiredStocks() {
        List<Stock> stocks = stockRepository.findExpiredStocks(LocalDate.now());
        return stocks.stream().map(StockResponse::from).toList();
    }

    // ===================================================================================
    // 통계 및 분석 메서드
    // ===================================================================================

    public Map<StockStatus, StockController.StockStatusStatistics> getStockStatusStatistics() {
        Map<StockStatus, List<Stock>> groupedByStatus = stockRepository.findAll().stream()
                .collect(Collectors.groupingBy(Stock::getStatus));

        Map<StockStatus, StockController.StockStatusStatistics> result = new EnumMap<>(StockStatus.class);
        long totalCount = groupedByStatus.values().stream().mapToLong(List::size).sum();

        groupedByStatus.forEach((status, stocks) -> {
            long count = stocks.size();
            long totalQuantity = stocks.stream().mapToLong(Stock::getQuantity).sum();
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0;

            result.put(status, new StockController.StockStatusStatistics(status, status.getDescription(), count, totalQuantity, percentage));
        });

        return result;
    }

    public StockController.StockChangeStatistics getStockChanges(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<StockTransaction> transactions = transactionRepository.findByTransactionDateBetween(startDateTime, endDateTime);

        long totalInbound = 0, totalOutbound = 0, totalAdjustment = 0;
        for (StockTransaction t : transactions) {
            switch (t.getTransactionType()) {
                case INBOUND -> totalInbound += t.getQuantity();
                case OUTBOUND -> totalOutbound += t.getQuantity();
                case ADJUSTMENT -> totalAdjustment += t.getQuantity();
            }
        }

        Map<String, Long> dailyChanges = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDate().toLocalDate().toString(),
                        Collectors.summingLong(t -> t.getTransactionType() == TransactionType.INBOUND ? t.getQuantity() : (t.getTransactionType() == TransactionType.OUTBOUND ? -t.getQuantity() : 0L))));

        return new StockController.StockChangeStatistics(startDate, endDate, totalInbound, totalOutbound, totalAdjustment, totalInbound - totalOutbound, dailyChanges);
    }

    public StockController.StockValuation calculateStockValuation() {
        List<Stock> allStocks = stockRepository.findAll();
        BigDecimal totalValue = BigDecimal.ZERO;
        long totalQuantity = 0L;
        Map<String, StockController.StockValuation.CategoryValuation> categoryValuations = new HashMap<>();

        for (Stock stock : allStocks) {
            totalQuantity += stock.getQuantity();
            if (stock.getPurchasePrice() != null) {
                BigDecimal stockValue = stock.calculateValue();
                totalValue = totalValue.add(stockValue);

                MedicineCategory categoryEnum = stock.getMedicine().getCategory();
                if (categoryEnum != null) {
                    String categoryName = categoryEnum.getDescription();
                    StockController.StockValuation.CategoryValuation catVal = categoryValuations.computeIfAbsent(categoryName, k ->
                            new StockController.StockValuation.CategoryValuation(k, 0.0, 0L, 0.0));

                    catVal.setValue(catVal.getValue() + stockValue.doubleValue());
                    catVal.setQuantity(catVal.getQuantity() + stock.getQuantity());
                }
            }
        }

        BigDecimal finalTotalValue = totalValue;
        categoryValuations.values().forEach(catVal -> {
            double percentage = finalTotalValue.doubleValue() > 0 ? (catVal.getValue() * 100 / finalTotalValue.doubleValue()) : 0;
            catVal.setPercentageOfTotal(percentage);
        });

        return new StockController.StockValuation(totalValue.doubleValue(), (long) allStocks.size(), totalQuantity, categoryValuations, new HashMap<>(), LocalDate.now());
    }

    // ===================================================================================
    // Private Helper 메서드
    // ===================================================================================

    private Specification<Stock> createSpecification(StockSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getMedicineId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("medicine").get("id"), request.getMedicineId()));
            }
            if (request.getMedicineName() != null && !request.getMedicineName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("medicine").get("name")), "%" + request.getMedicineName().toLowerCase() + "%"));
            }
            if (request.getLotNumber() != null && !request.getLotNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lotNumber")), "%" + request.getLotNumber().toLowerCase() + "%"));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + request.getLocation().toLowerCase() + "%"));
            }
            if (request.getExpiryDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expiryDate"), request.getExpiryDateFrom()));
            }
            if (request.getExpiryDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), request.getExpiryDateTo()));
            }
            if (Boolean.TRUE.equals(request.getIsExpired())) {
                predicates.add(criteriaBuilder.lessThan(root.get("expiryDate"), LocalDate.now()));
            }
            if (Boolean.TRUE.equals(request.getIsExpiringSoon())) {
                int days = request.getExpiryDaysThreshold() != null ? request.getExpiryDaysThreshold() : 30;
                predicates.add(criteriaBuilder.between(root.get("expiryDate"), LocalDate.now(), LocalDate.now().plusDays(days)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}