package com.trading.portfolio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.PortfolioUpdatedEvent;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.portfolio.cache.EventDedupCache;
import com.trading.portfolio.cache.PortfolioSnapshotCache;
import com.trading.portfolio.cache.StockPriceCache;
import com.trading.portfolio.entity.Holding;
import com.trading.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingsUpdateService {

    private final HoldingRepository holdingRepository;
    private final EventDedupCache eventDedupCache;
    private final PortfolioSnapshotCache portfolioSnapshotCache;
    private final StockPriceCache stockPriceCache;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void applyTrade(TradeExecutedEvent event) {
        if (!eventDedupCache.tryMarkProcessed(event.eventId())) {
            log.info("Duplicate trade-executed event {} ignored (already processed)", event.eventId());
            return;
        }

        Holding holding = holdingRepository.findByUserIdAndStockSymbol(event.userId(), event.symbol())
                .orElseGet(() -> Holding.builder()
                        .userId(event.userId()).stockSymbol(event.symbol())
                        .quantity(0L).avgBuyPrice(BigDecimal.ZERO).build());

        if ("BUY".equals(event.tradeType())) {
            applyBuy(holding, event);
        } else {
            applySell(holding, event);
        }

        holding.setUpdatedAt(LocalDateTime.now());
        holdingRepository.save(holding);

        portfolioSnapshotCache.evict(event.userId());
        publishPortfolioUpdated(holding, event);
    }

    private void applyBuy(Holding holding, TradeExecutedEvent event) {
        long newQuantity = holding.getQuantity() + event.quantity();

        BigDecimal oldTotalCost = holding.getAvgBuyPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal newTradeCost = event.price().multiply(BigDecimal.valueOf(event.quantity()));
        BigDecimal newAvgPrice = oldTotalCost.add(newTradeCost)
                .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAvgBuyPrice(newAvgPrice);
    }

    private void applySell(Holding holding, TradeExecutedEvent event) {
        long newQuantity = holding.getQuantity() - event.quantity();
        if (newQuantity < 0) {
            log.error("Negative holding quantity detected for user={}, symbol={}. Clamping to 0.",
                    event.userId(), event.symbol());
            newQuantity = 0;
        }
        holding.setQuantity(newQuantity);
    }

    private void publishPortfolioUpdated(Holding holding, TradeExecutedEvent event) {
        BigDecimal currentPrice = stockPriceCache.getPrice(holding.getStockSymbol());
        BigDecimal portfolioValue = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));

        PortfolioUpdatedEvent updatedEvent = new PortfolioUpdatedEvent(
                UUID.randomUUID().toString(), holding.getUserId(), holding.getStockSymbol(),
                holding.getQuantity(), holding.getAvgBuyPrice(), portfolioValue, Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(updatedEvent);
            kafkaTemplate.send("portfolio-updated", String.valueOf(holding.getUserId()), payload);
        } catch (Exception e) {
            log.error("Failed to publish portfolio-updated event for user {}: {}", holding.getUserId(), e.getMessage());
        }
    }

    public List<Holding> getHoldings(Long userId) {
        return holdingRepository.findByUserId(userId);
    }
}
