package com.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blog.entities.Notification;
import com.blog.exceptions.ApiException;
import com.blog.payloads.NotificationEvent;
import com.blog.repositories.NotificationRepo;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepo notificationRepo;

	@Mock
	private NotificationStreamService streamService;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	void handlePersistsAndPushesToStream() {
		when(notificationRepo.save(any(Notification.class))).thenAnswer(inv -> {
			Notification n = inv.getArgument(0);
			n.setId(1L);
			return n;
		});

		notificationService.handle(new NotificationEvent(42, "COMMENT", "Ada commented on your post", 7));

		verify(notificationRepo).save(any(Notification.class));
		verify(streamService).push(eq(42), any());
	}

	@Test
	void markSeenRejectsAnotherUsersNotification() {
		Notification n = new Notification();
		n.setId(5L);
		n.setRecipientId(100);
		when(notificationRepo.findById(5L)).thenReturn(Optional.of(n));

		assertThatThrownBy(() -> notificationService.markSeen(5L, 999))
				.isInstanceOf(ApiException.class);

		verify(notificationRepo, never()).save(any());
	}

	@Test
	void unreadCountDelegatesToRepo() {
		when(notificationRepo.countByRecipientIdAndSeenFalse(7)).thenReturn(3L);

		assertThat(notificationService.unreadCount(7)).isEqualTo(3L);
	}
}
