package com.nongcang.server.modules.abnormalstock.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
class AbnormalStockControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	void shouldReturnAbnormalStockOptionsAfterInspection() throws Exception {
		MvcResult stockResult = mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "1")
					.param("warehouseId", "1")
					.param("zoneId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		String stockId = objectMapper.readTree(stockResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INVENTORY_STOCK",
							  "sourceId": %s,
							  "inspectQuantity": 8,
							  "unqualifiedQuantity": 2,
							  "remarks": "异常库存测试"
							}
							""".formatted(stockId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/abnormal-stock/options")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].lockedQuantity").value(2.0));

		Integer batchLockCount = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM abnormal_stock_batch_lock
				""", new MapSqlParameterSource(), Integer.class);
		org.junit.jupiter.api.Assertions.assertEquals(1, batchLockCount);
	}

	@Test
	void shouldReleaseAbnormalStock() throws Exception {
		String abnormalStockId = createAbnormalStock();

		mockMvc.perform(patch("/api/abnormal-stock/" + abnormalStockId + "/release")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("异常库存已释放"));
	}

	@Test
	void shouldDisposeAbnormalStockToLoss() throws Exception {
		String abnormalStockId = createAbnormalStock();
		Double beforeRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 1
				  AND warehouse_id = 1
				  AND location_id = 1
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);

		mockMvc.perform(post("/api/abnormal-stock/" + abnormalStockId + "/dispose-loss")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "lossReason": "腐坏报废",
							  "remarks": "转损耗测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("异常库存已转损耗"));

		Double afterRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 1
				  AND warehouse_id = 1
				  AND location_id = 1
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);
		org.junit.jupiter.api.Assertions.assertEquals(beforeRemainingQuantity - 2D, afterRemainingQuantity);
	}

	private String createAbnormalStock() throws Exception {
		MvcResult stockResult = mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "1")
					.param("warehouseId", "1")
					.param("zoneId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		String stockId = objectMapper.readTree(stockResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INVENTORY_STOCK",
							  "sourceId": %s,
							  "inspectQuantity": 8,
							  "unqualifiedQuantity": 2,
							  "remarks": "异常库存测试"
							}
							""".formatted(stockId)))
				.andExpect(status().isOk());

		MvcResult optionsResult = mockMvc.perform(get("/api/abnormal-stock/options")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(optionsResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();
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
