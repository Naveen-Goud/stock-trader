package com.trading.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WatchlistItemResponse(
        String symbol, BigDecimal currentPrice, LocalDateTime addedAt
) {}
