package com.blog.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.blog.config.AppProperties;
import com.blog.payloads.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Event-driven publisher: sends notification events to Kafka so they are processed
 * asynchronously, off the API response path. Active when {@code app.kafka.enabled=true}.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaNotificationPublisher implements NotificationEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaNotificationPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String topic;

	public KafkaNotificationPublisher(KafkaTemplate<String, String> kafkaTemplate, AppProperties properties) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = properties.getKafka().getNotificationsTopic();
	}

	@Override
	public void publish(NotificationEvent event) {
		try {
			kafkaTemplate.send(topic, String.valueOf(event.recipientId()),
					objectMapper.writeValueAsString(event));
			log.debug("Published notification event to Kafka topic {}", topic);
		} catch (JsonProcessingException ex) {
			log.error("Failed to serialize notification event", ex);
		}
	}
}
