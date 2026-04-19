package com.nongcang.server.modules.inventorystocktaking.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class InventoryStocktakingControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnInventoryStocktakingList() throws Exception {
		mockMvc.perform(get("/api/inventory-stocktaking/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("stocktakingCode", "STK-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].stocktakingCode").value("STK-202604190001"));
	}

	@Test
	void shouldCreateSaveAndConfirmStocktaking() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/inventory-stocktaking")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 2,
							  "zoneId": 2,
							  "remarks": "盘点测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andReturn();

		JsonNode detail = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
		String stocktakingId = detail.path("id").asText();
		String itemId = detail.path("items").path(0).path("id").asText();

		mockMvc.perform(put("/api/inventory-stocktaking/" + stocktakingId + "/items")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "items": [
							    {
							      "itemId": %s,
							      "countedQuantity": 34,
							      "remarks": "盘亏测试"
							    }
							  ]
							}
							""".formatted(itemId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("盘点结果已保存"));

		mockMvc.perform(patch("/api/inventory-stocktaking/" + stocktakingId + "/confirm")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("盘点已确认"));
	}

	@Test
	void shouldRejectWhenCountBelowReservedQuantity() throws Exception {
		MvcResult createOrderResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-25T11:00:00",
							  "remarks": "预留测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 3,
							      "sortOrder": 1,
							      "remarks": ""
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String outboundOrderId = objectMapper.readTree(createOrderResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/outbound-order/" + outboundOrderId + "/dispatch")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		MvcResult outboundTaskResult = mockMvc.perform(get("/api/outbound-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String outboundTaskId = findOutboundTaskIdByOrderId(
				objectMapper.readTree(outboundTaskResult.getResponse().getContentAsString()).path("data"),
				outboundOrderId);

		mockMvc.perform(patch("/api/outbound-task/" + outboundTaskId + "/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 2,
							  "locationId": 2
							}
							"""))
				.andExpect(status().isOk());

		MvcResult createResult = mockMvc.perform(post("/api/inventory-stocktaking")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 2,
							  "zoneId": 2,
							  "remarks": "预留冲突测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode detail = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
		String stocktakingId = detail.path("id").asText();
		String itemId = detail.path("items").path(0).path("id").asText();

		mockMvc.perform(put("/api/inventory-stocktaking/" + stocktakingId + "/items")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "items": [
							    {
							      "itemId": %s,
							      "countedQuantity": 2,
							      "remarks": "少于预留"
							    }
							  ]
							}
							""".formatted(itemId)))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/inventory-stocktaking/" + stocktakingId + "/confirm")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_STOCKTAKING_RESERVED_CONFLICT"));
	}

	@Test
	void shouldRejectDecimalCountForIntegerUnitStocktaking() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/inventory-stocktaking")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 11,
							  "zoneId": 9,
							  "remarks": "整数单位盘点测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode detail = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
		String stocktakingId = detail.path("id").asText();
		String itemId = detail.path("items").path(0).path("id").asText();

		mockMvc.perform(put("/api/inventory-stocktaking/" + stocktakingId + "/items")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "items": [
							    {
							      "itemId": %s,
							      "countedQuantity": 47.5,
							      "remarks": "小数盘点"
							    }
							  ]
							}
							""".formatted(itemId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("QUANTITY_PRECISION_INVALID"));
	}

	private String findOutboundTaskIdByOrderId(JsonNode taskList, String outboundOrderId) {
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
