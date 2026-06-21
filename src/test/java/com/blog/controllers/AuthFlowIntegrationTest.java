package com.blog.controllers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void registerLoginAndAccessProtectedResource() throws Exception {
		String email = "tester@blog.com";
		String registerBody = """
				{"name":"Tester","email":"%s","password":"Password@1","about":"integration test user"}
				""".formatted(email);

		// 1. Register
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON).content(registerBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				// password must never be serialized back
				.andExpect(jsonPath("$.password").doesNotExist());

		// 2. Login -> tokens
		String loginBody = """
				{"username":"%s","password":"Password@1"}
				""".formatted(email);

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON).content(loginBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.refreshToken", notNullValue()))
				.andReturn();

		JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
		String accessToken = tokens.get("accessToken").asText();
		String refreshToken = tokens.get("refreshToken").asText();

		// 3. Protected endpoint without a token -> 401
		mockMvc.perform(get("/api/users/"))
				.andExpect(status().isUnauthorized());

		// 4. Protected endpoint with the access token -> 200
		mockMvc.perform(get("/api/users/").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		// 5. Refresh -> a brand new access token
		String refreshRequest = """
				{"refreshToken":"%s"}
				""".formatted(refreshToken);

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON).content(refreshRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()));
	}

	@Test
	void loginWithBadCredentialsIsRejected() throws Exception {
		String loginBody = """
				{"username":"nobody@blog.com","password":"wrongpass1"}
				""";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON).content(loginBody))
				.andExpect(status().is4xxClientError());
	}
}
