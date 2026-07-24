package com.trading.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.user.cache.EventDedupCache;
import com.trading.user.service.WalletUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeExecutedConsumer {

    private final ObjectMapper objectMapper;
    private final WalletUpdateService walletUpdateService;
    private final EventDedupCache dedupCache;

    @KafkaListener(
            topics = "trade-executed",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTradeExecuted(String payload) throws Exception {
        TradeExecutedEvent event = objectMapper.readValue(payload, TradeExecutedEvent.class);

        if (!dedupCache.tryMarkProcessed(event.eventId())) {
            log.info("Duplicate trade-executed event {} ignored (already processed)", event.eventId());
            return;
        }

        log.info("Applying wallet update: tradeId={}, userId={}, type={}, amount={}",
                event.tradeId(), event.userId(), event.tradeType(), event.totalAmount());
        walletUpdateService.applyTrade(event);
    }
}
