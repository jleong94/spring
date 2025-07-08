package com.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

@Configuration
public class RateLimitCacheConfig {

	@Bean
	CacheManager cacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
        return provider.getCacheManager();
	}

	@PostConstruct
    public Cache<String, Bucket> createBucketCache(CacheManager cacheManager) {
        MutableConfiguration<String, Bucket> config = new MutableConfiguration<String, Bucket>()
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE))
                .setStatisticsEnabled(true);
        return cacheManager.createCache("buckets", config);
    }
}
