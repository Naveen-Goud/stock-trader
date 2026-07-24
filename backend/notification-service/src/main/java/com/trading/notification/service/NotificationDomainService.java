package com.trading.notification.service;

import com.trading.notification.dto.WebSocketNotificationPayload;
import com.trading.notification.entity.Notification;
import com.trading.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDomainService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public Notification createAndBroadcast(Long userId, String username,
                                           Notification.NotificationType type,
                                           String message, String relatedSymbol) {
        Notification notification = Notification.builder()
                .userId(userId).type(type).message(message)
                .relatedSymbol(relatedSymbol).isRead(false).build();

        notification = notificationRepository.save(notification);
        broadcastToUser(username, notification);
        return notification;
    }

    private void broadcastToUser(String username, Notification notification) {
        WebSocketNotificationPayload payload = new WebSocketNotificationPayload(
                notification.getId(), notification.getType().name(), notification.getMessage(),
                notification.getRelatedSymbol(), notification.getCreatedAt().format(FORMATTER));

        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
        } catch (Exception ex) {
            log.error("WebSocket broadcast failed for user {}: {}", username, ex.getMessage());
        }
    }
}
