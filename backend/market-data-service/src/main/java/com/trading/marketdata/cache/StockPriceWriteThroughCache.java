package com.trading.marketdata.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceWriteThroughCache {

    private final StringRedisTemplate redisTemplate;
    private static final String PRICE_KEY_PREFIX = "stock:price:";
    private static final Duration TTL = Duration.ofSeconds(10);

    public void updatePrice(String symbol, BigDecimal price) {
        try {
            redisTemplate.opsForValue().set(PRICE_KEY_PREFIX + symbol, price.toPlainString(), TTL);
        } catch (Exception ex) {
            log.error("Redis write failed for symbol {}: {}", symbol, ex.getMessage());
        }
    }

    public BigDecimal getPrice(String symbol) {
        String value = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + symbol);
        return value != null ? new BigDecimal(value) : null;
    }

    public void warmUpCache(java.util.List<com.trading.marketdata.entity.Stock> stocks) {
        stocks.forEach(s -> updatePrice(s.getSymbol(), s.getCurrentPrice()));
        log.info("Redis cache warmed up for {} stocks", stocks.size());
    }
}
