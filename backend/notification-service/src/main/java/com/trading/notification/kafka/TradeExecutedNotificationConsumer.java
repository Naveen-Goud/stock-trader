package com.trading.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.TradeExecutedEvent;
import com.trading.notification.cache.NotificationDedupCache;
import com.trading.notification.cache.UserContextCache;
import com.trading.notification.entity.Notification;
import com.trading.notification.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeExecutedNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationDomainService notificationService;
    private final NotificationDedupCache dedupCache;
    private final UserContextCache userContextCache;

    @KafkaListener(topics = "trade-executed", groupId = "notification-service-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void onTradeExecuted(String payload) throws Exception {
        TradeExecutedEvent event = objectMapper.readValue(payload, TradeExecutedEvent.class);

        if (!dedupCache.tryMarkProcessed("trade:" + event.eventId())) {
            log.info("Duplicate trade-executed event {} skipped", event.eventId());
            return;
        }

        String message = buildTradeMessage(event);
        String username = resolveUsername(event.userId());

        notificationService.createAndBroadcast(
                event.userId(), username, Notification.NotificationType.TRADE_EXECUTED, message, event.symbol());
    }

    private String buildTradeMessage(TradeExecutedEvent event) {
        String action = "BUY".equals(event.tradeType()) ? "Bought" : "Sold";
        return String.format("%s %d shares of %s at $%.2f (Total: $%.2f)",
                action, event.quantity(), event.symbol(), event.price(), event.totalAmount());
    }

    private String resolveUsername(Long userId) {
        String username = userContextCache.getUsername(userId);
        return username != null ? username : "user:" + userId;
    }
}
