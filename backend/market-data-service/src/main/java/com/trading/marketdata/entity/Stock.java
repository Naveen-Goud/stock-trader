package com.trading.marketdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks", indexes = {
        @Index(name = "idx_stocks_symbol", columnList = "symbol", unique = true),
        @Index(name = "idx_stocks_sector", columnList = "sector")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 50)
    private String sector;

    @Column(name = "current_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", nullable = false, precision = 12, scale = 4)
    private BigDecimal previousClose;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal volatility;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal drift;

    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "last_updated", nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
