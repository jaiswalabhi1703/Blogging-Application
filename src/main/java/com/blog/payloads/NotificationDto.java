package com.blog.payloads;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
	private Long id;
	private String type;
	private String message;
	private Integer postId;
	private boolean seen;
	private Instant createdAt;
}
