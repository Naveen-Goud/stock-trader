package com.trading.portfolio.service;

import com.trading.portfolio.cache.PortfolioSnapshotCache;
import com.trading.portfolio.cache.StockPriceCache;
import com.trading.portfolio.dto.HoldingResponse;
import com.trading.portfolio.dto.PortfolioSummaryResponse;
import com.trading.portfolio.entity.Holding;
import com.trading.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioValuationService {

    private final HoldingRepository holdingRepository;
    private final StockPriceCache stockPriceCache;
    private final PortfolioSnapshotCache snapshotCache;

    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        PortfolioSummaryResponse cached = snapshotCache.get(userId);
        if (cached != null) {
            return cached;
        }

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        List<HoldingResponse> holdingResponses = new java.util.ArrayList<>();

        for (Holding h : holdings) {
            if (h.getQuantity() == 0) continue;

            BigDecimal currentPrice = stockPriceCache.getPrice(h.getStockSymbol());
            BigDecimal invested = h.getAvgBuyPrice().multiply(BigDecimal.valueOf(h.getQuantity()));
            BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(h.getQuantity()));
            BigDecimal pl = currentValue.subtract(invested);
            BigDecimal plPercent = invested.compareTo(BigDecimal.ZERO) > 0
                    ? pl.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            holdingResponses.add(new HoldingResponse(
                    h.getStockSymbol(), h.getQuantity(), h.getAvgBuyPrice(),
                    currentPrice, currentValue, pl, plPercent));

            totalInvested = totalInvested.add(invested);
            totalCurrentValue = totalCurrentValue.add(currentValue);
        }

        BigDecimal totalPL = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPLPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalPL.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        PortfolioSummaryResponse summary = new PortfolioSummaryResponse(
                totalInvested, totalCurrentValue, totalPL, totalPLPercent, holdingResponses);

        snapshotCache.put(userId, summary);
        return summary;
    }
}
