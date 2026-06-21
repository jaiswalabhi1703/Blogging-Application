package com.blog.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.entities.Notification;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

	long countByRecipientIdAndSeenFalse(Integer recipientId);
}
