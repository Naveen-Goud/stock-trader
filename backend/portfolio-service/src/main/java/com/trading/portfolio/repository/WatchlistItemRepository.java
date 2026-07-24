package com.trading.portfolio.repository;

import com.trading.portfolio.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    boolean existsByWatchlistIdAndStockSymbol(Long watchlistId, String stockSymbol);
}
