package com.blog.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.blog.config.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and validates JWTs using the modern jjwt 0.12.x API.
 * Issues short-lived access tokens and long-lived refresh tokens, both signed
 * with an HMAC-SHA key derived from {@code app.jwt.secret}.
 */
@Component
public class JwtTokenHelper {

	private static final String CLAIM_TOKEN_TYPE = "type";
	public static final String TYPE_ACCESS = "access";
	public static final String TYPE_REFRESH = "refresh";

	private final SecretKey signingKey;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;

	public JwtTokenHelper(AppProperties properties) {
		// Accept either a Base64-encoded key or a raw passphrase of sufficient length.
		byte[] keyBytes = decodeSecret(properties.getJwt().getSecret());
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenExpirationMs = properties.getJwt().getAccessTokenExpirationMs();
		this.refreshTokenExpirationMs = properties.getJwt().getRefreshTokenExpirationMs();
	}

	private byte[] decodeSecret(String secret) {
		try {
			return Decoders.BASE64.decode(secret);
		} catch (RuntimeException ex) {
			return secret.getBytes();
		}
	}

	// ---- read helpers ----

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public String getTokenType(String token) {
		return getClaimFromToken(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(getAllClaimsFromToken(token));
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private boolean isTokenExpired(String token) {
		return getExpirationDateFromToken(token).before(new Date());
	}

	// ---- generation ----

	public String generateAccessToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_TOKEN_TYPE, TYPE_ACCESS);
		return buildToken(claims, userDetails.getUsername(), accessTokenExpirationMs);
	}

	public String generateRefreshToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_TOKEN_TYPE, TYPE_REFRESH);
		// Unique JWT id so rotated tokens are always distinct, even when issued
		// within the same second (otherwise two tokens would be byte-identical).
		claims.put("jti", UUID.randomUUID().toString());
		return buildToken(claims, username, refreshTokenExpirationMs);
	}

	private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
		Date now = new Date();
		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMs))
				.signWith(signingKey)
				.compact();
	}

	// ---- validation ----

	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public long getRefreshTokenExpirationMs() {
		return refreshTokenExpirationMs;
	}
}
