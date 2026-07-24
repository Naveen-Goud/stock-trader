package com.trading.trading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.trading.client.PortfolioServiceClient;
import com.trading.trading.client.UserServiceClient;
import com.trading.trading.dto.*;
import com.trading.trading.entity.Trade;
import com.trading.trading.exception.*;
import com.trading.trading.repository.OutboxEventRepository;
import com.trading.trading.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock private TradeRepository tradeRepository;
    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private StockPriceLookupService priceLookupService;
    @Mock private UserServiceClient userServiceClient;
    @Mock private PortfolioServiceClient portfolioServiceClient;

    private ObjectMapper objectMapper = new ObjectMapper();
    private TradingService tradingService;

    @BeforeEach
    void setUp() {
        tradingService = new TradingService(
                tradeRepository, outboxEventRepository, priceLookupService,
                userServiceClient, portfolioServiceClient, objectMapper);
    }

    @Test
    void buyStock_sufficientBalance_executesTrade() {
        TradeRequest request = new TradeRequest("AAPL", 10L);

        when(tradeRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(priceLookupService.getCurrentPrice("AAPL")).thenReturn(new StockPriceResponse("AAPL", new BigDecimal("190.00")));
        when(userServiceClient.getWalletBalance(1L)).thenReturn(new WalletBalanceResponse(1L, new BigDecimal("5000.00")));

        Trade saved = Trade.builder()
                .id(100L).userId(1L).stockSymbol("AAPL").tradeType(Trade.TradeType.BUY)
                .quantity(10L).price(new BigDecimal("190.00")).totalAmount(new BigDecimal("1900.00"))
                .status(Trade.TradeStatus.EXECUTED).build();
        when(tradeRepository.save(any(Trade.class))).thenReturn(saved);

        TradeResponse response = tradingService.buyStock(1L, request, "idem-1");

        assertThat(response.tradeType()).isEqualTo("BUY");
        assertThat(response.totalAmount()).isEqualByComparingTo("1900.00");
        verify(outboxEventRepository).save(any());
    }

    @Test
    void buyStock_insufficientBalance_throwsException() {
        TradeRequest request = new TradeRequest("AAPL", 100L);

        when(tradeRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(priceLookupService.getCurrentPrice("AAPL")).thenReturn(new StockPriceResponse("AAPL", new BigDecimal("190.00")));
        when(userServiceClient.getWalletBalance(1L)).thenReturn(new WalletBalanceResponse(1L, new BigDecimal("500.00")));

        assertThatThrownBy(() -> tradingService.buyStock(1L, request, "idem-2"))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(tradeRepository, never()).save(any());
    }

    @Test
    void sellStock_sufficientHoldings_executesTrade() {
        TradeRequest request = new TradeRequest("AAPL", 5L);

        when(tradeRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(priceLookupService.getCurrentPrice("AAPL")).thenReturn(new StockPriceResponse("AAPL", new BigDecimal("195.00")));
        when(portfolioServiceClient.getHolding(1L, "AAPL")).thenReturn(new HoldingResponse(1L, "AAPL", 10L, new BigDecimal("180.00")));

        Trade saved = Trade.builder()
                .id(101L).userId(1L).stockSymbol("AAPL").tradeType(Trade.TradeType.SELL)
                .quantity(5L).price(new BigDecimal("195.00")).totalAmount(new BigDecimal("975.00"))
                .status(Trade.TradeStatus.EXECUTED).build();
        when(tradeRepository.save(any(Trade.class))).thenReturn(saved);

        TradeResponse response = tradingService.sellStock(1L, request, "idem-3");

        assertThat(response.tradeType()).isEqualTo("SELL");
    }

    @Test
    void sellStock_insufficientHoldings_throwsException() {
        TradeRequest request = new TradeRequest("AAPL", 20L);

        when(tradeRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(priceLookupService.getCurrentPrice("AAPL")).thenReturn(new StockPriceResponse("AAPL", new BigDecimal("195.00")));
        when(portfolioServiceClient.getHolding(1L, "AAPL")).thenReturn(new HoldingResponse(1L, "AAPL", 5L, new BigDecimal("180.00")));

        assertThatThrownBy(() -> tradingService.sellStock(1L, request, "idem-4"))
                .isInstanceOf(InsufficientHoldingsException.class);
    }

    @Test
    void buyStock_duplicateIdempotencyKey_throwsDuplicateTradeException() {
        TradeRequest request = new TradeRequest("AAPL", 10L);
        Trade existing = Trade.builder().id(99L).idempotencyKey("dup-key").build();

        when(tradeRepository.findByIdempotencyKey("dup-key")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> tradingService.buyStock(1L, request, "dup-key"))
                .isInstanceOf(DuplicateTradeException.class);

        verify(priceLookupService, never()).getCurrentPrice(any());
    }
}
