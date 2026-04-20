package com.nongcang.server.modules.alertrecord.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AlertRecordControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	void shouldRefreshAndListAlertRecords() throws Exception {
		mockMvc.perform(post("/api/alert-record/refresh")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.createdCount").isNumber());

		mockMvc.perform(get("/api/alert-record/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.data[0].alertCode").isNotEmpty());
	}

	@Test
	void shouldIgnoreAlertRecord() throws Exception {
		mockMvc.perform(post("/api/alert-record/refresh")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk());

		MvcResult listResult = mockMvc.perform(get("/api/alert-record/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String alertId = objectMapper.readTree(listResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/alert-record/" + alertId + "/ignore")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("已忽略"));
	}

	@Test
	void shouldGenerateNearExpiryAndExpiredAlertsFromInventoryBatch() throws Exception {
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_batch
				SET warning_at = DATE_SUB(NOW(), INTERVAL 1 HOUR),
				    expected_expire_at = DATE_ADD(NOW(), INTERVAL 1 DAY)
				WHERE id = 3
				""", new MapSqlParameterSource());
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_batch
				SET warning_at = DATE_SUB(NOW(), INTERVAL 3 DAY),
				    expected_expire_at = DATE_SUB(NOW(), INTERVAL 1 HOUR)
				WHERE id = 2
				""", new MapSqlParameterSource());

		mockMvc.perform(post("/api/alert-record/refresh")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/alert-record/list")
					.header("Authorization", bearerToken())
					.param("alertType", "NEAR_EXPIRY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.alertType=='NEAR_EXPIRY')]").isNotEmpty());

		mockMvc.perform(get("/api/alert-record/list")
					.header("Authorization", bearerToken())
					.param("alertType", "EXPIRED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.alertType=='EXPIRED')]").isNotEmpty());
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
