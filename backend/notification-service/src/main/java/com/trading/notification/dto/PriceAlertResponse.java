package com.trading.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceAlertResponse(
        Long id, String symbol, String alertType, BigDecimal targetPrice,
        boolean isActive, LocalDateTime triggeredAt, LocalDateTime createdAt
) {}
