package com.nongcang.server.modules.inventorystock.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class InventoryStockControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnInventoryStockList() throws Exception {
		mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].productCode").isNotEmpty())
				.andExpect(jsonPath("$.data[0].stockQuantity").isNumber())
				.andExpect(jsonPath("$.data[0].nearestExpireAt").isNotEmpty())
				.andExpect(jsonPath("$.data[0].remainingShelfLifeDays").isNumber());
	}

	@Test
	void shouldShowReservedQuantityAfterOutboundAssigned() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-25T09:00:00",
							  "remarks": "库存查询测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 2,
							      "sortOrder": 1,
							      "remarks": ""
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String outboundOrderId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/outbound-order/" + outboundOrderId + "/dispatch")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		MvcResult taskListResult = mockMvc.perform(get("/api/outbound-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String taskId = findTaskIdByOrderId(
				objectMapper.readTree(taskListResult.getResponse().getContentAsString()).path("data"),
				outboundOrderId);

		mockMvc.perform(patch("/api/outbound-task/" + taskId + "/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 2,
							  "locationId": 2
							}
							"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "3")
					.param("warehouseId", "2")
					.param("zoneId", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].reservedQuantity").value(2.0))
				.andExpect(jsonPath("$.data[0].availableQuantity").value(Matchers.greaterThan(0.0)));
	}

	@Test
	void shouldShowLockedQuantityAfterQualityInspection() throws Exception {
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
							  "unqualifiedQuantity": 3,
							  "remarks": "锁定异常库存"
							}
							""".formatted(stockId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "1")
					.param("warehouseId", "1")
					.param("zoneId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].lockedQuantity").value(3.0))
				.andExpect(jsonPath("$.data[0].availableQuantity").value(Matchers.greaterThan(0.0)));
	}

	private String findTaskIdByOrderId(JsonNode taskList, String outboundOrderId) {
		for (JsonNode task : taskList) {
			if (outboundOrderId.equals(task.path("outboundOrderId").asText())) {
				return task.path("id").asText();
			}
		}

		throw new IllegalStateException("未找到对应的拣货出库任务");
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
