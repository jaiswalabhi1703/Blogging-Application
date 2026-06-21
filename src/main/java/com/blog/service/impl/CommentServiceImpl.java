package com.blog.service.impl;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.blog.entities.Comment;
import com.blog.entities.Post;
import com.blog.entities.User;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.messaging.NotificationEventPublisher;
import com.blog.payloads.CommentDto;
import com.blog.payloads.NotificationEvent;
import com.blog.repositories.CommentRepo;
import com.blog.repositories.PostRepo;
import com.blog.repositories.UserRepo;
import com.blog.service.CommentService;

@Service
public class CommentServiceImpl implements CommentService {

	private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

	@Autowired
	private PostRepo postRepo;

	@Autowired
	private CommentRepo commentRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private NotificationEventPublisher notificationPublisher;

	// kya comment karna , kis post pe comment karna h.
	@Override
	public CommentDto createComment(CommentDto commentDto, Integer postId) {

		// 1st find the post
		Post post = this.postRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));

		Comment comment = this.modelMapper.map(commentDto, Comment.class);
		comment.setPost(post);
		Comment savedComment = this.commentRepo.save(comment);

		notifyPostAuthor(post);

		return this.modelMapper.map(savedComment, CommentDto.class);
	}

	/**
	 * Fires a notification event to the post's author (unless they commented on their own post).
	 * Publishing is event-driven via Kafka when enabled, synchronous otherwise; failures here
	 * must never break comment creation.
	 */
	private void notifyPostAuthor(Post post) {
		try {
			User author = post.getUser();
			if (author == null) {
				return;
			}
			String actorEmail = currentUserEmail();
			if (actorEmail != null && actorEmail.equals(author.getEmail())) {
				return; // commenting on your own post - no notification
			}
			String actorName = actorEmail == null ? "Someone"
					: this.userRepo.findByEmail(actorEmail).map(User::getName).orElse("Someone");

			NotificationEvent event = new NotificationEvent(
					author.getId(),
					"COMMENT",
					actorName + " commented on your post: " + post.getTitle(),
					post.getPostId());
			this.notificationPublisher.publish(event);
		} catch (Exception ex) {
			log.warn("Could not publish comment notification", ex);
		}
	}

	private String currentUserEmail() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		return auth == null ? null : auth.getName();
	}

	@Override
	public void deleteComment(Integer commentId) {
		Comment com = commentRepo.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment", "comment id", commentId));
		this.commentRepo.delete(com);
	}

}
