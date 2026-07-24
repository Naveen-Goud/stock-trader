package com.trading.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeExecutedEvent(
        String eventId,
        Long tradeId,
        Long userId,
        String symbol,
        String tradeType,
        Long quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        Instant executedAt
) {}
