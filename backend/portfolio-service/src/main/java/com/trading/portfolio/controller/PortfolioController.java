package com.trading.portfolio.controller;

import com.trading.portfolio.dto.PortfolioSummaryResponse;
import com.trading.portfolio.service.PortfolioValuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioValuationService portfolioValuationService;

    @GetMapping
    public ResponseEntity<PortfolioSummaryResponse> getPortfolio(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(portfolioValuationService.getPortfolioSummary(userId));
    }
}
