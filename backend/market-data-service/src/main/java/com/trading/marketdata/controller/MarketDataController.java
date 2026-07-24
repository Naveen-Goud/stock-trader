package com.trading.marketdata.controller;

import com.trading.marketdata.dto.*;
import com.trading.marketdata.service.StockMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final StockMarketService stockMarketService;

    @GetMapping("/stocks")
    public ResponseEntity<StockPageResponse> getStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(stockMarketService.getStocks(page, size, sector, sortBy));
    }

    @GetMapping("/stocks/search")
    public ResponseEntity<StockPageResponse> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(stockMarketService.searchStocks(query, page, size));
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockDetailResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockMarketService.getStockBySymbol(symbol));
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors() {
        return ResponseEntity.ok(stockMarketService.getSectors());
    }
}
