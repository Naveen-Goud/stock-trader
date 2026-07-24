package com.trading.portfolio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.portfolio.cache.EventDedupCache;
import com.trading.portfolio.cache.PortfolioSnapshotCache;
import com.trading.portfolio.cache.StockPriceCache;
import com.trading.portfolio.entity.Holding;
import com.trading.portfolio.repository.HoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingsUpdateServiceTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private EventDedupCache eventDedupCache;
    @Mock private PortfolioSnapshotCache portfolioSnapshotCache;
    @Mock private StockPriceCache stockPriceCache;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    private HoldingsUpdateService service;

    @BeforeEach
    void setUp() {
        service = new HoldingsUpdateService(
                holdingRepository, eventDedupCache, portfolioSnapshotCache,
                stockPriceCache, kafkaTemplate, objectMapper);
        when(eventDedupCache.tryMarkProcessed(any())).thenReturn(true);
        when(stockPriceCache.getPrice(any())).thenReturn(new BigDecimal("200.00"));
    }

    @Test
    void applyTrade_buyNewHolding_createsHoldingWithCorrectAvgPrice() {
        TradeExecutedEvent event = new TradeExecutedEvent(
                UUID.randomUUID().toString(), 1L, 100L, "AAPL", "BUY", 10L,
                new BigDecimal("190.00"), new BigDecimal("1900.00"), Instant.now());

        when(holdingRepository.findByUserIdAndStockSymbol(100L, "AAPL")).thenReturn(Optional.empty());

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        service.applyTrade(event);

        verify(holdingRepository).save(captor.capture());
        Holding saved = captor.getValue();

        assertThat(saved.getQuantity()).isEqualTo(10L);
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("190.0000");
        verify(portfolioSnapshotCache).evict(100L);
    }

    @Test
    void applyTrade_buyMoreOfExistingHolding_recalculatesWeightedAvgPrice() {
        TradeExecutedEvent event = new TradeExecutedEvent(
                UUID.randomUUID().toString(), 2L, 100L, "AAPL", "BUY", 10L,
                new BigDecimal("210.00"), new BigDecimal("2100.00"), Instant.now());

        Holding existing = Holding.builder()
                .id(1L).userId(100L).stockSymbol("AAPL")
                .quantity(10L).avgBuyPrice(new BigDecimal("190.0000")).build();
        when(holdingRepository.findByUserIdAndStockSymbol(100L, "AAPL")).thenReturn(Optional.of(existing));

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        service.applyTrade(event);

        verify(holdingRepository).save(captor.capture());
        Holding saved = captor.getValue();

        assertThat(saved.getQuantity()).isEqualTo(20L);
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("200.0000");
    }

    @Test
    void applyTrade_sellPartialHolding_decreasesQuantityKeepsAvgPrice() {
        TradeExecutedEvent event = new TradeExecutedEvent(
                UUID.randomUUID().toString(), 3L, 100L, "AAPL", "SELL", 4L,
                new BigDecimal("220.00"), new BigDecimal("880.00"), Instant.now());

        Holding existing = Holding.builder()
                .id(1L).userId(100L).stockSymbol("AAPL")
                .quantity(10L).avgBuyPrice(new BigDecimal("190.0000")).build();
        when(holdingRepository.findByUserIdAndStockSymbol(100L, "AAPL")).thenReturn(Optional.of(existing));

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        service.applyTrade(event);

        verify(holdingRepository).save(captor.capture());
        Holding saved = captor.getValue();

        assertThat(saved.getQuantity()).isEqualTo(6L);
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("190.0000");
    }

    @Test
    void applyTrade_duplicateEvent_skipsProcessing() {
        TradeExecutedEvent event = new TradeExecutedEvent(
                "dup-event-id", 5L, 100L, "AAPL", "BUY", 10L,
                new BigDecimal("190.00"), new BigDecimal("1900.00"), Instant.now());

        when(eventDedupCache.tryMarkProcessed("dup-event-id")).thenReturn(false);

        service.applyTrade(event);

        verify(holdingRepository, never()).save(any());
        verify(portfolioSnapshotCache, never()).evict(any());
    }

    @Test
    void applyTrade_sellExceedingQuantity_clampsToZeroDefensively() {
        TradeExecutedEvent event = new TradeExecutedEvent(
                UUID.randomUUID().toString(), 6L, 100L, "AAPL", "SELL", 15L,
                new BigDecimal("220.00"), new BigDecimal("3300.00"), Instant.now());

        Holding existing = Holding.builder()
                .id(1L).userId(100L).stockSymbol("AAPL")
                .quantity(10L).avgBuyPrice(new BigDecimal("190.0000")).build();
        when(holdingRepository.findByUserIdAndStockSymbol(100L, "AAPL")).thenReturn(Optional.of(existing));

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        service.applyTrade(event);

        verify(holdingRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(0L);
    }
}
