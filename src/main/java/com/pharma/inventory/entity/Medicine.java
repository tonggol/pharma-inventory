package com.pharma.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicines", indexes = {
        @Index(name = "idx_medicine_code", columnList = "medicine_code", unique = true),
        @Index(name = "idx_medicine_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 protected 생성자
public class Medicine extends BaseEntity { // BaseEntity로 Auditing 필드 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_code", nullable = false, unique = true, length = 50)
    private String medicineCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String manufacturer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MedicineCategory category;

    @Column(length = 20)
    private String unit; // 정, 캡슐, ml 등

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 0;
    
    // === 추가 필드들 (이전에 없던 것들) ===
    @Column(name = "name_en", length = 200)
    private String nameEn;  // 영문명
    
    @Column(name = "code", length = 50)
    private String code;  // 의약품 코드 별칭 (medicineCode와 동일, 호환성을 위해)
    
    @Column(name = "storage_condition", length = 100)
    private String storageCondition;  // 보관 조건
    
    @Column(name = "is_prescription_required")
    private Boolean isPrescriptionRequired = false;  // 처방전 필요 여부

    @Column(name = "min_stock_quantity")
    private Integer minStockQuantity = 0;  // Repository 쿼리와 호환을 위해 추가
    
    @Column(name = "is_active")
    private Boolean isActive = true;  // 활성 상태

    // 양방향 관계 - LAZY 로딩
    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Stock> stocks = new ArrayList<>();

    // === 생성자를 통한 객체 생성 (Builder 대신) ===
    public Medicine(String medicineCode, String name, String manufacturer,
                    MedicineCategory category, Integer minStockLevel) {
        validateMedicineCode(medicineCode);
        validateName(name);
        validateMinStockLevel(minStockLevel);

        this.medicineCode = medicineCode;
        this.code = medicineCode;  // 호환성을 위해 둘 다 설정
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
        this.minStockLevel = minStockLevel;
        this.minStockQuantity = minStockLevel;  // 호환성
        this.isActive = true;
        this.isPrescriptionRequired = false;
    }
    
    // === 전체 필드 생성자 (DataInitializer용) ===
    public Medicine(String code, String name, String nameEn, String description, 
                   String manufacturer, String unit, MedicineCategory category,
                   String storageCondition, Integer minStockQuantity, 
                   Boolean isPrescriptionRequired) {
        this.medicineCode = code;
        this.code = code;
        this.name = name;
        this.nameEn = nameEn;
        this.description = description;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.category = category;
        this.storageCondition = storageCondition;
        this.minStockLevel = minStockQuantity;
        this.minStockQuantity = minStockQuantity;
        this.isPrescriptionRequired = isPrescriptionRequired != null ? isPrescriptionRequired : false;
        this.isActive = true;
    }

    // === 비즈니스 메소드 (Setter 대신 명시적 메소드) ===
    public void updateMedicineInfo(String name, String manufacturer, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (manufacturer != null) {
            this.manufacturer = manufacturer;
        }
        if (description != null) {
            this.description = description;
        }
    }
    
    // 전체 정보 업데이트 메서드
    public void updateFullInfo(String name, String nameEn, String description,
                               String manufacturer, String unit, MedicineCategory category,
                               String storageCondition, Integer minStockQuantity,
                               Boolean isPrescriptionRequired, Boolean isActive) {
        if (name != null && !name.trim().isEmpty()) this.name = name;
        if (nameEn != null) this.nameEn = nameEn;
        if (description != null) this.description = description;
        if (manufacturer != null) this.manufacturer = manufacturer;
        if (unit != null) this.unit = unit;
        if (category != null) this.category = category;
        if (storageCondition != null) this.storageCondition = storageCondition;
        if (minStockQuantity != null) {
            this.minStockLevel = minStockQuantity;
            this.minStockQuantity = minStockQuantity;
        }
        if (isPrescriptionRequired != null) this.isPrescriptionRequired = isPrescriptionRequired;
        if (isActive != null) this.isActive = isActive;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }

    public void updatePrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        this.price = price;
    }

    public void updateMinStockLevel(Integer minStockLevel) {
        validateMinStockLevel(minStockLevel);
        this.minStockLevel = minStockLevel;
    }

    // === Validation 메소드 ===
    private void validateMedicineCode(String medicineCode) {
        if (medicineCode == null || medicineCode.trim().isEmpty()) {
            throw new IllegalArgumentException("의약품 코드는 필수입니다");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("의약품명은 필수입니다");
        }
    }

    private void validateMinStockLevel(Integer level) {
        if (level == null || level < 0) {
            throw new IllegalArgumentException("최소 재고 수준은 0 이상이어야 합니다");
        }
    }

    // === 추가 getter 메서드 (호환성) ===
    public String getCode() { return this.code != null ? this.code : this.medicineCode; }
    public Integer getMinimumStock() { return this.minStockLevel; }
    public String getNameEn() { return this.nameEn; }
    public String getStorageCondition() { return this.storageCondition; }
    public Boolean getIsPrescriptionRequired() { return this.isPrescriptionRequired; }
    public Integer getMinStockQuantity() {
        return this.minStockQuantity;
    }
    
    // === toString (연관 엔티티 제외) ===
    @Override
    public String toString() {
        return String.format("Medicine[id=%d, code='%s', name='%s']",
                id, medicineCode, name);
    }
}