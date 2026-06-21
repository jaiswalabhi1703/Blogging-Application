package com.blog.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.blog.payloads.NotificationEvent;
import com.blog.service.NotificationService;

/**
 * Fallback publisher used when Kafka is disabled (local dev / tests): handles the
 * event synchronously so the notification feature still works without a broker.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class SyncNotificationPublisher implements NotificationEventPublisher {

	private final NotificationService notificationService;

	public SyncNotificationPublisher(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Override
	public void publish(NotificationEvent event) {
		notificationService.handle(event);
	}
}
