package com.blog.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.config.AppProperties;
import com.blog.entities.RefreshToken;
import com.blog.exceptions.ApiException;
import com.blog.payloads.ApiResponse;
import com.blog.payloads.JwtAuthRequest;
import com.blog.payloads.JwtAuthResponse;
import com.blog.payloads.RefreshTokenRequest;
import com.blog.payloads.UserDto;
import com.blog.security.JwtTokenHelper;
import com.blog.service.RefreshTokenService;
import com.blog.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login and token-refresh endpoints")
public class AuthController {

	private final JwtTokenHelper jwtTokenHelper;
	private final UserDetailsService userDetailsService;
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final long accessTokenExpirationSeconds;

	public AuthController(JwtTokenHelper jwtTokenHelper, UserDetailsService userDetailsService,
			AuthenticationManager authenticationManager, UserService userService,
			RefreshTokenService refreshTokenService, AppProperties appProperties) {
		this.jwtTokenHelper = jwtTokenHelper;
		this.userDetailsService = userDetailsService;
		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.refreshTokenService = refreshTokenService;
		this.accessTokenExpirationSeconds = appProperties.getJwt().getAccessTokenExpirationMs() / 1000;
	}

	@Operation(summary = "Register a new user")
	@PostMapping("/register")
	public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
		UserDto registeredUser = userService.registerNewUser(userDto);
		return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
	}

	@Operation(summary = "Authenticate and obtain access + refresh tokens")
	@PostMapping("/login")
	public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody JwtAuthRequest request) {
		authenticate(request.getUsername(), request.getPassword());

		UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
		String accessToken = jwtTokenHelper.generateAccessToken(userDetails);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

		return ResponseEntity.ok(buildResponse(accessToken, refreshToken.getToken(), userDetails.getUsername()));
	}

	@Operation(summary = "Exchange a valid refresh token for a new access token (token rotation)")
	@PostMapping("/refresh")
	public ResponseEntity<JwtAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		RefreshToken stored = refreshTokenService.verifyValidToken(request.getRefreshToken());
		String email = stored.getUser().getEmail();

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		String accessToken = jwtTokenHelper.generateAccessToken(userDetails);
		// Rotate the refresh token on every use to limit replay.
		RefreshToken rotated = refreshTokenService.createRefreshToken(email);

		return ResponseEntity.ok(buildResponse(accessToken, rotated.getToken(), email));
	}

	@Operation(summary = "Revoke the caller's refresh tokens (logout)")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
		RefreshToken stored = refreshTokenService.verifyValidToken(request.getRefreshToken());
		refreshTokenService.revokeForUser(stored.getUser().getEmail());
		return ResponseEntity.ok(new ApiResponse("Logged out successfully", true));
	}

	private JwtAuthResponse buildResponse(String accessToken, String refreshToken, String username) {
		return JwtAuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.username(username)
				.expiresIn(accessTokenExpirationSeconds)
				.build();
	}

	private void authenticate(String username, String password) {
		try {
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
			authenticationManager.authenticate(authentication);
		} catch (BadCredentialsException ex) {
			throw new ApiException("Invalid username or password");
		}
	}
}
