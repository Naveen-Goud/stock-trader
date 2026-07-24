package com.trading.notification.repository;

import com.trading.notification.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByStockSymbolAndIsActiveTrue(String stockSymbol);
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);
}
