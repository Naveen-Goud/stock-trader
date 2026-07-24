package com.trading.notification.controller;

import com.trading.notification.dto.*;
import com.trading.notification.service.NotificationQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final NotificationQueryService notificationQueryService;

    @PostMapping
    public ResponseEntity<PriceAlertResponse> create(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CreatePriceAlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationQueryService.createPriceAlert(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<PriceAlertResponse>> getAll(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationQueryService.getPriceAlerts(userId));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> delete(@RequestHeader("X-User-Id") Long userId, @PathVariable Long alertId) {
        notificationQueryService.deletePriceAlert(alertId, userId);
        return ResponseEntity.noContent().build();
    }
}
