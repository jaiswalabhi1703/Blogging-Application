package com.blog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.entities.Notification;
import com.blog.exceptions.ApiException;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.payloads.NotificationDto;
import com.blog.payloads.NotificationEvent;
import com.blog.repositories.NotificationRepo;

/**
 * Persists notifications and pushes them to live SSE connections. The entry point
 * {@link #handle(NotificationEvent)} is invoked by the Kafka consumer (async) or by
 * the synchronous publisher when Kafka is disabled - the behaviour is identical.
 */
@Service
public class NotificationService {

	private final NotificationRepo notificationRepo;
	private final NotificationStreamService streamService;

	public NotificationService(NotificationRepo notificationRepo, NotificationStreamService streamService) {
		this.notificationRepo = notificationRepo;
		this.streamService = streamService;
	}

	@Transactional
	public void handle(NotificationEvent event) {
		Notification notification = new Notification();
		notification.setRecipientId(event.recipientId());
		notification.setType(event.type());
		notification.setMessage(event.message());
		notification.setPostId(event.postId());

		Notification saved = notificationRepo.save(notification);
		// real-time push to any open SSE connections for this user
		streamService.push(event.recipientId(), toDto(saved));
	}

	@Transactional(readOnly = true)
	public List<NotificationDto> listForUser(Integer userId) {
		return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId)
				.stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public long unreadCount(Integer userId) {
		return notificationRepo.countByRecipientIdAndSeenFalse(userId);
	}

	@Transactional
	public void markSeen(Long id, Integer userId) {
		Notification notification = notificationRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
		if (!notification.getRecipientId().equals(userId)) {
			throw new ApiException("You cannot modify another user's notification");
		}
		notification.setSeen(true);
		notificationRepo.save(notification);
	}

	private NotificationDto toDto(Notification n) {
		return new NotificationDto(n.getId(), n.getType(), n.getMessage(), n.getPostId(), n.isSeen(),
				n.getCreatedAt());
	}
}
