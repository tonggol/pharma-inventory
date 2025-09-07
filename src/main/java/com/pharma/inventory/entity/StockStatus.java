package com.pharma.inventory.entity;

/**
 * 재고 상태
 */
public enum StockStatus {
    AVAILABLE("사용가능"),      // 정상적으로 사용 가능한 재고
    RESERVED("예약됨"),         // 출고 예정으로 예약된 재고
    QUARANTINE("격리"),        // 품질 검사 중이거나 문제가 있어 격리된 재고
    EXPIRED("만료"),           // 유효기간이 만료된 재고
    DAMAGED("파손"),           // 파손되어 사용 불가능한 재고
    DISPOSED("폐기");          // 폐기 처리된 재고
    
    private final String description;
    
    StockStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isUsable() {
        return this == AVAILABLE || this == RESERVED;
    }
}
