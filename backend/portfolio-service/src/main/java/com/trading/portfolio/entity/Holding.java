package com.trading.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_stock", columnNames = {"user_id", "stock_symbol"}),
       indexes = @Index(name = "idx_holdings_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_symbol", nullable = false, length = 10)
    private String stockSymbol;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "avg_buy_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal avgBuyPrice;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version;
}
