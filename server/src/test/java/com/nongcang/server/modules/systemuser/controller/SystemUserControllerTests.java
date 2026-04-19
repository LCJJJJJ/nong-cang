package com.nongcang.server.modules.systemuser.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SystemUserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnSystemUserListForAdmin() throws Exception {
		mockMvc.perform(get("/api/system-user/list")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.data[0].roleCode").isNotEmpty());
	}

	@Test
	void shouldCreateSystemUser() throws Exception {
		mockMvc.perform(post("/api/system-user")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "username": "qa_user_test",
							  "displayName": "质检测试员",
							  "phone": "13800000199",
							  "roleCode": "QUALITY_ADMIN",
							  "warehouseId": 1,
							  "status": 1,
							  "initialPassword": "QaTest@123456",
							  "remarks": "接口测试用户"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.username").value("qa_user_test"))
				.andExpect(jsonPath("$.data.roleCode").value("QUALITY_ADMIN"))
				.andExpect(jsonPath("$.data.warehouseId").value("1"));
	}

	@Test
	void shouldUpdateStatusAndResetPasswordAndDeleteSystemUser() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/system-user")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "username": "inventory_user_test",
							  "displayName": "库存测试员",
							  "phone": "13800000198",
							  "roleCode": "INVENTORY_ADMIN",
							  "warehouseId": 1,
							  "status": 1,
							  "initialPassword": "Inventory@123456",
							  "remarks": "待更新用户"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(put("/api/system-user/" + userId)
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "displayName": "库存测试员已更新",
							  "phone": "13800000198",
							  "roleCode": "INVENTORY_ADMIN",
							  "warehouseId": 1,
							  "status": 1,
							  "remarks": "已更新"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.displayName").value("库存测试员已更新"));

		mockMvc.perform(patch("/api/system-user/" + userId + "/status")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "status": 0
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("状态更新成功"));

		mockMvc.perform(patch("/api/system-user/" + userId + "/reset-password")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "newPassword": "Inventory@654321"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("密码重置成功"));

		mockMvc.perform(delete("/api/system-user/" + userId)
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("删除成功"));
	}

	@Test
	void shouldRejectSystemUserListForNonAdmin() throws Exception {
		mockMvc.perform(get("/api/system-user/list")
					.header(HttpHeaders.AUTHORIZATION, warehouseAdminBearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private String adminBearerToken() throws Exception {
		return bearerToken("admin", "Admin@123456");
	}

	private String warehouseAdminBearerToken() throws Exception {
		return bearerToken("warehouse_admin", "Warehouse@123456");
	}

	private String bearerToken(String account, String password) throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "%s",
							  "password": "%s"
							}
							""".formatted(account, password)))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
		return "Bearer " + jsonNode.path("data").path("accessToken").asText();
	}
}
