package com.trading.marketdata.mapper;

import com.trading.marketdata.dto.PriceHistoryPoint;
import com.trading.marketdata.dto.StockDetailResponse;
import com.trading.marketdata.dto.StockResponse;
import com.trading.marketdata.entity.PriceHistory;
import com.trading.marketdata.entity.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class StockMapper {

    public StockResponse toResponse(Stock stock) {
        BigDecimal change = stock.getCurrentPrice().subtract(stock.getPreviousClose());
        BigDecimal changePct = stock.getPreviousClose().compareTo(BigDecimal.ZERO) > 0
                ? change.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new StockResponse(
                stock.getSymbol(), stock.getCompanyName(), stock.getSector(),
                stock.getCurrentPrice(), stock.getPreviousClose(),
                change, changePct, stock.getMarketCap(), stock.getLastUpdated());
    }

    public StockDetailResponse toDetailResponse(Stock stock, List<PriceHistory> history) {
        BigDecimal change = stock.getCurrentPrice().subtract(stock.getPreviousClose());
        BigDecimal changePct = stock.getPreviousClose().compareTo(BigDecimal.ZERO) > 0
                ? change.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<PriceHistoryPoint> historyPoints = history.stream()
                .map(h -> new PriceHistoryPoint(h.getPrice(), h.getChangePercent(), h.getRecordedAt()))
                .toList();

        return new StockDetailResponse(
                stock.getSymbol(), stock.getCompanyName(), stock.getSector(),
                stock.getCurrentPrice(), stock.getPreviousClose(),
                change, changePct, stock.getMarketCap(), stock.getLastUpdated(), historyPoints);
    }
}
