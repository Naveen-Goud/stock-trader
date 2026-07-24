package com.trading.marketdata.repository;

import com.trading.marketdata.entity.PriceHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByStockSymbolOrderByRecordedAtDesc(String symbol, Pageable pageable);
    void deleteByRecordedAtBefore(LocalDateTime cutoff);
}
