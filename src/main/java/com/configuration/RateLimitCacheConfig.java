package com.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@Configuration
public class RateLimitCacheConfig {

	@Bean
	CacheManager cacheManager() {
		return Caching.getCachingProvider("com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider").getCacheManager();
	}

	@Bean
	Cache<String, byte[]> bucketCache(CacheManager cacheManager) {
		MutableConfiguration<String, byte[]> config = 
				new MutableConfiguration<String, byte[]>()
				.setTypes(String.class, byte[].class)
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY))
				.setStatisticsEnabled(true);

				return cacheManager.createCache("rate-limit-buckets", config);
	}
}
