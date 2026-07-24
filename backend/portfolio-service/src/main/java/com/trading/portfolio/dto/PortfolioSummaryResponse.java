package com.trading.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
        BigDecimal totalInvested, BigDecimal currentValue,
        BigDecimal totalProfitLoss, BigDecimal totalProfitLossPercent,
        List<HoldingResponse> holdings
) {}
