package com.alex.ecom_cart.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class RedisConfig {

    @Value(value = "${cache.redis.address}")
    private String serverAddress;
    @Value(value = "${cache.redis.password}")
    private String serverPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(serverAddress)
                .setPassword(serverPassword);
        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> configs = new HashMap<>();
        configs.put(CacheConstants.PRODUCT_CACHE_NAME, new CacheConfig(30 * 60 * 1000L, 15 * 60 * 1000L));
        configs.put(CacheConstants.CUSTOMERS_CACHE_NAME, new CacheConfig(15 * 60 * 1000L, 5 * 60 * 1000L));
        configs.put(CacheConstants.ORDERS_CACHE_NAME, new CacheConfig(60 * 1000L, 300 * 1000L));
        configs.put(CacheConstants.ORDERS_DETAILS_CACHE_NAME, new CacheConfig(60 * 1000L, 300 * 1000L));

        return new RedissonSpringCacheManager(redissonClient, configs);
    }

    public class CacheConstants {
        public static final String PRODUCT_CACHE_NAME = "products"; //cache name for products
        public static final String CUSTOMERS_CACHE_NAME = "customers"; // cache name for users
        public static final String ORDERS_CACHE_NAME = "orders"; // cache name for orders
        public static final String ORDERS_DETAILS_CACHE_NAME = "order_detail"; // cache name for orders



    }
}
