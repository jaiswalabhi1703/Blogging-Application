package com.blog.payloads;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RefreshTokenRequest {

	@NotEmpty(message = "refreshToken is required")
	private String refreshToken;
}
