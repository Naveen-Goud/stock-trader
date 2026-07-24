package com.trading.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record StockPriceUpdatedEvent(
        String eventId,
        String symbol,
        BigDecimal previousPrice,
        BigDecimal currentPrice,
        BigDecimal changePercent,
        Instant timestamp
) {}
