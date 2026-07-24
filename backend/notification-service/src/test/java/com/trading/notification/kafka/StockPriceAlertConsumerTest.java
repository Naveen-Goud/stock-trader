package com.trading.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.StockPriceUpdatedEvent;
import com.trading.notification.cache.NotificationDedupCache;
import com.trading.notification.cache.UserContextCache;
import com.trading.notification.entity.PriceAlert;
import com.trading.notification.repository.PriceAlertRepository;
import com.trading.notification.service.NotificationDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockPriceAlertConsumerTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private PriceAlertRepository priceAlertRepository;
    @Mock private NotificationDomainService notificationService;
    @Mock private NotificationDedupCache dedupCache;
    @Mock private UserContextCache userContextCache;

    @InjectMocks
    private StockPriceAlertConsumer consumer;

    @BeforeEach
    void setUp() {
        when(dedupCache.tryMarkProcessed(any())).thenReturn(true);
    }

    @Test
    void onStockPriceUpdated_aboveTarget_firesAlert() throws Exception {
        StockPriceUpdatedEvent event = new StockPriceUpdatedEvent(
                UUID.randomUUID().toString(), "AAPL", new BigDecimal("195.00"),
                new BigDecimal("201.00"), new BigDecimal("3.08"), Instant.now());

        PriceAlert alert = PriceAlert.builder()
                .id(1L).userId(100L).stockSymbol("AAPL")
                .alertType(PriceAlert.AlertType.ABOVE).targetPrice(new BigDecimal("200.00")).isActive(true).build();

        when(objectMapper.readValue(any(String.class), eq(StockPriceUpdatedEvent.class))).thenReturn(event);
        when(priceAlertRepository.findByStockSymbolAndIsActiveTrue("AAPL")).thenReturn(List.of(alert));
        when(userContextCache.getUsername(100L)).thenReturn("naveen");

        consumer.onStockPriceUpdated("{\"symbol\":\"AAPL\"}");

        verify(priceAlertRepository).save(argThat(a -> !a.isActive()));
        verify(notificationService).createAndBroadcast(eq(100L), eq("naveen"), any(), contains("AAPL"), eq("AAPL"));
    }

    @Test
    void onStockPriceUpdated_priceNotYetAtTarget_doesNotFire() throws Exception {
        StockPriceUpdatedEvent event = new StockPriceUpdatedEvent(
                UUID.randomUUID().toString(), "AAPL", new BigDecimal("195.00"),
                new BigDecimal("198.00"), new BigDecimal("1.54"), Instant.now());

        PriceAlert alert = PriceAlert.builder()
                .id(1L).userId(100L).stockSymbol("AAPL")
                .alertType(PriceAlert.AlertType.ABOVE).targetPrice(new BigDecimal("200.00")).isActive(true).build();

        when(objectMapper.readValue(any(String.class), eq(StockPriceUpdatedEvent.class))).thenReturn(event);
        when(priceAlertRepository.findByStockSymbolAndIsActiveTrue("AAPL")).thenReturn(List.of(alert));

        consumer.onStockPriceUpdated("{\"symbol\":\"AAPL\"}");

        verify(notificationService, never()).createAndBroadcast(any(), any(), any(), any(), any());
    }

    @Test
    void onStockPriceUpdated_duplicateEvent_skipsProcessing() throws Exception {
        when(dedupCache.tryMarkProcessed(any())).thenReturn(false);
        StockPriceUpdatedEvent event = new StockPriceUpdatedEvent(
                "dup-id", "AAPL", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, Instant.now());

        when(objectMapper.readValue(any(String.class), eq(StockPriceUpdatedEvent.class))).thenReturn(event);

        consumer.onStockPriceUpdated("{\"symbol\":\"AAPL\"}");

        verify(priceAlertRepository, never()).findByStockSymbolAndIsActiveTrue(any());
    }
}
