package com.nongcang.server.modules.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldLoginSuccessfully() throws Exception {
		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "admin",
							  "password": "Admin@123456",
							  "rememberMe": true
							}
							"""))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("登录成功"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.data.user.userId").value("1"))
				.andExpect(jsonPath("$.data.user.username").value("admin"))
				.andExpect(jsonPath("$.data.user.roleCode").value("ADMIN"))
				.andExpect(jsonPath("$.data.user.warehouseId").isEmpty());
	}

	@Test
	void shouldLoginWarehouseAdminSuccessfully() throws Exception {
		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "warehouse_admin",
							  "password": "Warehouse@123456"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.user.username").value("warehouse_admin"))
				.andExpect(jsonPath("$.data.user.roleCode").value("WAREHOUSE_ADMIN"))
				.andExpect(jsonPath("$.data.user.warehouseId").isNotEmpty())
				.andExpect(jsonPath("$.data.user.warehouseName").isNotEmpty());
	}

	@Test
	void shouldRejectInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "admin",
							  "password": "wrong-password"
							}
							"""))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.message").value("账号或密码错误"));
	}

	@Test
	void shouldRefreshTokenSuccessfully() throws Exception {
		String refreshToken = loginAndRead("refreshToken");

		mockMvc.perform(post("/api/auth/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "refreshToken": "%s"
							}
							""".formatted(refreshToken)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("刷新成功"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
	}

	@Test
	void shouldReturnCurrentUserWhenAuthorized() throws Exception {
		String accessToken = loginAndRead("accessToken");

		mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.userId").value("1"))
				.andExpect(jsonPath("$.data.username").value("admin"))
				.andExpect(jsonPath("$.data.displayName").value("系统管理员"))
				.andExpect(jsonPath("$.data.roleCode").value("ADMIN"));
	}

	private String loginAndRead(String fieldName) throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "admin",
							  "password": "Admin@123456"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
		return jsonNode.path("data").path(fieldName).asText();
	}
}
