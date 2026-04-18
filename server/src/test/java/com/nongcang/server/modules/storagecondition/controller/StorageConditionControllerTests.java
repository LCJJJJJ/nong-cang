package com.nongcang.server.modules.storagecondition.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StorageConditionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnStorageConditionList() throws Exception {
		mockMvc.perform(get("/api/storage-condition/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("conditionCode", "SC-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].conditionCode").value("SC-202604190001"));
	}

	@Test
	void shouldCreateStorageCondition() throws Exception {
		mockMvc.perform(post("/api/storage-condition")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "conditionName": "恒温花卉标准",
							  "storageType": "恒温",
							  "temperatureMin": 8.0,
							  "temperatureMax": 12.0,
							  "humidityMin": 65.0,
							  "humidityMax": 75.0,
							  "lightRequirement": "需避强光",
							  "ventilationRequirement": "普通通风",
							  "status": 1,
							  "sortOrder": 40,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.conditionCode").value(Matchers.startsWith("SC-")))
				.andExpect(jsonPath("$.data.conditionName").value("恒温花卉标准"));
	}

	@Test
	void shouldUpdateStorageConditionStatus() throws Exception {
		mockMvc.perform(patch("/api/storage-condition/1/status")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "status": 0
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("状态更新成功"));
	}

	@Test
	void shouldDeleteStorageCondition() throws Exception {
		mockMvc.perform(delete("/api/storage-condition/3").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("删除成功"));
	}

	private String bearerToken() throws Exception {
		String response = mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "admin",
							  "password": "Admin@123456"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode jsonNode = objectMapper.readTree(response);
		return "Bearer " + jsonNode.path("data").path("accessToken").asText();
	}
}
