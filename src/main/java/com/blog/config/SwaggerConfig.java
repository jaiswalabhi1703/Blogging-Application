package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI / Swagger UI definition with a global JWT bearer security scheme.
 * Browse the docs at {@code /swagger-ui.html}.
 */
@Configuration
public class SwaggerConfig {

	private static final String SECURITY_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI blogOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Blogging Application API")
						.description("A production-grade blogging REST API with JWT authentication, "
								+ "refresh tokens, role-based access control and an OpenAI-powered authoring assistant.")
						.version("1.0.0")
						.contact(new Contact().name("Blog App").email("support@blog.com"))
						.license(new License().name("MIT")))
				.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
				.components(new Components().addSecuritySchemes(SECURITY_SCHEME,
						new SecurityScheme()
								.name(SECURITY_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
