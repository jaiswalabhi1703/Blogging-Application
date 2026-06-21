package com.blog.payloads;

import java.util.List;

/**
 * Structured result returned by the AI assistant for a post.
 *
 * @param summary a concise abstract of the post
 * @param tags    a handful of suggested topic tags
 */
public record AiSummaryResult(String summary, List<String> tags) {
}
