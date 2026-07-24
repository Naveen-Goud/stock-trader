package com.trading.trading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.trading.client.PortfolioServiceClient;
import com.trading.trading.client.UserServiceClient;
import com.trading.trading.dto.*;
import com.trading.trading.entity.OutboxEvent;
import com.trading.trading.entity.Trade;
import com.trading.trading.exception.*;
import com.trading.trading.repository.OutboxEventRepository;
import com.trading.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private final TradeRepository tradeRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final StockPriceLookupService priceLookupService;
    private final UserServiceClient userServiceClient;
    private final PortfolioServiceClient portfolioServiceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public TradeResponse buyStock(Long userId, TradeRequest request, String idempotencyKey) {
        checkIdempotency(idempotencyKey);

        StockPriceResponse price = priceLookupService.getCurrentPrice(request.symbol());
        BigDecimal totalAmount = price.currentPrice().multiply(BigDecimal.valueOf(request.quantity()));

        WalletBalanceResponse wallet = userServiceClient.getWalletBalance(userId);
        if (wallet.walletBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance: required %.2f, available %.2f",
                            totalAmount, wallet.walletBalance()));
        }

        Trade trade = Trade.builder()
                .userId(userId).stockSymbol(request.symbol()).tradeType(Trade.TradeType.BUY)
                .quantity(request.quantity()).price(price.currentPrice()).totalAmount(totalAmount)
                .status(Trade.TradeStatus.EXECUTED).idempotencyKey(idempotencyKey).build();

        trade = tradeRepository.save(trade);
        publishTradeExecutedEvent(trade);

        log.info("BUY executed: user={}, symbol={}, qty={}, price={}", userId, request.symbol(), request.quantity(), price.currentPrice());
        return TradeResponse.fromEntity(trade);
    }

    @Transactional
    public TradeResponse sellStock(Long userId, TradeRequest request, String idempotencyKey) {
        checkIdempotency(idempotencyKey);

        StockPriceResponse price = priceLookupService.getCurrentPrice(request.symbol());

        HoldingResponse holding = portfolioServiceClient.getHolding(userId, request.symbol());
        if (holding == null || holding.quantity() < request.quantity()) {
            long available = holding == null ? 0 : holding.quantity();
            throw new InsufficientHoldingsException(
                    String.format("Insufficient holdings: requested %d, available %d", request.quantity(), available));
        }

        BigDecimal totalAmount = price.currentPrice().multiply(BigDecimal.valueOf(request.quantity()));

        Trade trade = Trade.builder()
                .userId(userId).stockSymbol(request.symbol()).tradeType(Trade.TradeType.SELL)
                .quantity(request.quantity()).price(price.currentPrice()).totalAmount(totalAmount)
                .status(Trade.TradeStatus.EXECUTED).idempotencyKey(idempotencyKey).build();

        trade = tradeRepository.save(trade);
        publishTradeExecutedEvent(trade);

        log.info("SELL executed: user={}, symbol={}, qty={}, price={}", userId, request.symbol(), request.quantity(), price.currentPrice());
        return TradeResponse.fromEntity(trade);
    }

    public Page<TradeResponse> getTradeHistory(Long userId, Pageable pageable) {
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable)
                .map(TradeResponse::fromEntity);
    }

    private void checkIdempotency(String idempotencyKey) {
        if (idempotencyKey != null && tradeRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateTradeException("Trade with idempotency key already processed: " + idempotencyKey);
        }
    }

    private void publishTradeExecutedEvent(Trade trade) {
        TradeExecutedEvent event = new TradeExecutedEvent(
                UUID.randomUUID().toString(), trade.getId(), trade.getUserId(), trade.getStockSymbol(),
                trade.getTradeType().name(), trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(),
                Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(trade.getId()).eventType("TRADE_EXECUTED").topic("trade-executed")
                    .partitionKey(String.valueOf(trade.getUserId())).payload(payload).build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize TradeExecutedEvent", e);
        }
    }
}
