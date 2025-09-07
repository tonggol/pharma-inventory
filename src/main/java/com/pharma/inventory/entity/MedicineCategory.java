package com.pharma.inventory.entity;

import java.util.Arrays;

/**
 * 의약품 카테고리
 */
public enum MedicineCategory {
    // From DataInitializer
    PAINKILLER("해열진통제"),
    ANTIBIOTIC("항생제"),
    DIGESTIVE("소화기계약물"),
    IV_SOLUTION("수액제"),
    HORMONE("호르몬제"),
    VITAMIN("비타민제"),
    TOPICAL("외용제"),
    ANTIHISTAMINE("항히스타민제"),

    // Original
    PRESCRIPTION("전문의약품"),
    OTC("일반의약품"),
    NARCOTIC("마약류"),
    PSYCHOTROPIC("향정신성의약품"),
    BIOLOGICAL("생물학적제제"),
    VACCINE("백신"),
    BLOOD("혈액제제"),
    HERBAL("한약제제"),
    RADIOPHARMACEUTICAL("방사성의약품"),
    OTHER("기타");

    private final String description;

    MedicineCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static MedicineCategory fromDescription(String description) {
        return Arrays.stream(MedicineCategory.values())
                .filter(category -> category.description.equals(description))
                .findFirst()
                .orElse(OTHER);
    }
}