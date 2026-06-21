package com.blog.exceptions;

/**
 * Thrown when a refresh token is missing, expired, or has been revoked.
 */
public class TokenRefreshException extends RuntimeException {

	public TokenRefreshException(String message) {
		super(message);
	}
}
