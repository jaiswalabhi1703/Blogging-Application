package com.blog.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.payloads.PostDto;
import com.blog.service.AiContentService;
import com.blog.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AI-assisted authoring endpoints powered by OpenAI.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "AI Assistant", description = "OpenAI-powered summary and tag generation")
public class AiController {

	private final PostService postService;
	private final AiContentService aiContentService;

	public AiController(PostService postService, AiContentService aiContentService) {
		this.postService = postService;
		this.aiContentService = aiContentService;
	}

	@Operation(summary = "Whether AI features are currently enabled on this deployment")
	@GetMapping("/ai/status")
	public ResponseEntity<Map<String, Object>> status() {
		return ResponseEntity.ok(Map.of("enabled", aiContentService.isEnabled()));
	}

	@Operation(summary = "Generate and persist an AI summary + tags for a post (requires authentication)")
	@PostMapping("/posts/{postId}/ai/summarize")
	public ResponseEntity<PostDto> summarizePost(@PathVariable Integer postId) {
		return ResponseEntity.ok(postService.summarizePost(postId));
	}
}
