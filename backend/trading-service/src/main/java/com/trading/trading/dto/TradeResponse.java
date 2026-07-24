package com.trading.trading.dto;

import com.trading.trading.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        Long id, String symbol, String tradeType, Long quantity,
        BigDecimal price, BigDecimal totalAmount, String status, LocalDateTime executedAt
) {
    public static TradeResponse fromEntity(Trade trade) {
        return new TradeResponse(
                trade.getId(), trade.getStockSymbol(), trade.getTradeType().name(),
                trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(),
                trade.getStatus().name(), trade.getExecutedAt());
    }
}
