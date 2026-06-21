package com.blog.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.blog.payloads.ApiResponse;
import com.blog.payloads.NotificationDto;
import com.blog.service.NotificationService;
import com.blog.service.NotificationStreamService;
import com.blog.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * User notifications: list, unread count, mark-as-read, and a real-time SSE stream.
 * All endpoints operate on the authenticated user.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Real-time, event-driven user notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationStreamService streamService;
	private final UserService userService;

	public NotificationController(NotificationService notificationService,
			NotificationStreamService streamService, UserService userService) {
		this.notificationService = notificationService;
		this.streamService = streamService;
		this.userService = userService;
	}

	@Operation(summary = "List the current user's notifications (newest first)")
	@GetMapping
	public ResponseEntity<List<NotificationDto>> list(Authentication authentication) {
		return ResponseEntity.ok(notificationService.listForUser(currentUserId(authentication)));
	}

	@Operation(summary = "Count of unread notifications")
	@GetMapping("/unread-count")
	public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
		return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(currentUserId(authentication))));
	}

	@Operation(summary = "Mark a notification as read")
	@PutMapping("/{id}/read")
	public ResponseEntity<ApiResponse> markRead(@PathVariable Long id, Authentication authentication) {
		notificationService.markSeen(id, currentUserId(authentication));
		return ResponseEntity.ok(new ApiResponse("Notification marked as read", true));
	}

	@Operation(summary = "Subscribe to a real-time stream of new notifications (Server-Sent Events)")
	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(Authentication authentication) {
		return streamService.subscribe(currentUserId(authentication));
	}

	private Integer currentUserId(Authentication authentication) {
		return userService.getUserByEmail(authentication.getName()).getId();
	}
}
