package com.pharma.inventory.service;

import com.pharma.inventory.controller.MedicineController;
import com.pharma.inventory.dto.request.MedicineCreateRequest;
import com.pharma.inventory.dto.request.MedicineSearchRequest;
import com.pharma.inventory.dto.request.MedicineUpdateRequest;
import com.pharma.inventory.dto.response.MedicineResponse;
import com.pharma.inventory.entity.Medicine;
import com.pharma.inventory.entity.MedicineCategory;
import com.pharma.inventory.entity.Stock;
import com.pharma.inventory.repository.MedicineRepository;
import com.pharma.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 의약품 Service - 의약품 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final StockRepository stockRepository;

    /**
     * 의약품 목록 조회 (페이징)
     */
    public Page<MedicineResponse> getMedicines(Pageable pageable) {
        log.debug("의약품 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Medicine> medicines = medicineRepository.findAll(pageable);
        return medicines.map(this::convertToResponseWithStock);
    }

    /**
     * 의약품 검색
     */
    public Page<MedicineResponse> searchMedicines(MedicineSearchRequest request, Pageable pageable) {
        log.debug("의약품 검색 - 조건: {}", request);
        
        Specification<Medicine> spec = createSpecification(request);
        Page<Medicine> medicines = medicineRepository.findAll(spec, pageable);
        return medicines.map(this::convertToResponseWithStock);
    }

    /**
     * 의약품 상세 조회
     */
    public MedicineResponse getMedicine(Long id) {
        log.debug("의약품 상세 조회 - ID: {}", id);
        
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + id));
        
        return convertToResponseWithStock(medicine);
    }

    /**
     * 의약품 코드로 조회
     */
    public MedicineResponse getMedicineByCode(String code) {
        log.debug("의약품 조회 - 코드: {}", code);
        
        Medicine medicine = medicineRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. 코드: " + code));
        
        return convertToResponseWithStock(medicine);
    }

    /**
     * 의약품 등록
     */
    @Transactional
    public MedicineResponse createMedicine(MedicineCreateRequest request) {
        log.info("의약품 등록 - 코드: {}, 이름: {}", request.getCode(), request.getName());
        
        if (medicineRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 존재하는 의약품 코드입니다: " + request.getCode());
        }
        
        Medicine medicine = new Medicine(
                request.getCode(),
                request.getName(),
                request.getNameEn(),
                request.getDescription(),
                request.getManufacturer(),
                request.getUnit(),
                request.getCategory(),
                request.getStorageCondition(),
                request.getMinStockQuantity(),
                request.getIsPrescriptionRequired()
        );

        if (request.getIsActive() != null && !request.getIsActive()) {
            medicine.deactivate();
        }

        Medicine saved = medicineRepository.save(medicine);
        return convertToResponseWithStock(saved);
    }

    /**
     * 의약품 수정 (PUT)
     */
    @Transactional
    public MedicineResponse updateMedicine(Long id, MedicineUpdateRequest request) {
        log.info("의약품 수정 - ID: {}", id);
        
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + id));
        
        medicine.updateFullInfo(
                request.getName(),
                request.getNameEn(),
                request.getDescription(),
                request.getManufacturer(),
                request.getUnit(),
                request.getCategory(),
                request.getStorageCondition(),
                request.getMinStockQuantity(),
                request.getIsPrescriptionRequired(),
                request.getIsActive()
        );
        
        Medicine updated = medicineRepository.save(medicine);
        return convertToResponseWithStock(updated);
    }

    /**
     * 의약품 부분 수정 (PATCH)
     */
    @Transactional
    public MedicineResponse patchMedicine(Long id, Map<String, Object> updates) {
        log.info("의약품 부분 수정 - ID: {}, 필드: {}", id, updates.keySet());

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + id));

        // updateFullInfo 메소드는 null이 아닌 값만 업데이트하므로 부분 수정에 활용 가능
        medicine.updateFullInfo(
                (String) updates.get("name"),
                (String) updates.get("nameEn"),
                (String) updates.get("description"),
                (String) updates.get("manufacturer"),
                (String) updates.get("unit"),
                updates.containsKey("category") ? MedicineCategory.valueOf((String) updates.get("category")) : null,
                (String) updates.get("storageCondition"),
                (Integer) updates.get("minStockQuantity"),
                (Boolean) updates.get("isPrescriptionRequired"),
                (Boolean) updates.get("isActive")
        );

        Medicine updated = medicineRepository.save(medicine);
        return convertToResponseWithStock(updated);
    }

    /**
     * 의약품 삭제 (비활성화)
     */
    @Transactional
    public void deleteMedicine(Long id) {
        log.info("의약품 비활성화 - ID: {}", id);
        
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + id));
        
        medicine.deactivate();
        medicineRepository.save(medicine);
    }

    /**
     * 재고 부족 의약품 목록 조회
     */
    public List<MedicineResponse> getLowStockMedicines() {
        log.debug("재고 부족 의약품 조회");
        
        List<Medicine> medicines = medicineRepository.findLowStockMedicines();
        
        return medicines.stream()
                .map(this::convertToResponseWithStock)
                .collect(Collectors.toList());
    }

    /**
     * 의약품 재고 요약 정보 조회
     */
    public MedicineController.MedicineStockSummary getStockSummary(Long medicineId) {
        log.debug("의약품 재고 요약 조회 - ID: {}", medicineId);
        
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. ID: " + medicineId));
        
        List<Stock> stocks = stockRepository.findByMedicineId(medicineId);
        
        int totalQuantity = 0;
        int availableQuantity = 0;
        int reservedQuantity = 0;
        int expiredQuantity = 0;
        int expiringQuantity = 0;
        double totalValue = 0.0;
        
        for (Stock stock : stocks) {
            totalQuantity += stock.getQuantity();
            
            if (stock.getStatus() != null) {
                switch (stock.getStatus()) {
                    case AVAILABLE:
                        availableQuantity += stock.getQuantity();
                        break;
                    case RESERVED:
                        reservedQuantity += stock.getQuantity();
                        break;
                    case EXPIRED:
                        expiredQuantity += stock.getQuantity();
                        break;
                }
            }
            
            if (stock.isExpiringSoon(30)) {
                expiringQuantity += stock.getQuantity();
            }
            
            if (stock.getPurchasePrice() != null) {
                totalValue += stock.getQuantity() * stock.getPurchasePrice().doubleValue();
            }
        }
        
        String stockStatus = determineStockStatus(totalQuantity, medicine.getMinStockQuantity());
        
        return MedicineController.MedicineStockSummary.builder()
                .medicineId(medicineId)
                .medicineName(medicine.getName())
                .medicineCode(medicine.getCode())
                .totalQuantity(totalQuantity)
                .availableQuantity(availableQuantity)
                .reservedQuantity(reservedQuantity)
                .expiredQuantity(expiredQuantity)
                .expiringQuantity(expiringQuantity)
                .minStockQuantity(medicine.getMinStockQuantity())
                .stockStatus(stockStatus)
                .distinctLots(stocks.size())
                .totalValue(totalValue)
                .build();
    }

    /**
     * Excel 파일로 의약품 일괄 등록
     */
    @Transactional
    public MedicineController.BulkUploadResult bulkCreateFromExcel(MultipartFile file) {
        log.info("의약품 일괄 등록 시작 - 파일명: {}", file.getOriginalFilename());
        
        // TODO: Excel 파일 파싱 및 처리 로직 구현
        return MedicineController.BulkUploadResult.builder()
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .errors(new ArrayList<>())
                .createdIds(new ArrayList<>())
                .build();
    }

    /**
     * Medicine Entity를 Response DTO로 변환 (재고 정보 포함)
     */
    private MedicineResponse convertToResponseWithStock(Medicine medicine) {
        MedicineResponse response = MedicineResponse.from(medicine);
        
        Integer currentStock = stockRepository.getTotalQuantityByMedicine(medicine);
        response.setCurrentStock(currentStock != null ? currentStock : 0);
        return response;
    }

    /**
     * 검색 조건에 따른 Specification 생성
     */
    private Specification<Medicine> createSpecification(MedicineSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturer")), keyword)
                ));
            }
            
            if (request.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), request.getCategory()));
            }
            
            if (request.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), request.getIsActive()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 재고 상태 결정
     */
    private String determineStockStatus(int currentStock, int minStock) {
        if (currentStock == 0) {
            return "OUT_OF_STOCK";
        } else if (minStock > 0 && currentStock < minStock * 0.5) {
            return "CRITICAL";
        } else if (minStock > 0 && currentStock < minStock) {
            return "LOW";
        } else {
            return "SUFFICIENT";
        }
    }
}