package com.trading.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.StockPriceUpdatedEvent;
import com.trading.notification.cache.NotificationDedupCache;
import com.trading.notification.cache.UserContextCache;
import com.trading.notification.entity.Notification;
import com.trading.notification.entity.PriceAlert;
import com.trading.notification.repository.PriceAlertRepository;
import com.trading.notification.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceAlertConsumer {

    private final ObjectMapper objectMapper;
    private final PriceAlertRepository priceAlertRepository;
    private final NotificationDomainService notificationService;
    private final NotificationDedupCache dedupCache;
    private final UserContextCache userContextCache;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String STOCK_PRICES_TOPIC = "/topic/stock-prices";

    @KafkaListener(topics = "stock-price-updated", groupId = "notification-service-group",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onStockPriceUpdated(String payload) throws Exception {
        StockPriceUpdatedEvent event = objectMapper.readValue(payload, StockPriceUpdatedEvent.class);

        if (!dedupCache.tryMarkProcessed("price:" + event.eventId())) {
            return;
        }

        broadcastLivePrice(event);

        List<PriceAlert> activeAlerts = priceAlertRepository.findByStockSymbolAndIsActiveTrue(event.symbol());

        for (PriceAlert alert : activeAlerts) {
            if (isTriggered(alert, event.currentPrice())) {
                fireAlert(alert, event.currentPrice());
            }
        }
    }

    private void broadcastLivePrice(StockPriceUpdatedEvent event) {
        try {
            messagingTemplate.convertAndSend(STOCK_PRICES_TOPIC, Map.of(
                    "symbol", event.symbol(),
                    "currentPrice", event.currentPrice(),
                    "changePercent", event.changePercent()
            ));
        } catch (Exception ex) {
            log.error("Failed to broadcast live price for {}: {}", event.symbol(), ex.getMessage());
        }
    }

    private boolean isTriggered(PriceAlert alert, BigDecimal currentPrice) {
        return switch (alert.getAlertType()) {
            case ABOVE -> currentPrice.compareTo(alert.getTargetPrice()) >= 0;
            case BELOW -> currentPrice.compareTo(alert.getTargetPrice()) <= 0;
        };
    }

    @Transactional
    protected void fireAlert(PriceAlert alert, BigDecimal currentPrice) {
        alert.setActive(false);
        alert.setTriggeredAt(LocalDateTime.now());
        priceAlertRepository.save(alert);

        String direction = alert.getAlertType() == PriceAlert.AlertType.ABOVE ? "above" : "below";
        String message = String.format("Price Alert: %s is now $%.2f - %s your target of $%.2f",
                alert.getStockSymbol(), currentPrice, direction, alert.getTargetPrice());

        String username = resolveUsername(alert.getUserId());

        notificationService.createAndBroadcast(
                alert.getUserId(), username, Notification.NotificationType.PRICE_ALERT, message, alert.getStockSymbol());
    }

    private String resolveUsername(Long userId) {
        String cached = userContextCache.getUsername(userId);
        return cached != null ? cached : "user:" + userId;
    }
}
