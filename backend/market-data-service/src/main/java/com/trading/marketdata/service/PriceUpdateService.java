package com.trading.marketdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.StockPriceUpdatedEvent;
import com.trading.marketdata.cache.StockPriceWriteThroughCache;
import com.trading.marketdata.entity.PriceHistory;
import com.trading.marketdata.entity.Stock;
import com.trading.marketdata.repository.PriceHistoryRepository;
import com.trading.marketdata.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StockPriceWriteThroughCache priceCache;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_STOCK_PRICE_UPDATED = "stock-price-updated";

    @Transactional
    public void applyPriceUpdate(Stock stock, BigDecimal newPrice) {
        BigDecimal oldPrice = stock.getCurrentPrice();
        BigDecimal changeAmount = newPrice.subtract(oldPrice);
        BigDecimal changePercent = oldPrice.compareTo(BigDecimal.ZERO) > 0
                ? changeAmount.divide(oldPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        stock.setCurrentPrice(newPrice);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        PriceHistory history = PriceHistory.builder()
                .stockSymbol(stock.getSymbol()).price(newPrice)
                .changeAmount(changeAmount).changePercent(changePercent).build();
        priceHistoryRepository.save(history);

        priceCache.updatePrice(stock.getSymbol(), newPrice);
        publishPriceEvent(stock.getSymbol(), oldPrice, newPrice, changePercent);
    }

    private void publishPriceEvent(String symbol, BigDecimal oldPrice, BigDecimal newPrice, BigDecimal changePercent) {
        StockPriceUpdatedEvent event = new StockPriceUpdatedEvent(
                UUID.randomUUID().toString(), symbol, oldPrice, newPrice, changePercent, Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_STOCK_PRICE_UPDATED, symbol, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish price event for {}: {}", symbol, ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Serialization error publishing price event for {}: {}", symbol, e.getMessage());
        }
    }
}
