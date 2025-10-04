package com.pharma.inventory.entity;

/**
 * 在庫取引事由
 */
public enum TransactionReason {
    PURCHASE("購入"),
    SALES("販売"),
    PRESCRIPTION("処方"),
    INVENTORY_CHECK("棚卸"),
    EXPIRED("使用期限切れ"),
    DAMAGED("破損"),
    LOST("紛失"),
    SAMPLE("サンプル"),
    DONATION("寄付"),
    OTHER("その他");
    
    private final String description;
    
    TransactionReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
