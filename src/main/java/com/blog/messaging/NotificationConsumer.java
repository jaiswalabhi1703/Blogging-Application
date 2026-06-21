package com.blog.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.blog.payloads.NotificationEvent;
import com.blog.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumes notification events from Kafka and persists/pushes them.
 * Active only when {@code app.kafka.enabled=true}.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class NotificationConsumer {

	private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

	private final NotificationService notificationService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NotificationConsumer(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = "${app.kafka.notifications-topic}", groupId = "${spring.kafka.consumer.group-id}")
	public void onMessage(String payload) {
		try {
			NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
			notificationService.handle(event);
		} catch (Exception ex) {
			log.error("Failed to process notification event: {}", payload, ex);
		}
	}
}
