package com.trading.notification.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContextCache {

    private final StringRedisTemplate redisTemplate;
    private static final String USER_KEY_PREFIX = "user:name:";
    private static final Duration TTL = Duration.ofHours(12);

    public void storeUsername(Long userId, String username) {
        redisTemplate.opsForValue().set(USER_KEY_PREFIX + userId, username, TTL);
    }

    public String getUsername(Long userId) {
        return redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
    }
}
