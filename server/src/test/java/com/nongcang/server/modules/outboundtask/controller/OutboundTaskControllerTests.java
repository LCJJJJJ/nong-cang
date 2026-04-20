package com.nongcang.server.modules.outboundtask.controller;

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
class OutboundTaskControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	void shouldReturnOutboundTaskList() throws Exception {
		mockMvc.perform(get("/api/outbound-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("taskCode", "OT-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].taskCode").value("OT-202604190001"));
	}

	@Test
	void shouldReturnStockOptions() throws Exception {
		mockMvc.perform(get("/api/outbound-task/1/stock-options")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].locationId").value("2"))
				.andExpect(jsonPath("$.data[0].availableQuantity").value(36.0));
	}

	@Test
	void shouldAssignPickAndCompleteOutboundTask() throws Exception {
		Double beforeRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 3
				  AND warehouse_id = 2
				  AND location_id = 2
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);

		MvcResult createResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-23T09:00:00",
							  "remarks": "出库任务测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 6,
							      "sortOrder": 1,
							      "remarks": "测试"
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
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("拣货任务已生成"));

		MvcResult taskListResult = mockMvc.perform(get("/api/outbound-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("taskCode", "OT-"))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode taskList = objectMapper.readTree(taskListResult.getResponse().getContentAsString()).path("data");
		String taskId = findTaskIdByOrderId(taskList, outboundOrderId);

		mockMvc.perform(patch("/api/outbound-task/" + taskId + "/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 2,
							  "locationId": 2
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("分配库存成功"));

		mockMvc.perform(patch("/api/outbound-task/" + taskId + "/pick")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("拣货完成"));

		mockMvc.perform(patch("/api/outbound-task/" + taskId + "/complete")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("出库完成"));

		Double afterRemainingQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(remaining_quantity), 0)
				FROM inventory_batch
				WHERE product_id = 3
				  AND warehouse_id = 2
				  AND location_id = 2
				  AND status = 'ACTIVE'
				""", new MapSqlParameterSource(), Double.class);
		Integer allocationCount = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM outbound_task_batch_allocation
				WHERE outbound_task_id = :outboundTaskId
				""", new MapSqlParameterSource("outboundTaskId", taskId), Integer.class);

		org.junit.jupiter.api.Assertions.assertEquals(beforeRemainingQuantity - 6D, afterRemainingQuantity);
		org.junit.jupiter.api.Assertions.assertEquals(1, allocationCount);
	}

	@Test
	void shouldCancelOutboundTask() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-23T10:00:00",
							  "remarks": "取消任务测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 4,
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

		JsonNode taskList = objectMapper.readTree(taskListResult.getResponse().getContentAsString()).path("data");
		String taskId = findTaskIdByOrderId(taskList, outboundOrderId);

		mockMvc.perform(patch("/api/outbound-task/" + taskId + "/cancel")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("任务已取消"));
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
