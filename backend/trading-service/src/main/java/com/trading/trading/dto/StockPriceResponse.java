package com.trading.trading.dto;

import java.math.BigDecimal;

public record StockPriceResponse(String symbol, BigDecimal currentPrice) {}
