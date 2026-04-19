package com.nongcang.server.modules.inventoryadjustment.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class InventoryAdjustmentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnInventoryAdjustmentList() throws Exception {
		mockMvc.perform(get("/api/inventory-adjustment/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("adjustmentCode", "ADJ-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].adjustmentCode").value("ADJ-202604190001"));
	}

	@Test
	void shouldCreateIncreaseAdjustment() throws Exception {
		mockMvc.perform(post("/api/inventory-adjustment")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 1,
							  "zoneId": 1,
							  "locationId": 1,
							  "productId": 1,
							  "adjustmentType": "INCREASE",
							  "quantity": 1.5,
							  "reason": "人工补录",
							  "remarks": "测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.adjustmentCode").value(Matchers.startsWith("ADJ-")))
				.andExpect(jsonPath("$.data.adjustmentType").value("INCREASE"))
				.andExpect(jsonPath("$.data.quantity").value(1.5));
	}

	@Test
	void shouldRejectDecreaseWhenAvailableInsufficient() throws Exception {
		mockMvc.perform(post("/api/inventory-adjustment")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 2,
							  "zoneId": 2,
							  "locationId": 2,
							  "productId": 3,
							  "adjustmentType": "DECREASE",
							  "quantity": 999,
							  "reason": "人工纠错",
							  "remarks": "测试"
							}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("INVENTORY_ADJUSTMENT_STOCK_INSUFFICIENT"));
	}

	@Test
	void shouldRejectDecimalQuantityForIntegerUnitInventoryAdjustment() throws Exception {
		mockMvc.perform(post("/api/inventory-adjustment")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 11,
							  "zoneId": 9,
							  "locationId": 9,
							  "productId": 12,
							  "adjustmentType": "INCREASE",
							  "quantity": 1.5,
							  "reason": "人工补录",
							  "remarks": "精度测试"
							}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("QUANTITY_PRECISION_INVALID"));
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
