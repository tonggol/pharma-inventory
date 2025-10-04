package com.pharma.inventory.entity;

/**
 * 在庫取引タイプ
 */
public enum TransactionType {
    INBOUND("入庫", "fa-arrow-down"),
    OUTBOUND("出庫", "fa-arrow-up"),
    ADJUSTMENT("調整", "fa-edit"),
    RETURN("返品", "fa-undo"),
    DISPOSAL("廃棄", "fa-trash"),
    TRANSFER("移動", "fa-exchange-alt");
    
    private final String description;
    private final String icon;
    
    TransactionType(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }
    
    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
    
    public boolean isIncreasing() {
        return this == INBOUND || this == RETURN;
    }
    
    public boolean isDecreasing() {
        return this == OUTBOUND || this == DISPOSAL;
    }
}
