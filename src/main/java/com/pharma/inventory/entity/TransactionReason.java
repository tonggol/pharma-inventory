package com.pharma.inventory.entity;

/**
 * 재고 거래 사유
 */
public enum TransactionReason {
    PURCHASE("구매"),
    SALES("판매"),
    PRESCRIPTION("처방"),
    INVENTORY_CHECK("재고실사"),
    EXPIRED("유효기간만료"),
    DAMAGED("파손"),
    LOST("분실"),
    SAMPLE("샘플"),
    DONATION("기부"),
    OTHER("기타");
    
    private final String description;
    
    TransactionReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
