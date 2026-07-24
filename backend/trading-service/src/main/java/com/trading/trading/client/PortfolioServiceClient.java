package com.trading.trading.client;

import com.trading.trading.dto.HoldingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "portfolio-service")
public interface PortfolioServiceClient {
    @GetMapping("/api/portfolio/internal/{userId}/holdings/{symbol}")
    HoldingResponse getHolding(@PathVariable("userId") Long userId, @PathVariable("symbol") String symbol);
}
