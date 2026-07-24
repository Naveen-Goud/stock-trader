package com.trading.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PortfolioUpdatedEvent(
        String eventId,
        Long userId,
        String symbol,
        Long newQuantity,
        BigDecimal newAvgBuyPrice,
        BigDecimal portfolioValue,
        Instant timestamp
) {}
