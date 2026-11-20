package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // 使用 Spring 内置的并发 Map 作为缓存，不需要连接任何外部服务
        return new ConcurrentMapCacheManager();
    }
}