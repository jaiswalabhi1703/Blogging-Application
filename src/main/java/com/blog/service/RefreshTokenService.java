package com.blog.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.config.AppProperties;
import com.blog.entities.RefreshToken;
import com.blog.entities.User;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.exceptions.TokenRefreshException;
import com.blog.repositories.RefreshTokenRepo;
import com.blog.repositories.UserRepo;
import com.blog.security.JwtTokenHelper;

/**
 * Owns the lifecycle of server-side refresh tokens: issue, verify, rotate and revoke.
 */
@Service
public class RefreshTokenService {

	private final RefreshTokenRepo refreshTokenRepo;
	private final UserRepo userRepo;
	private final JwtTokenHelper jwtTokenHelper;
	private final long refreshTokenExpirationMs;

	public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, UserRepo userRepo,
			JwtTokenHelper jwtTokenHelper, AppProperties appProperties) {
		this.refreshTokenRepo = refreshTokenRepo;
		this.userRepo = userRepo;
		this.jwtTokenHelper = jwtTokenHelper;
		this.refreshTokenExpirationMs = appProperties.getJwt().getRefreshTokenExpirationMs();
	}

	/** Issues a fresh, single-use refresh token for the given user (one active token per user). */
	@Transactional
	public RefreshToken createRefreshToken(String email) {
		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email", 0));

		// Rotate: drop any previous token so only the newest one is valid.
		// Flush ensures the DELETE is sent before the new INSERT (Hibernate would
		// otherwise order inserts before deletes within the same transaction).
		refreshTokenRepo.deleteByUser(user);
		refreshTokenRepo.flush();

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(jwtTokenHelper.generateRefreshToken(user.getEmail()));
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
		return refreshTokenRepo.save(refreshToken);
	}

	/** Looks up a stored token, enforcing existence and expiry. */
	public RefreshToken verifyValidToken(String token) {
		RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
				.orElseThrow(() -> new TokenRefreshException("Refresh token not found. Please log in again."));

		if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenRepo.delete(refreshToken);
			throw new TokenRefreshException("Refresh token has expired. Please log in again.");
		}
		return refreshToken;
	}

	@Transactional
	public void revokeForUser(String email) {
		userRepo.findByEmail(email).ifPresent(refreshTokenRepo::deleteByUser);
	}
}
