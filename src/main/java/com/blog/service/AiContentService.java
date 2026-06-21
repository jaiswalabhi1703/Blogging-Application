package com.blog.service;

import com.blog.payloads.AiSummaryResult;

/**
 * AI-assisted authoring features backed by OpenAI.
 */
public interface AiContentService {

	/** @return true when an API key is configured and AI features are enabled. */
	boolean isEnabled();

	/**
	 * Generates a concise summary and a set of topic tags for a blog post.
	 *
	 * @param title   the post title
	 * @param content the post body
	 * @return the generated summary and tags
	 */
	AiSummaryResult summarize(String title, String content);
}
