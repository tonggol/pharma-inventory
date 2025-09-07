package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 알림 상태
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlerts {
    private Long criticalStockCount;  // 재고 매우 부족
    private Long lowStockCount;       // 재고 부족
    private Long expiredCount;        // 만료된 재고
    private Long expiringSoonCount;   // 30일 이내 만료
    private Long quarantineCount;     // 격리 중인 재고
}
