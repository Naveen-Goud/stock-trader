package com.trading.trading.controller;

import com.trading.trading.dto.TradeRequest;
import com.trading.trading.dto.TradeResponse;
import com.trading.trading.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trading")
@RequiredArgsConstructor
public class TradingController {

    private final TradingService tradingService;

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradingService.buyStock(userId, request, idempotencyKey));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradingService.sellStock(userId, request, idempotencyKey));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<TradeResponse>> history(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "executedAt") Pageable pageable) {
        return ResponseEntity.ok(tradingService.getTradeHistory(userId, pageable));
    }
}
