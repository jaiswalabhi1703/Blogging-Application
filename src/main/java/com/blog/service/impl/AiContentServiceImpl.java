package com.blog.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.blog.config.AppProperties;
import com.blog.exceptions.ApiException;
import com.blog.payloads.AiSummaryResult;
import com.blog.service.AiContentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI-backed implementation of {@link AiContentService}.
 *
 * <p>Talks to the OpenAI Chat Completions API over plain HTTP (no SDK). The client
 * is only created when an API key is present, so the application starts and serves
 * every other endpoint normally without one.
 */
@Service
public class AiContentServiceImpl implements AiContentService {

	private static final Logger log = LoggerFactory.getLogger(AiContentServiceImpl.class);

	private final AppProperties.Ai aiProps;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RestClient client; // null when no API key is configured

	public AiContentServiceImpl(AppProperties appProperties) {
		this.aiProps = appProperties.getAi();
		if (aiProps.isEnabled() && aiProps.getApiKey() != null && !aiProps.getApiKey().isBlank()) {
			this.client = RestClient.builder()
					.baseUrl(aiProps.getBaseUrl())
					.defaultHeader("Authorization", "Bearer " + aiProps.getApiKey())
					.build();
			log.info("AI assistant enabled (model={})", aiProps.getModel());
		} else {
			this.client = null;
			log.info("AI assistant disabled (no OPENAI_API_KEY configured)");
		}
	}

	@Override
	public boolean isEnabled() {
		return client != null;
	}

	@Override
	public AiSummaryResult summarize(String title, String content) {
		if (!isEnabled()) {
			throw new ApiException("AI assistant is not configured. Set OPENAI_API_KEY to enable it.");
		}

		String prompt = """
				You are an assistant for a blogging platform. Read the blog post below and produce:
				1. A concise, engaging summary (2-3 sentences, max 60 words).
				2. Between 3 and 6 short topic tags (single words or short phrases, lowercase).

				Respond with ONLY a JSON object in exactly this shape, and nothing else:
				{"summary": "...", "tags": ["tag1", "tag2", "tag3"]}

				TITLE: %s

				CONTENT:
				%s
				""".formatted(nullSafe(title), nullSafe(content));

		Map<String, Object> requestBody = Map.of(
				"model", aiProps.getModel(),
				"messages", List.of(Map.of("role", "user", "content", prompt)),
				"response_format", Map.of("type", "json_object"),
				"temperature", 0.4);

		try {
			String response = client.post()
					.uri("/chat/completions")
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
					.retrieve()
					.body(String.class);

			JsonNode root = objectMapper.readTree(response);
			String messageContent = root.path("choices").path(0).path("message").path("content").asText("");
			return parse(messageContent);
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("AI summarization failed", ex);
			throw new ApiException("AI assistant request failed: " + ex.getMessage());
		}
	}

	/** Extracts the JSON object from the model output and maps it to a result, with a safe fallback. */
	private AiSummaryResult parse(String raw) {
		String json = extractJson(raw);
		try {
			JsonNode node = objectMapper.readTree(json);
			String summary = node.path("summary").asText("").trim();
			List<String> tags = node.has("tags")
					? objectMapper.convertValue(node.get("tags"), objectMapper.getTypeFactory()
							.constructCollectionType(List.class, String.class))
					: List.of();
			if (summary.isBlank()) {
				summary = raw.trim();
			}
			return new AiSummaryResult(summary, tags);
		} catch (Exception ex) {
			// Model returned prose instead of JSON - degrade gracefully.
			log.warn("Could not parse AI JSON response, returning raw text as summary");
			return new AiSummaryResult(raw.trim(), List.of());
		}
	}

	private String extractJson(String raw) {
		int start = raw.indexOf('{');
		int end = raw.lastIndexOf('}');
		if (start >= 0 && end > start) {
			return raw.substring(start, end + 1);
		}
		return raw;
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}
}
