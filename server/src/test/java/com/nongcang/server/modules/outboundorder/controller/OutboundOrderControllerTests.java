package com.nongcang.server.modules.outboundorder.controller;

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
class OutboundOrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnOutboundOrderList() throws Exception {
		mockMvc.perform(get("/api/outbound-order/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("orderCode", "OUT-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].orderCode").value("OUT-202604190001"));
	}

	@Test
	void shouldCreateOutboundOrder() throws Exception {
		mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-22T09:00:00",
							  "remarks": "接口测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 10.5,
							      "sortOrder": 1,
							      "remarks": "首行"
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.orderCode").value(Matchers.startsWith("OUT-")))
				.andExpect(jsonPath("$.data.totalItemCount").value(1))
				.andExpect(jsonPath("$.data.totalQuantity").value(10.5));
	}

	@Test
	void shouldUpdateOutboundOrder() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 2,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-22T11:00:00",
							  "remarks": "更新前",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 5,
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

		mockMvc.perform(put("/api/outbound-order/" + outboundOrderId)
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 1,
							  "expectedDeliveryAt": "2026-04-23T10:30:00",
							  "remarks": "更新后",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 9,
							      "sortOrder": 1,
							      "remarks": "更新行"
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.customerId").value("1"))
				.andExpect(jsonPath("$.data.warehouseId").value("1"))
				.andExpect(jsonPath("$.data.totalQuantity").value(9.0));
	}

	@Test
	void shouldCancelOutboundOrder() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/outbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "customerId": 1,
							  "warehouseId": 2,
							  "expectedDeliveryAt": "2026-04-22T15:00:00",
							  "remarks": "取消测试",
							  "items": [
							    {
							      "productId": 3,
							      "quantity": 6,
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

		mockMvc.perform(patch("/api/outbound-order/" + outboundOrderId + "/cancel")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("出库单已取消"));
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
