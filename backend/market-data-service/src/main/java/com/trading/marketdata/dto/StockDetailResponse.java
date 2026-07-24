package com.trading.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StockDetailResponse(
        String symbol, String companyName, String sector,
        BigDecimal currentPrice, BigDecimal previousClose,
        BigDecimal changeAmount, BigDecimal changePercent,
        BigDecimal marketCap, LocalDateTime lastUpdated,
        List<PriceHistoryPoint> priceHistory
) {}
