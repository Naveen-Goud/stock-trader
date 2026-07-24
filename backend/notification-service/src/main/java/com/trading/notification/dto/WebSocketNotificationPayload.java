package com.trading.notification.dto;

public record WebSocketNotificationPayload(
        Long notificationId, String type, String message, String relatedSymbol, String timestamp
) {}
