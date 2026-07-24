package com.trading.portfolio.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StockPriceCache {

    private final StringRedisTemplate redisTemplate;
    private static final String PRICE_KEY_PREFIX = "stock:price:";

    public BigDecimal getPrice(String symbol) {
        String value = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + symbol);
        return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
    }
}
