package com.blog.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.blog.payloads.NotificationDto;

/**
 * Keeps the live (Server-Sent Events) connections per user and pushes
 * notifications to them in real time as they are created.
 */
@Service
public class NotificationStreamService {

	private static final Logger log = LoggerFactory.getLogger(NotificationStreamService.class);
	private static final long TIMEOUT = 30 * 60 * 1000L; // 30 min

	private final Map<Integer, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	/** Registers a new live connection for a user. */
	public SseEmitter subscribe(Integer userId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> remove(userId, emitter));
		emitter.onTimeout(() -> remove(userId, emitter));
		emitter.onError(e -> remove(userId, emitter));

		try {
			emitter.send(SseEmitter.event().name("connected").data("ok"));
		} catch (IOException ignored) {
			remove(userId, emitter);
		}
		return emitter;
	}

	/** Pushes a notification to all of a user's open connections. */
	public void push(Integer userId, NotificationDto notification) {
		List<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters == null) {
			return;
		}
		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event().name("notification").data(notification));
			} catch (IOException ex) {
				log.debug("Dropping dead SSE connection for user {}", userId);
				remove(userId, emitter);
			}
		}
	}

	private void remove(Integer userId, SseEmitter emitter) {
		List<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters != null) {
			userEmitters.remove(emitter);
			if (userEmitters.isEmpty()) {
				emitters.remove(userId);
			}
		}
	}
}
