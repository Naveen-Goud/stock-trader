package com.trading.user.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EventDedupCache {

    private final StringRedisTemplate redisTemplate;
    private static final String DEDUP_PREFIX = "dedup:";
    private static final Duration TTL = Duration.ofHours(24);

    public boolean tryMarkProcessed(String eventId) {
        Boolean wasAbsent = redisTemplate.opsForValue()
                .setIfAbsent(DEDUP_PREFIX + eventId, "1", TTL);
        return Boolean.TRUE.equals(wasAbsent);
    }
}
