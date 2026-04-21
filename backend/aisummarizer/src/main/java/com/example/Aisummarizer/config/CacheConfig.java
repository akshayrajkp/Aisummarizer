package com.example.Aisummarizer.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheCustomizer() {
        return builder -> builder
            .withCacheConfiguration("summaries",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(6))
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer())
                    )
            );
    }
}
