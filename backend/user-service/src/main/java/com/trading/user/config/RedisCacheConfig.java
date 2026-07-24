package com.trading.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring Boot's default RedisCacheManager serializes cache values with plain
 * JDK serialization, which requires every cached object to implement
 * java.io.Serializable. Our DTOs are Java records (e.g. UserResponse) and
 * don't implement it, so @Cacheable methods fail at runtime with a bare
 * 500 the first time a value is written to Redis.
 *
 * This configures Redis to serialize cache values as JSON instead, which
 * works with records/DTOs out of the box and is the standard approach for
 * Spring + Redis caching.
 */
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper.copy());

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(60000))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
    }
}
