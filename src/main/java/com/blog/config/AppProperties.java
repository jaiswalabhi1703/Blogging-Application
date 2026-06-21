package com.blog.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Strongly-typed binding for all {@code app.*} configuration.
 * Keeps secrets and tunables out of the code and in the environment.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Jwt jwt = new Jwt();
	private final Cors cors = new Cors();
	private final Ai ai = new Ai();
	private final Kafka kafka = new Kafka();
	private final Redis redis = new Redis();
	private final Cache cache = new Cache();

	@Getter
	@Setter
	public static class Jwt {
		/** Base64-encoded HMAC-SHA secret. Provide via JWT_SECRET in real environments. */
		private String secret;
		/** Access-token lifetime in milliseconds (default 15 min). */
		private long accessTokenExpirationMs = 900_000L;
		/** Refresh-token lifetime in milliseconds (default 7 days). */
		private long refreshTokenExpirationMs = 604_800_000L;
	}

	@Getter
	@Setter
	public static class Cors {
		/** Origins allowed to call the API from a browser (frontend, Cloudflare domains). */
		private List<String> allowedOrigins = List.of("http://localhost:3000");
	}

	@Getter
	@Setter
	public static class Ai {
		/** Master switch for AI-assisted endpoints. */
		private boolean enabled = true;
		/** OpenAI model id. */
		private String model = "gpt-4o-mini";
		/** OpenAI API key. Empty disables AI features gracefully. */
		private String apiKey = "";
		/** OpenAI-compatible API base URL. */
		private String baseUrl = "https://api.openai.com/v1";
	}

	@Getter
	@Setter
	public static class Kafka {
		/** When true, notifications are published to Kafka; otherwise produced synchronously. */
		private boolean enabled = false;
		/** Topic that carries notification events. */
		private String notificationsTopic = "blog.notifications";
	}

	@Getter
	@Setter
	public static class Redis {
		/** When true, the cache-aside layer is backed by Redis; otherwise an in-memory cache. */
		private boolean enabled = false;
	}

	@Getter
	@Setter
	public static class Cache {
		/** TTL (seconds) for cached posts. */
		private long postTtlSeconds = 600;
	}
}
