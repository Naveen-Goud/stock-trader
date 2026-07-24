package com.trading.notification.service;

import com.trading.notification.dto.WebSocketNotificationPayload;
import com.trading.notification.entity.Notification;
import com.trading.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDomainServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationDomainService service;

    @Test
    void createAndBroadcast_persistsNotification() {
        Notification saved = Notification.builder()
                .id(1L).userId(1L).type(Notification.NotificationType.TRADE_EXECUTED)
                .message("Bought 10 shares of AAPL at $190.00").relatedSymbol("AAPL").build();

        when(notificationRepository.save(any())).thenReturn(saved);

        service.createAndBroadcast(1L, "naveen", Notification.NotificationType.TRADE_EXECUTED,
                "Bought 10 shares of AAPL at $190.00", "AAPL");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createAndBroadcast_broadcastsToCorrectUser() {
        Notification saved = Notification.builder()
                .id(2L).userId(1L).type(Notification.NotificationType.PRICE_ALERT)
                .message("AAPL hit $200").relatedSymbol("AAPL").build();

        when(notificationRepository.save(any())).thenReturn(saved);

        service.createAndBroadcast(1L, "naveen", Notification.NotificationType.PRICE_ALERT, "AAPL hit $200", "AAPL");

        ArgumentCaptor<WebSocketNotificationPayload> payloadCaptor = ArgumentCaptor.forClass(WebSocketNotificationPayload.class);
        verify(messagingTemplate).convertAndSendToUser(eq("naveen"), eq("/queue/notifications"), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue().type()).isEqualTo("PRICE_ALERT");
    }

    @Test
    void createAndBroadcast_webSocketFailure_doesNotThrow() {
        Notification saved = Notification.builder()
                .id(3L).userId(1L).type(Notification.NotificationType.TRADE_EXECUTED).message("Sold 5 MSFT").build();

        when(notificationRepository.save(any())).thenReturn(saved);
        doThrow(new RuntimeException("WS broker down"))
                .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                service.createAndBroadcast(1L, "naveen", Notification.NotificationType.TRADE_EXECUTED, "Sold 5 MSFT", "MSFT"));

        verify(notificationRepository).save(any());
    }
}
