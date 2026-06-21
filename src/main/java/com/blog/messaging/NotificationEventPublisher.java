package com.blog.messaging;

import com.blog.payloads.NotificationEvent;

/**
 * Publishes a notification event. The active implementation depends on configuration:
 * Kafka when {@code app.kafka.enabled=true}, synchronous otherwise.
 */
public interface NotificationEventPublisher {
	void publish(NotificationEvent event);
}
