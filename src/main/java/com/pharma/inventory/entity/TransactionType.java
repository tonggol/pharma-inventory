package com.pharma.inventory.entity;

/**
 * 재고 거래 유형
 */
public enum TransactionType {
    INBOUND("입고"),
    OUTBOUND("출고"),
    ADJUSTMENT("조정"),
    RETURN("반품"),
    DISPOSAL("폐기"),
    TRANSFER("이동");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isIncreasing() {
        return this == INBOUND || this == RETURN;
    }
    
    public boolean isDecreasing() {
        return this == OUTBOUND || this == DISPOSAL;
    }
}
