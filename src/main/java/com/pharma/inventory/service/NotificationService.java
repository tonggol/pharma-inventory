package com.pharma.inventory.service;

import com.pharma.inventory.dto.response.NotificationResponse;
import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 알림 Service (간소화 버전)
 * 메모리 기반 알림 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;
    
    // 메모리 기반 알림 저장소 (실제로는 DB나 Redis 사용 권장)
    private final Map<String, List<Notification>> userNotifications = new ConcurrentHashMap<>();
    private Long notificationIdCounter = 1L;

    /**
     * 사용자 알림 조회
     */
    public List<NotificationResponse> getUserNotifications(String username, boolean unreadOnly) {
        log.debug("알림 조회 - 사용자: {}, 읽지 않은 것만: {}", username, unreadOnly);
        
        List<Notification> notifications = userNotifications.getOrDefault(username, new ArrayList<>());
        
        return notifications.stream()
                .filter(n -> !unreadOnly || !n.isRead())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        log.debug("알림 읽음 처리 - ID: {}, 사용자: {}", notificationId, username);
        
        List<Notification> notifications = userNotifications.get(username);
        if (notifications != null) {
            notifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .ifPresent(n -> {
                        n.setRead(true);
                        n.setReadAt(LocalDateTime.now());
                    });
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(String username) {
        log.debug("모든 알림 읽음 처리 - 사용자: {}", username);
        
        List<Notification> notifications = userNotifications.get(username);
        if (notifications == null) {
            return 0;
        }
        
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
                count++;
            }
        }
        
        return count;
    }

    /**
     * 알림 생성
     */
    @Transactional
    public void createNotification(String username, NotificationResponse.NotificationType type, 
                                  NotificationResponse.NotificationPriority priority,
                                  String title, String message, 
                                  String referenceType, Long referenceId) {
        log.debug("알림 생성 - 사용자: {}, 타입: {}", username, type);
        
        Notification notification = Notification.builder()
                .id(notificationIdCounter++)
                .type(type)
                .priority(priority)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .username(username)
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();
        
        userNotifications.computeIfAbsent(username, k -> new ArrayList<>()).add(notification);
    }

    /**
     * 시스템 알림 생성 (모든 사용자)
     */
    @Transactional
    public void createSystemNotification(String title, String message) {
        log.info("시스템 알림 생성");
        
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        for (User user : activeUsers) {
            createNotification(user.getUsername(), 
                    NotificationResponse.NotificationType.SYSTEM_ALERT,
                    NotificationResponse.NotificationPriority.MEDIUM,
                    title, message, null, null);
        }
    }

    /**
     * 재고 부족 알림
     */
    @Transactional
    public void createLowStockNotification(Long medicineId, String medicineName, 
                                          int currentStock, int minimumStock) {
        String title = "재고 부족 경고";
        String message = String.format("%s의 재고가 부족합니다. 현재: %d, 최소: %d", 
                medicineName, currentStock, minimumStock);
        
        NotificationResponse.NotificationPriority priority = 
                currentStock == 0 ? NotificationResponse.NotificationPriority.CRITICAL 
                                  : NotificationResponse.NotificationPriority.HIGH;
        
        // 관리자와 약사에게만 알림
        List<User> users = userRepository.findByRole(User.UserRole.ADMIN);
        users.addAll(userRepository.findByRole(User.UserRole.PHARMACIST));
        
        for (User user : users) {
            createNotification(user.getUsername(), 
                    NotificationResponse.NotificationType.STOCK_LOW,
                    priority,
                    title, message, 
                    "MEDICINE", medicineId);
        }
    }

    /**
     * 만료 임박 알림
     */
    @Transactional
    public void createExpiryNotification(Long stockId, String medicineName, 
                                        String lotNumber, LocalDateTime expiryDate) {
        String title = "의약품 만료 임박";
        String message = String.format("%s (로트: %s)가 %s에 만료됩니다", 
                medicineName, lotNumber, expiryDate.toLocalDate());
        
        boolean isExpired = expiryDate.isBefore(LocalDateTime.now());
        
        NotificationResponse.NotificationType type = isExpired ? 
                NotificationResponse.NotificationType.STOCK_EXPIRED : 
                NotificationResponse.NotificationType.STOCK_EXPIRING;
        
        NotificationResponse.NotificationPriority priority = isExpired ? 
                NotificationResponse.NotificationPriority.CRITICAL : 
                NotificationResponse.NotificationPriority.HIGH;
        
        // 관리자와 약사에게만 알림
        List<User> users = userRepository.findByRole(User.UserRole.ADMIN);
        users.addAll(userRepository.findByRole(User.UserRole.PHARMACIST));
        
        for (User user : users) {
            createNotification(user.getUsername(), type, priority,
                    title, message, "STOCK", stockId);
        }
    }

    /**
     * 트랜잭션 승인 대기 알림
     */
    @Transactional
    public void createTransactionPendingNotification(Long transactionId, String requester, 
                                                    String transactionType) {
        String title = "트랜잭션 승인 대기";
        String message = String.format("%s님이 요청한 %s 트랜잭션이 승인 대기 중입니다", 
                requester, transactionType);
        
        // 관리자에게만 알림
        List<User> admins = userRepository.findByRole(User.UserRole.ADMIN);
        
        for (User admin : admins) {
            createNotification(admin.getUsername(), 
                    NotificationResponse.NotificationType.TRANSACTION_PENDING,
                    NotificationResponse.NotificationPriority.MEDIUM,
                    title, message, 
                    "TRANSACTION", transactionId);
        }
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId, String username) {
        log.debug("알림 삭제 - ID: {}, 사용자: {}", notificationId, username);
        
        List<Notification> notifications = userNotifications.get(username);
        if (notifications != null) {
            notifications.removeIf(n -> n.getId().equals(notificationId));
        }
    }

    /**
     * 읽은 알림 삭제
     */
    @Transactional
    public int deleteReadNotifications(String username) {
        log.debug("읽은 알림 삭제 - 사용자: {}", username);
        
        List<Notification> notifications = userNotifications.get(username);
        if (notifications == null) {
            return 0;
        }
        
        int countBefore = notifications.size();
        notifications.removeIf(Notification::isRead);
        int countAfter = notifications.size();
        
        return countBefore - countAfter;
    }

    /**
     * 알림 개수 조회
     */
    public int getUnreadCount(String username) {
        List<Notification> notifications = userNotifications.get(username);
        if (notifications == null) {
            return 0;
        }
        
        return (int) notifications.stream()
                .filter(n -> !n.isRead())
                .count();
    }

    /**
     * Notification을 Response로 변환
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    /**
     * 내부 Notification 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class Notification {
        private Long id;
        private String username;
        private NotificationResponse.NotificationType type;
        private NotificationResponse.NotificationPriority priority;
        private String title;
        private String message;
        private String referenceType;
        private Long referenceId;
        private LocalDateTime createdAt;
        private boolean read;
        private LocalDateTime readAt;
    }
}
