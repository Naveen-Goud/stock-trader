package com.trading.notification.service;

import com.trading.notification.dto.*;
import com.trading.notification.entity.Notification;
import com.trading.notification.entity.PriceAlert;
import com.trading.notification.exception.AlertNotFoundException;
import com.trading.notification.repository.NotificationRepository;
import com.trading.notification.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final PriceAlertRepository priceAlertRepository;

    public NotificationPageResponse getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long unreadCount = notificationRepository.countByUserIdAndIsRead(userId, false);

        return new NotificationPageResponse(
                notifPage.getContent().stream().map(NotificationResponse::from).toList(),
                notifPage.getTotalElements(), notifPage.getTotalPages(), unreadCount);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    @Transactional
    public void markOneRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markOneRead(notificationId, userId);
        if (updated == 0) {
            throw new AlertNotFoundException("Notification not found or does not belong to user");
        }
    }

    @Transactional
    public PriceAlertResponse createPriceAlert(Long userId, CreatePriceAlertRequest request) {
        PriceAlert alert = PriceAlert.builder()
                .userId(userId).stockSymbol(request.symbol())
                .alertType(PriceAlert.AlertType.valueOf(request.alertType()))
                .targetPrice(request.targetPrice()).isActive(true).build();

        alert = priceAlertRepository.save(alert);
        return toAlertResponse(alert);
    }

    public List<PriceAlertResponse> getPriceAlerts(Long userId) {
        return priceAlertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toAlertResponse).toList();
    }

    @Transactional
    public void deletePriceAlert(Long alertId, Long userId) {
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException("Price alert not found: " + alertId));
        priceAlertRepository.delete(alert);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldNotifications() {
        notificationRepository.deleteOlderThan(LocalDateTime.now().minusDays(90));
    }

    private PriceAlertResponse toAlertResponse(PriceAlert a) {
        return new PriceAlertResponse(
                a.getId(), a.getStockSymbol(), a.getAlertType().name(),
                a.getTargetPrice(), a.isActive(), a.getTriggeredAt(), a.getCreatedAt());
    }
}
