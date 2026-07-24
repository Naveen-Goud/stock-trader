package com.trading.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        String symbol, Long quantity, BigDecimal avgBuyPrice, BigDecimal currentPrice,
        BigDecimal currentValue, BigDecimal profitLoss, BigDecimal profitLossPercent
) {}
