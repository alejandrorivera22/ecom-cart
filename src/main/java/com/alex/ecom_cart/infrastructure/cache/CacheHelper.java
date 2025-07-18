package com.alex.ecom_cart.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheHelper {

    private final RedisCacheManagerWrapper cacheManagerWrapper;

    public <T> void putCacheValues(String cacheName, T value, String... keys) {
        for (String key : keys) {
            cacheManagerWrapper.putCacheValue(cacheName, key, value);
        }
    }

    public void evictCacheKeys(String cacheName, String... keys) {
        for (String key : keys) {
            cacheManagerWrapper.evictCacheKey(cacheName, key);
        }
    }

}
