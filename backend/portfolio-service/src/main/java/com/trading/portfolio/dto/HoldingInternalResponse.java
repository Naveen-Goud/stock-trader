package com.trading.portfolio.dto;

import java.math.BigDecimal;

public record HoldingInternalResponse(Long userId, String stockSymbol, Long quantity, BigDecimal avgBuyPrice) {}
