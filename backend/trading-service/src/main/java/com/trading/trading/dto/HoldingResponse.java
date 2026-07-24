package com.trading.trading.dto;

import java.math.BigDecimal;

public record HoldingResponse(Long userId, String stockSymbol, Long quantity, BigDecimal avgBuyPrice) {}
