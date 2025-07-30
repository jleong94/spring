package com.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bucket;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

/**
 * Configuration class for setting up rate limiting with a caching mechanism.
 * Uses JSR-107 (JCache) to store buckets (token buckets for rate limiting).
 */
@Configuration
public class RateLimitConfig {

	/**
     * Provides the default JCache CacheManager.
     * This is required to create and manage named caches like "buckets".
     *
     * @return a JSR-107 compatible CacheManager
     */
	@Bean(name = "rateLimitCacheManager")
	CacheManager cacheManager() {
		// Get the default caching provider (e.g., Ehcache, Hazelcast, etc.)
		CachingProvider provider = Caching.getCachingProvider();
        return provider.getCacheManager();
	}

	/**
     * Configures and creates a cache named "buckets" to store rate limit buckets.
     * This cache uses a TTL (time-to-live) of 1 minute for each entry.
     *
     * @param cacheManager the CacheManager used to create caches
     * @return a cache storing token buckets keyed by a String identifier (e.g., user ID or IP)
     */
	@Bean
    Cache<String, Bucket> bucketCache(CacheManager cacheManager) {
        MutableConfiguration<String, Bucket> config = new MutableConfiguration<String, Bucket>()
                .setStoreByValue(false)// Store references instead of copying the Bucket object
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE))// TTL per entry
                .setStatisticsEnabled(true);// Enable cache hit/miss statistics
        return cacheManager.createCache("buckets", config);
    }
	
	/**
     * Binds custom rate-limiting configuration properties.
     * This bean can be used to read values from application.properties or application.yml.
     *
     * @return a RateLimitProperties bean instance
     */
	@Bean
    RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }
}
