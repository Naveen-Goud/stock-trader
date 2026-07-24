package com.trading.portfolio.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.portfolio.service.HoldingsUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeExecutedConsumer {

    private final ObjectMapper objectMapper;
    private final HoldingsUpdateService holdingsUpdateService;

    @KafkaListener(
            topics = "trade-executed",
            groupId = "portfolio-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTradeExecuted(String payload) throws Exception {
        TradeExecutedEvent event = objectMapper.readValue(payload, TradeExecutedEvent.class);
        log.info("Received trade-executed event: tradeId={}, userId={}, symbol={}", event.tradeId(), event.userId(), event.symbol());
        holdingsUpdateService.applyTrade(event);
    }
}
