package com.trading.marketdata.dto;

import java.util.List;

public record StockPageResponse(
        List<StockResponse> stocks, long totalElements, int totalPages, int page, int size
) {}
