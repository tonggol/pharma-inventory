package com.pharma.inventory.service;

import com.pharma.inventory.controller.StockTransactionController;
import com.pharma.inventory.dto.request.BatchTransactionRequest;
import com.pharma.inventory.dto.request.StockTransactionRequest;
import com.pharma.inventory.dto.request.TransactionSearchRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 재고 트랜잭션 Service
 * 입출고 처리 및 이력 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransactionService {
    
    private final StockTransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    /**
     * 트랜잭션 목록 조회 (페이징)
     */
    public Page<StockTransactionResponse> getTransactions(Pageable pageable) {
        log.debug("트랜잭션 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<StockTransaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(StockTransactionResponse::from);
    }

    /**
     * 트랜잭션 상세 조회
     */
    public StockTransactionResponse getTransaction(Long id) {
        log.debug("트랜잭션 상세 조회 - ID: {}", id);
        
        StockTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("트랜잭션을 찾을 수 없습니다. ID: " + id));
        
        return StockTransactionResponse.from(transaction);
    }

    /**
     * 트랜잭션 검색
     */
    public Page<StockTransactionResponse> searchTransactions(TransactionSearchRequest request, Pageable pageable) {
        log.debug("트랜잭션 검색 - 조건: {}", request);
        
        Specification<StockTransaction> spec = createSpecification(request);
        Page<StockTransaction> transactions = transactionRepository.findAll(spec, pageable);
        return transactions.map(StockTransactionResponse::from);
    }

    /**
     * 트랜잭션 처리 (통합 메서드)
     */
    @Transactional
    public StockTransactionResponse processTransaction(StockTransactionRequest request) {
        log.info("트랜잭션 처리 - 타입: {}, 의약품ID: {}, 수량: {}", 
                request.getTransactionType(), request.getMedicineId(), request.getQuantity());
        
        // 의약품 확인
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + request.getMedicineId()));
        
        Stock stock = null;
        Integer beforeQuantity = 0;
        Integer afterQuantity = 0;
        
        // 트랜잭션 타입별 처리
        switch (request.getTransactionType()) {
            case INBOUND:
                // 입고: 새 재고 생성 또는 기존 재고 증가
                if (request.getStockId() != null) {
                    stock = stockRepository.findById(request.getStockId()).orElseThrow();
                    beforeQuantity = stock.getQuantity();
                    stock.setQuantity(beforeQuantity + request.getQuantity());
                    afterQuantity = stock.getQuantity();
                    stockRepository.save(stock);
                }
                break;
                
            case OUTBOUND:
                // 출고: FEFO 방식으로 재고 차감
                processOutbound(medicine, request.getQuantity(), request.getDepartment(), request.getRequesterName());
                break;
                
            case ADJUSTMENT:
                // 조정: 재고 수량 직접 조정
                if (request.getStockId() != null) {
                    stock = stockRepository.findById(request.getStockId()).orElseThrow();
                    beforeQuantity = stock.getQuantity();
                    afterQuantity = request.getQuantity();
                    stock.setQuantity(afterQuantity);
                    stockRepository.save(stock);
                }
                break;
                
            case RETURN:
                // 반품: 재고 증가
                if (request.getStockId() != null) {
                    stock = stockRepository.findById(request.getStockId()).orElseThrow();
                    beforeQuantity = stock.getQuantity();
                    stock.setQuantity(beforeQuantity + request.getQuantity());
                    afterQuantity = stock.getQuantity();
                    stockRepository.save(stock);
                }
                break;
                
            case DISPOSAL:
                // 폐기: 재고 차감 및 상태 변경
                if (request.getStockId() != null) {
                    stock = stockRepository.findById(request.getStockId()).orElseThrow();
                    beforeQuantity = stock.getQuantity();
                    stock.setQuantity(Math.max(0, beforeQuantity - request.getQuantity()));
                    afterQuantity = stock.getQuantity();
                    if (afterQuantity == 0) {
                        stock.setStatus(Stock.StockStatus.EXPIRED);
                    }
                    stockRepository.save(stock);
                }
                break;
                
            case TRANSFER:
                // 이동: 위치 변경
                if (request.getStockId() != null) {
                    stock = stockRepository.findById(request.getStockId()).orElseThrow();
                    // 위치 정보는 별도 처리 필요
                }
                break;
        }
        
        // 트랜잭션 기록
        StockTransaction transaction = StockTransaction.builder()
                .medicine(medicine)
                .stock(stock)
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now())
                .referenceNumber(request.getReferenceNumber())
                .department(request.getDepartment())
                .requesterName(request.getRequesterName())
                .approverName(request.getApproverName())
                .reason(request.getReason())
                .remarks(request.getRemarks())
                .createdBy(getCurrentUser())
                .build();
        
        StockTransaction saved = transactionRepository.save(transaction);
        return StockTransactionResponse.from(saved);
    }

    /**
     * 일괄 트랜잭션 처리
     */
    @Transactional
    public StockTransactionController.BatchTransactionResult processBatchTransactions(BatchTransactionRequest request) {
        log.info("일괄 트랜잭션 처리 시작 - 개수: {}", request.getTransactions().size());
        
        List<Long> successIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (BatchTransactionRequest.TransactionItem item : request.getTransactions()) {
            try {
                StockTransactionRequest txRequest = StockTransactionRequest.builder()
                        .medicineId(item.getMedicineId())
                        .stockId(item.getStockId())
                        .transactionType(item.getTransactionType())
                        .quantity(item.getQuantity())
                        .transactionDate(request.getTransactionDate())
                        .department(request.getDepartment())
                        .requesterName(request.getRequesterName())
                        .approverName(request.getApproverName())
                        .reason(item.getReason())
                        .referenceNumber(item.getReferenceNumber())
                        .remarks(item.getRemarks() != null ? item.getRemarks() : request.getRemarks())
                        .build();
                
                StockTransactionResponse response = processTransaction(txRequest);
                successIds.add(response.getId());
                
            } catch (Exception e) {
                errors.add(String.format("의약품 ID %d: %s", item.getMedicineId(), e.getMessage()));
            }
        }
        
        return StockTransactionController.BatchTransactionResult.builder()
                .totalCount(request.getTransactions().size())
                .successCount(successIds.size())
                .failCount(errors.size())
                .successIds(successIds)
                .errors(errors)
                .build();
    }

    /**
     * 트랜잭션 취소
     */
    @Transactional
    public StockTransactionResponse cancelTransaction(Long id, String reason) {
        log.info("트랜잭션 취소 - ID: {}, 사유: {}", id, reason);
        
        StockTransaction original = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("트랜잭션을 찾을 수 없습니다. ID: " + id));
        
        // 재고 원복
        if (original.getStock() != null) {
            Stock stock = original.getStock();
            
            switch (original.getTransactionType()) {
                case INBOUND:
                case RETURN:
                    // 입고/반품 취소: 재고 차감
                    stock.setQuantity(stock.getQuantity() - original.getQuantity());
                    break;
                case OUTBOUND:
                case DISPOSAL:
                    // 출고/폐기 취소: 재고 증가
                    stock.setQuantity(stock.getQuantity() + original.getQuantity());
                    break;
                case ADJUSTMENT:
                    // 조정 취소: 이전 수량으로 복원
                    stock.setQuantity(original.getBeforeQuantity());
                    break;
            }
            stockRepository.save(stock);
        }
        
        // 취소 트랜잭션 생성
        StockTransaction cancelTx = StockTransaction.builder()
                .medicine(original.getMedicine())
                .stock(original.getStock())
                .transactionType(StockTransaction.TransactionType.ADJUSTMENT)
                .quantity(original.getQuantity())
                .beforeQuantity(original.getAfterQuantity())
                .afterQuantity(original.getBeforeQuantity())
                .transactionDate(LocalDateTime.now())
                .referenceNumber("CANCEL-" + original.getId())
                .reason(StockTransaction.TransactionReason.OTHER)
                .remarks("트랜잭션 #" + original.getId() + " 취소: " + reason)
                .createdBy(getCurrentUser())
                .build();
        
        StockTransaction saved = transactionRepository.save(cancelTx);
        return StockTransactionResponse.from(saved);
    }

    /**
     * 의약품별 트랜잭션 조회
     */
    public List<StockTransactionResponse> getTransactionsByMedicine(Long medicineId, int days) {
        log.debug("의약품별 트랜잭션 조회 - 의약품ID: {}, 기간: {}일", medicineId, days);
        
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + medicineId));
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<StockTransaction> transactions = transactionRepository
                .findByMedicine(medicine).stream()
                .filter(tx -> tx.getTransactionDate().isAfter(startDate) && tx.getTransactionDate().isBefore(endDate))
                .sorted(Comparator.comparing(StockTransaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
        
        return transactions.stream()
                .map(StockTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 로트번호별 트랜잭션 조회
     */
    public List<StockTransactionResponse> getTransactionsByLotNumber(String lotNumber) {
        log.debug("로트번호별 트랜잭션 조회 - 로트번호: {}", lotNumber);
        
        Stock stock = stockRepository.findByLotNumber(lotNumber)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다. 로트번호: " + lotNumber));
        
        List<StockTransaction> transactions = transactionRepository.findByStock(stock);
        
        return transactions.stream()
                .map(StockTransactionResponse::from)
                .sorted(Comparator.comparing(StockTransactionResponse::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 일별 트랜잭션 통계
     */
    public StockTransactionController.DailyTransactionStatistics getDailyStatistics(LocalDate date) {
        log.debug("일별 트랜잭션 통계 조회 - 날짜: {}", date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<StockTransaction> transactions = transactionRepository
                .findByTransactionDateBetween(startOfDay, endOfDay);
        
        Map<StockTransaction.TransactionType, Long> countByType = transactions.stream()
                .collect(Collectors.groupingBy(StockTransaction::getTransactionType, Collectors.counting()));
        
        Map<StockTransaction.TransactionType, Long> quantityByType = transactions.stream()
                .collect(Collectors.groupingBy(StockTransaction::getTransactionType,
                        Collectors.summingLong(StockTransaction::getQuantity)));
        
        // Top 5 의약품
        Map<String, Long> topMedicines = transactions.stream()
                .collect(Collectors.groupingBy(tx -> tx.getMedicine().getName(),
                        Collectors.summingLong(StockTransaction::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        
        // 부서별 활동
        Map<String, Long> departmentActivity = transactions.stream()
                .filter(tx -> tx.getDepartment() != null)
                .collect(Collectors.groupingBy(StockTransaction::getDepartment, Collectors.counting()));
        
        return StockTransactionController.DailyTransactionStatistics.builder()
                .date(date)
                .totalTransactions((long) transactions.size())
                .inboundCount(countByType.getOrDefault(StockTransaction.TransactionType.INBOUND, 0L))
                .outboundCount(countByType.getOrDefault(StockTransaction.TransactionType.OUTBOUND, 0L))
                .adjustmentCount(countByType.getOrDefault(StockTransaction.TransactionType.ADJUSTMENT, 0L))
                .returnCount(countByType.getOrDefault(StockTransaction.TransactionType.RETURN, 0L))
                .disposalCount(countByType.getOrDefault(StockTransaction.TransactionType.DISPOSAL, 0L))
                .transferCount(countByType.getOrDefault(StockTransaction.TransactionType.TRANSFER, 0L))
                .totalInboundQuantity(quantityByType.getOrDefault(StockTransaction.TransactionType.INBOUND, 0L))
                .totalOutboundQuantity(quantityByType.getOrDefault(StockTransaction.TransactionType.OUTBOUND, 0L))
                .topMedicines(topMedicines)
                .departmentActivity(departmentActivity)
                .build();
    }

    /**
     * 기간별 트랜잭션 요약
     */
    public StockTransactionController.TransactionSummary getTransactionSummary(LocalDate startDate, LocalDate endDate) {
        log.debug("트랜잭션 요약 조회 - 기간: {} ~ {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<StockTransaction> transactions = transactionRepository
                .findByTransactionDateBetween(startDateTime, endDateTime);
        
        Map<StockTransaction.TransactionType, Long> transactionsByType = transactions.stream()
                .collect(Collectors.groupingBy(StockTransaction::getTransactionType, Collectors.counting()));
        
        Map<StockTransaction.TransactionReason, Long> transactionsByReason = transactions.stream()
                .filter(tx -> tx.getReason() != null)
                .collect(Collectors.groupingBy(StockTransaction::getReason, Collectors.counting()));
        
        Long totalQuantityIn = transactions.stream()
                .filter(tx -> tx.getTransactionType() == StockTransaction.TransactionType.INBOUND ||
                             tx.getTransactionType() == StockTransaction.TransactionType.RETURN)
                .mapToLong(StockTransaction::getQuantity)
                .sum();
        
        Long totalQuantityOut = transactions.stream()
                .filter(tx -> tx.getTransactionType() == StockTransaction.TransactionType.OUTBOUND ||
                             tx.getTransactionType() == StockTransaction.TransactionType.DISPOSAL)
                .mapToLong(StockTransaction::getQuantity)
                .sum();
        
        // Top Items 계산
        List<StockTransactionController.TransactionSummary.TopItem> topInboundItems = calculateTopItems(
                transactions, StockTransaction.TransactionType.INBOUND);
        
        List<StockTransactionController.TransactionSummary.TopItem> topOutboundItems = calculateTopItems(
                transactions, StockTransaction.TransactionType.OUTBOUND);
        
        return StockTransactionController.TransactionSummary.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalTransactions((long) transactions.size())
                .transactionsByType(transactionsByType)
                .transactionsByReason(transactionsByReason)
                .totalQuantityIn(totalQuantityIn)
                .totalQuantityOut(totalQuantityOut)
                .netQuantityChange(totalQuantityIn - totalQuantityOut)
                .topInboundItems(topInboundItems)
                .topOutboundItems(topOutboundItems)
                .build();
    }

    /**
     * 부서별 통계
     */
    public Map<String, StockTransactionController.DepartmentStatistics> getDepartmentStatistics(
            LocalDate startDate, LocalDate endDate) {
        
        log.debug("부서별 통계 조회 - 기간: {} ~ {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Object[]> departmentStats = transactionRepository
                .getDepartmentStatistics(startDateTime, endDateTime);
        
        Map<String, StockTransactionController.DepartmentStatistics> result = new HashMap<>();
        
        for (Object[] stat : departmentStats) {
            String department = (String) stat[0];
            Long totalQuantity = ((Number) stat[1]).longValue();
            
            // 부서별 상세 트랜잭션 조회
            List<StockTransaction> deptTransactions = transactionRepository
                    .findByDepartment(department).stream()
                    .filter(tx -> tx.getTransactionDate().isAfter(startDateTime) && 
                                 tx.getTransactionDate().isBefore(endDateTime))
                    .collect(Collectors.toList());
            
            Map<String, Long> medicineUsage = deptTransactions.stream()
                    .collect(Collectors.groupingBy(
                            tx -> tx.getMedicine().getName(),
                            Collectors.summingLong(StockTransaction::getQuantity)
                    ));
            
            List<String> frequentRequesters = deptTransactions.stream()
                    .filter(tx -> tx.getRequesterName() != null)
                    .collect(Collectors.groupingBy(StockTransaction::getRequesterName, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            result.put(department, StockTransactionController.DepartmentStatistics.builder()
                    .department(department)
                    .transactionCount((long) deptTransactions.size())
                    .totalQuantity(totalQuantity)
                    .medicineUsage(medicineUsage)
                    .frequentRequesters(frequentRequesters)
                    .build());
        }
        
        return result;
    }

    /**
     * 최근 트랜잭션 조회
     */
    public List<StockTransactionResponse> getRecentTransactions(int limit) {
        log.debug("최근 트랜잭션 조회 - 개수: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<StockTransaction> transactions = transactionRepository.findAllByOrderByTransactionDateDesc(pageable);
        
        return transactions.getContent().stream()
                .map(StockTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 일/월별 통계 조회
     */
    public List<StockTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 트랜잭션 조회 - {} ~ {}", startDate, endDate);
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    /**
     * 부서별 통계 조회 (기존 메서드 유지)
     */
    public Map<String, Long> getDepartmentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("부서별 출고 통계 조회 - {} ~ {}", startDate, endDate);
        
        List<Object[]> stats = transactionRepository.getDepartmentStatistics(startDate, endDate);
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            String department = (String) stat[0];
            Long quantity = ((Number) stat[1]).longValue();
            result.put(department, quantity);
        }
        
        return result;
    }

    /**
     * 최근 트랜잭션 조회 (기존 메서드 유지)
     */
    public List<StockTransaction> getRecentTransactions(Pageable pageable) {
        log.debug("최근 트랜잭션 조회");
        return transactionRepository.findAllByOrderByTransactionDateDesc(pageable).getContent();
    }

    /**
     * 출고 처리 (FEFO)
     */
    private void processOutbound(Medicine medicine, Integer quantity, String department, String requester) {
        stockService.processOutboundStock(medicine.getId(), quantity, department, requester);
    }

    /**
     * 현재 로그인 사용자 조회
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                return userRepository.findByUsername(authentication.getName()).orElse(null);
            }
        } catch (Exception e) {
            log.debug("현재 사용자 조회 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Top Items 계산
     */
    private List<StockTransactionController.TransactionSummary.TopItem> calculateTopItems(
            List<StockTransaction> transactions, StockTransaction.TransactionType type) {
        
        Map<Medicine, Long> quantityByMedicine = transactions.stream()
                .filter(tx -> tx.getTransactionType() == type)
                .collect(Collectors.groupingBy(StockTransaction::getMedicine,
                        Collectors.summingLong(StockTransaction::getQuantity)));
        
        Map<Medicine, Long> countByMedicine = transactions.stream()
                .filter(tx -> tx.getTransactionType() == type)
                .collect(Collectors.groupingBy(StockTransaction::getMedicine, Collectors.counting()));
        
        return quantityByMedicine.entrySet().stream()
                .sorted(Map.Entry.<Medicine, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Medicine med = entry.getKey();
                    return StockTransactionController.TransactionSummary.TopItem.builder()
                            .medicineId(med.getId())
                            .medicineName(med.getName())
                            .medicineCode(med.getCode())
                            .quantity(entry.getValue())
                            .transactionCount(countByMedicine.get(med))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 검색 조건 Specification 생성
     */
    private Specification<StockTransaction> createSpecification(TransactionSearchRequest request) {
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
            
            if (request.getStockId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("stock").get("id"), request.getStockId()));
            }
            
            if (request.getTransactionType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType"), request.getTransactionType()));
            }
            
            if (request.getReason() != null) {
                predicates.add(criteriaBuilder.equal(root.get("reason"), request.getReason()));
            }
            
            if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("department")),
                        "%" + request.getDepartment().toLowerCase() + "%"
                ));
            }
            
            if (request.getRequesterName() != null && !request.getRequesterName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("requesterName")),
                        "%" + request.getRequesterName().toLowerCase() + "%"
                ));
            }
            
            if (request.getTransactionDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("transactionDate"), request.getTransactionDateFrom()
                ));
            }
            
            if (request.getTransactionDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("transactionDate"), request.getTransactionDateTo()
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}