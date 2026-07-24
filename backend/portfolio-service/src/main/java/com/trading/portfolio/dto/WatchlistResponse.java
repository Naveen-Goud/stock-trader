package com.trading.portfolio.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WatchlistResponse(
        Long id, String name, LocalDateTime createdAt, List<WatchlistItemResponse> items
) {}
