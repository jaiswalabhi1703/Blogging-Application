package com.blog.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Cache-aside configuration. Backed by Redis when {@code app.redis.enabled=true}
 * (prod / docker-compose); otherwise an in-memory cache so the app runs locally
 * and in tests without Redis. Either way, the {@code @Cacheable} annotations on the
 * post service are unchanged - only the backing store differs.
 */
@Configuration
@EnableCaching
public class CacheConfig {

	public static final String POSTS_CACHE = "posts";

	@Bean
	@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
	public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, AppProperties properties) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofSeconds(properties.getCache().getPostTtlSeconds()))
				.disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}

	@Bean
	@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
	public CacheManager inMemoryCacheManager() {
		return new ConcurrentMapCacheManager(POSTS_CACHE);
	}
}
