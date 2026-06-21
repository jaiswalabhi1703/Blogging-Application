package com.blog.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A notification delivered to a user (e.g. "someone commented on your post").
 * Written asynchronously by the Kafka consumer (or synchronously when Kafka is disabled).
 */
@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notif_recipient", columnList = "recipient_id"))
@Getter
@Setter
@NoArgsConstructor
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** User id this notification belongs to. */
	@Column(name = "recipient_id", nullable = false)
	private Integer recipientId;

	@Column(nullable = false, length = 50)
	private String type;

	@Column(nullable = false, length = 500)
	private String message;

	/** Optional deep-link target (e.g. the post id). */
	private Integer postId;

	@Column(nullable = false)
	private boolean seen = false;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();
}
