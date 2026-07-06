package com.thakur.scheduler.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestCacheConfig {

    @Bean
    @Primary
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "recommendations",
                "executionOrder",
                "dailyPlan"
        );
    }
}