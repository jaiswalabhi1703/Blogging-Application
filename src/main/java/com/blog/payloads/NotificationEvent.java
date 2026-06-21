package com.blog.payloads;

/**
 * Event published to Kafka (or handled synchronously) describing something that
 * should notify a user. Serialized to JSON on the wire.
 *
 * @param recipientId user who should receive the notification
 * @param type        notification type, e.g. "COMMENT"
 * @param message     human-readable message
 * @param postId      related post id (deep link), nullable
 */
public record NotificationEvent(Integer recipientId, String type, String message, Integer postId) {
}
