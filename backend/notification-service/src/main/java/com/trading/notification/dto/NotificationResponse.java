package com.trading.notification.dto;

import com.trading.notification.entity.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id, String type, String message, String relatedSymbol, boolean isRead, LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getMessage(), n.getRelatedSymbol(), n.isRead(), n.getCreatedAt());
    }
}
