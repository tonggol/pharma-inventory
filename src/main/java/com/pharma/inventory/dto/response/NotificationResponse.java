package com.pharma.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private String referenceType;  // MEDICINE, STOCK, TRANSACTION
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public enum NotificationType {
        STOCK_LOW("재고 부족"),
        STOCK_EXPIRED("재고 만료"),
        STOCK_EXPIRING("재고 만료 임박"),
        TRANSACTION_PENDING("트랜잭션 승인 대기"),
        SYSTEM_ALERT("시스템 알림");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum NotificationPriority {
        LOW("낮음"),
        MEDIUM("보통"),
        HIGH("높음"),
        CRITICAL("긴급");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}