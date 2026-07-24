package com.trading.notification.dto;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> notifications, long totalElements, int totalPages, long unreadCount
) {}
