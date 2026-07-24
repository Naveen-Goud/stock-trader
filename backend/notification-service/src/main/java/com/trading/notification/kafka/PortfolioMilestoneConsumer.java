package com.trading.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.event.PortfolioUpdatedEvent;
import com.trading.notification.cache.NotificationDedupCache;
import com.trading.notification.cache.UserContextCache;
import com.trading.notification.entity.Notification;
import com.trading.notification.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioMilestoneConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationDomainService notificationService;
    private final NotificationDedupCache dedupCache;
    private final UserContextCache userContextCache;

    private static final BigDecimal MILESTONE_INCREMENT = new BigDecimal("1000");

    @KafkaListener(topics = "portfolio-updated", groupId = "notification-service-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void onPortfolioUpdated(String payload) throws Exception {
        PortfolioUpdatedEvent event = objectMapper.readValue(payload, PortfolioUpdatedEvent.class);

        if (!dedupCache.tryMarkProcessed("portfolio:" + event.eventId())) {
            return;
        }

        if (isMilestone(event.portfolioValue())) {
            String message = String.format("Portfolio Milestone: Your %s holdings have reached a value of $%,.2f!",
                    event.symbol(), event.portfolioValue());

            String username = resolveUsername(event.userId());

            notificationService.createAndBroadcast(
                    event.userId(), username, Notification.NotificationType.PORTFOLIO_MILESTONE, message, event.symbol());
        }
    }

    private boolean isMilestone(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) return false;
        return value.remainder(MILESTONE_INCREMENT).abs().compareTo(new BigDecimal("10.00")) < 0;
    }

    private String resolveUsername(Long userId) {
        String cached = userContextCache.getUsername(userId);
        return cached != null ? cached : "user:" + userId;
    }
}
