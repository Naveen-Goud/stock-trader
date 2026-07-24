package com.trading.portfolio.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.portfolio.dto.PortfolioSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PORTFOLIO_KEY_PREFIX = "portfolio:";
    private static final Duration TTL = Duration.ofSeconds(30);

    public PortfolioSummaryResponse get(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(PORTFOLIO_KEY_PREFIX + userId);
            return json != null ? objectMapper.readValue(json, PortfolioSummaryResponse.class) : null;
        } catch (Exception e) {
            log.warn("Failed to read portfolio cache for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public void put(Long userId, PortfolioSummaryResponse summary) {
        try {
            String json = objectMapper.writeValueAsString(summary);
            redisTemplate.opsForValue().set(PORTFOLIO_KEY_PREFIX + userId, json, TTL);
        } catch (Exception e) {
            log.warn("Failed to write portfolio cache for user {}: {}", userId, e.getMessage());
        }
    }

    public void evict(Long userId) {
        redisTemplate.delete(PORTFOLIO_KEY_PREFIX + userId);
    }
}
