package com.trading.portfolio.controller;

import com.trading.portfolio.dto.HoldingInternalResponse;
import com.trading.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/portfolio/internal")
@RequiredArgsConstructor
public class InternalHoldingsController {

    private final HoldingRepository holdingRepository;

    /**
     * findByUserIdAndStockSymbol acquires a PESSIMISTIC_WRITE lock (SELECT
     * ... FOR UPDATE), which Hibernate requires an active transaction to
     * issue. Without @Transactional here, this call throws
     * InvalidDataAccessApiUsageException outside of a transaction -- which
     * surfaced as a bare 500 whenever trading-service checked holdings
     * before a SELL (BUY never hits this endpoint, so it looked fine).
     */
    @Transactional
    @GetMapping("/{userId}/holdings/{symbol}")
    public ResponseEntity<HoldingInternalResponse> getHolding(
            @PathVariable Long userId, @PathVariable String symbol) {

        return holdingRepository.findByUserIdAndStockSymbol(userId, symbol)
                .map(h -> ResponseEntity.ok(new HoldingInternalResponse(h.getUserId(), h.getStockSymbol(), h.getQuantity(), h.getAvgBuyPrice())))
                .orElseGet(() -> ResponseEntity.ok(new HoldingInternalResponse(userId, symbol, 0L, BigDecimal.ZERO)));
    }
}
