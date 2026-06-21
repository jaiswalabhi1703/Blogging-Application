package com.blog.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.blog.config.AppProperties;

class JwtTokenHelperTest {

	private JwtTokenHelper jwtTokenHelper;
	private final UserDetails user = new User("alice@blog.com", "secret", List.of());

	@BeforeEach
	void setUp() {
		AppProperties props = new AppProperties();
		props.getJwt().setSecret("5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
		props.getJwt().setAccessTokenExpirationMs(900_000L);
		props.getJwt().setRefreshTokenExpirationMs(604_800_000L);
		jwtTokenHelper = new JwtTokenHelper(props);
	}

	@Test
	void accessTokenCarriesSubjectAndType() {
		String token = jwtTokenHelper.generateAccessToken(user);

		assertThat(jwtTokenHelper.getUsernameFromToken(token)).isEqualTo("alice@blog.com");
		assertThat(jwtTokenHelper.getTokenType(token)).isEqualTo(JwtTokenHelper.TYPE_ACCESS);
	}

	@Test
	void refreshTokenIsTaggedAsRefresh() {
		String token = jwtTokenHelper.generateRefreshToken("alice@blog.com");

		assertThat(jwtTokenHelper.getTokenType(token)).isEqualTo(JwtTokenHelper.TYPE_REFRESH);
	}

	@Test
	void validateTokenAcceptsMatchingUser() {
		String token = jwtTokenHelper.generateAccessToken(user);

		assertThat(jwtTokenHelper.validateToken(token, user)).isTrue();
	}

	@Test
	void validateTokenRejectsDifferentUser() {
		String token = jwtTokenHelper.generateAccessToken(user);
		UserDetails other = new User("bob@blog.com", "secret", List.of());

		assertThat(jwtTokenHelper.validateToken(token, other)).isFalse();
	}
}
