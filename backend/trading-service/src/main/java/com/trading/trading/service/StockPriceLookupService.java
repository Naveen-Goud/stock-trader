package com.trading.trading.service;

import com.trading.trading.dto.StockPriceResponse;
import com.trading.trading.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StockPriceLookupService {

    private final StringRedisTemplate redisTemplate;
    private static final String PRICE_KEY_PREFIX = "stock:price:";

    public StockPriceResponse getCurrentPrice(String symbol) {
        String value = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + symbol);
        if (value == null) {
            throw new StockNotFoundException("No price data available for symbol: " + symbol);
        }
        return new StockPriceResponse(symbol, new BigDecimal(value));
    }
}
