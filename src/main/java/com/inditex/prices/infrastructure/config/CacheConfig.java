package com.inditex.prices.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inditex.prices.domain.model.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@EnableCaching
@Configuration
@RequiredArgsConstructor
@Profile("!local && !integration")
public class CacheConfig {
    private final CacheProperties cacheProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Price> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Price.class);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getCacheTtl())
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
