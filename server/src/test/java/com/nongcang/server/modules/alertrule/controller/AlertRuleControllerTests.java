package com.nongcang.server.modules.alertrule.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AlertRuleControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnAlertRuleList() throws Exception {
		mockMvc.perform(get("/api/alert-rule/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.data[0].ruleCode").isNotEmpty())
				.andExpect(jsonPath("$.data[?(@.ruleCode=='AR-NEAR-EXPIRY')]").isNotEmpty())
				.andExpect(jsonPath("$.data[?(@.ruleCode=='AR-EXPIRED')]").isNotEmpty());
	}

	@Test
	void shouldUpdateAlertRule() throws Exception {
		mockMvc.perform(put("/api/alert-rule/1")
					.header("Authorization", bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "severity": "HIGH",
							  "thresholdValue": 8,
							  "description": "测试更新",
							  "sortOrder": 11
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.severity").value("HIGH"))
				.andExpect(jsonPath("$.data.thresholdValue").value(8.0));
	}

	@Test
	void shouldToggleAlertRuleStatus() throws Exception {
		mockMvc.perform(patch("/api/alert-rule/1/status")
					.header("Authorization", bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "enabled": 0
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("状态更新成功"));
	}

	private String bearerToken() throws Exception {
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
		return "Bearer " + jsonNode.path("data").path("accessToken").asText();
	}
}
