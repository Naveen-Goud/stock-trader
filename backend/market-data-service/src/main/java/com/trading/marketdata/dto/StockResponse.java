package com.trading.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        String symbol, String companyName, String sector,
        BigDecimal currentPrice, BigDecimal previousClose,
        BigDecimal changeAmount, BigDecimal changePercent,
        BigDecimal marketCap, LocalDateTime lastUpdated
) {}
