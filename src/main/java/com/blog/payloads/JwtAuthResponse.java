package com.blog.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

	private String accessToken;
	private String refreshToken;
	@Builder.Default
	private String tokenType = "Bearer";
	private String username;
	/** Access-token lifetime in seconds. */
	private long expiresIn;
}
