package com.pharma.inventory.entity;

/**
 * 在庫ステータス
 */
public enum StockStatus {
    AVAILABLE("利用可能", "fa-check-circle"),      // 正常に利用可能な在庫
    RESERVED("予約済み", "fa-clock"),         // 出庫予定で予約された在庫
    QUARANTINE("隔離", "fa-shield-alt"),        // 品質検査中または問題があり隔離された在庫
    EXPIRED("期限切れ", "fa-calendar-times"),           // 有効期限が切れた在庫
    DAMAGED("破損", "fa-heart-broken"),           // 破損して使用不可能な在庫
    DISPOSED("廃棄", "fa-trash");          // 廃棄処理された在庫
    
    private final String description;
    private final String icon;
    
    StockStatus(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }
    
    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
    
    public boolean isUsable() {
        return this == AVAILABLE || this == RESERVED;
    }
}
