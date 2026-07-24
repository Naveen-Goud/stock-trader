package com.trading.notification.controller;

import com.trading.notification.dto.*;
import com.trading.notification.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationQueryService.getNotifications(userId, page, size));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        notificationQueryService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markOneRead(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        notificationQueryService.markOneRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}
