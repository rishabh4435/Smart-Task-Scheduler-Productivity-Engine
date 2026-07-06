package com.thakur.scheduler.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheErrorConfig {

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                exception.printStackTrace();
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                exception.printStackTrace();
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                exception.printStackTrace();
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                exception.printStackTrace();
            }
        };
    }
}