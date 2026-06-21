package com.blog.payloads;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class JwtAuthRequest {

	@NotEmpty(message = "username (email) is required")
	private String username; // email

	@NotEmpty(message = "password is required")
	private String password;
}
