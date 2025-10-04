package com.pharma.inventory.entity;

import java.util.Arrays;

/**
 * 医薬品カテゴリ
 */
public enum MedicineCategory {
    PAINKILLER("解熱鎮痛剤", "fa-pills"),
    ANTIBIOTIC("抗生物質", "fa-capsules"),
    DIGESTIVE("消化器系薬物", "fa-tablets"),
    IV_SOLUTION("輸液剤", "fa-syringe"),
    HORMONE("ホルモン剤", "fa-dna"),
    VITAMIN("ビタミン剤", "fa-leaf"),
    TOPICAL("外用剤", "fa-band-aid"),
    ANTIHISTAMINE("抗ヒスタミン剤", "fa-allergies"),
    PRESCRIPTION("処方箋医薬品", "fa-prescription"),
    OTC("一般用医薬品", "fa-pills"),
    NARCOTIC("麻薬類", "fa-exclamation-triangle"),
    PSYCHOTROPIC("向精神薬", "fa-brain"),
    BIOLOGICAL("生物学的製剤", "fa-dna"),
    VACCINE("ワクチン", "fa-syringe"),
    BLOOD("血液製剤", "fa-tint"),
    HERBAL("漢方製剤", "fa-leaf"),
    RADIOPHARMACEUTICAL("放射性医薬品", "fa-atom"),
    OTHER("その他", "fa-tag");

    private final String description;
    private final String icon;

    MedicineCategory(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public static MedicineCategory fromDescription(String description) {
        return Arrays.stream(MedicineCategory.values())
                .filter(category -> category.description.equals(description))
                .findFirst()
                .orElse(OTHER);
    }
}