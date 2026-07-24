package com.trading.marketdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.marketdata.cache.StockPriceWriteThroughCache;
import com.trading.marketdata.entity.PriceHistory;
import com.trading.marketdata.entity.Stock;
import com.trading.marketdata.repository.PriceHistoryRepository;
import com.trading.marketdata.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceUpdateServiceTest {

    @Mock private StockRepository stockRepository;
    @Mock private PriceHistoryRepository priceHistoryRepository;
    @Mock private StockPriceWriteThroughCache priceCache;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private PriceUpdateService service;

    @BeforeEach
    void setUp() {
        service = new PriceUpdateService(
                stockRepository, priceHistoryRepository, priceCache, kafkaTemplate,
                new ObjectMapper().findAndRegisterModules());
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void applyPriceUpdate_persistsStockAndHistory() {
        Stock stock = Stock.builder()
                .symbol("AAPL").currentPrice(new BigDecimal("190.00"))
                .previousClose(new BigDecimal("188.00"))
                .drift(new BigDecimal("0.00005")).volatility(new BigDecimal("0.018")).build();

        service.applyPriceUpdate(stock, new BigDecimal("191.50"));

        verify(stockRepository).save(argThat(s -> s.getCurrentPrice().compareTo(new BigDecimal("191.50")) == 0));

        ArgumentCaptor<PriceHistory> histCaptor = ArgumentCaptor.forClass(PriceHistory.class);
        verify(priceHistoryRepository).save(histCaptor.capture());
        assertThat(histCaptor.getValue().getPrice()).isEqualByComparingTo("191.50");
    }

    @Test
    void applyPriceUpdate_writesToRedisBeforeKafka() {
        Stock stock = Stock.builder()
                .symbol("MSFT").currentPrice(new BigDecimal("420.00"))
                .previousClose(new BigDecimal("418.00"))
                .drift(new BigDecimal("0.00005")).volatility(new BigDecimal("0.016")).build();

        service.applyPriceUpdate(stock, new BigDecimal("422.00"));

        var order = inOrder(priceCache, kafkaTemplate);
        order.verify(priceCache).updatePrice("MSFT", new BigDecimal("422.00"));
        order.verify(kafkaTemplate).send(eq("stock-price-updated"), eq("MSFT"), anyString());
    }
}
