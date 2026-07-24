package com.trading.portfolio.controller;

import com.trading.portfolio.dto.*;
import com.trading.portfolio.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    public ResponseEntity<WatchlistResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateWatchlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.createWatchlist(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getAll(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(watchlistService.getWatchlists(userId));
    }

    @PostMapping("/{watchlistId}/items")
    public ResponseEntity<WatchlistResponse> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long watchlistId,
            @Valid @RequestBody AddWatchlistItemRequest request) {
        return ResponseEntity.ok(watchlistService.addItem(userId, watchlistId, request));
    }

    @DeleteMapping("/{watchlistId}/items/{symbol}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long watchlistId,
            @PathVariable String symbol) {
        watchlistService.removeItem(userId, watchlistId, symbol);
        return ResponseEntity.noContent().build();
    }
}
