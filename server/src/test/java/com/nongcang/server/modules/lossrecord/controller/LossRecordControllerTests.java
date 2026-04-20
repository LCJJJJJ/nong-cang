package com.nongcang.server.modules.lossrecord.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class LossRecordControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	void shouldCreateDirectLossRecord() throws Exception {
		Double beforeRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 1
				  AND warehouse_id = 1
				  AND location_id = 1
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);

		mockMvc.perform(post("/api/loss-record/direct")
					.header("Authorization", bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 1,
							  "zoneId": 1,
							  "locationId": 1,
							  "productId": 1,
							  "quantity": 1.5,
							  "lossReason": "破损报废",
							  "remarks": "direct"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.lossCode").value(Matchers.startsWith("LOSS-")))
				.andExpect(jsonPath("$.data.sourceType").value("DIRECT"))
				.andExpect(jsonPath("$.data.quantity").value(1.5));

		Double afterRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 1
				  AND warehouse_id = 1
				  AND location_id = 1
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);
		org.junit.jupiter.api.Assertions.assertEquals(beforeRemainingQuantity - 1.5D, afterRemainingQuantity);
	}

	@Test
	void shouldReturnLossRecordList() throws Exception {
		mockMvc.perform(get("/api/loss-record/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
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
