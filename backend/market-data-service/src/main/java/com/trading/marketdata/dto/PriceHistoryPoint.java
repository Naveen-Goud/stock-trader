package com.trading.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryPoint(BigDecimal price, BigDecimal changePercent, LocalDateTime recordedAt) {}
