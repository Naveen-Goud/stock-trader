package com.trading.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts", indexes = {
        @Index(name = "idx_price_alerts_user_id", columnList = "user_id"),
        @Index(name = "idx_price_alerts_symbol", columnList = "stock_symbol"),
        @Index(name = "idx_price_alerts_active", columnList = "stock_symbol, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_symbol", nullable = false, length = 10)
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 10)
    private AlertType alertType;

    @Column(name = "target_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal targetPrice;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AlertType { ABOVE, BELOW }
}
