package com.nongcang.server.modules.inboundorder.controller;

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
class InboundOrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnInboundOrderList() throws Exception {
		mockMvc.perform(get("/api/inbound-order/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("orderCode", "IN-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].orderCode").value("IN-202604190001"));
	}

	@Test
	void shouldCreateInboundOrder() throws Exception {
		mockMvc.perform(post("/api/inbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "supplierId": 1,
							  "warehouseId": 1,
							  "expectedArrivalAt": "2026-04-21T09:00:00",
							  "remarks": "接口测试",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 12.5,
							      "sortOrder": 1,
							      "remarks": "首行"
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.orderCode").value(Matchers.startsWith("IN-")))
				.andExpect(jsonPath("$.data.totalItemCount").value(1))
				.andExpect(jsonPath("$.data.totalQuantity").value(12.5));
	}

	@Test
	void shouldConfirmArrival() throws Exception {
		mockMvc.perform(patch("/api/inbound-order/1/arrive").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("到货确认成功"));
	}

	@Test
	void shouldCancelInboundOrder() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/inbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "supplierId": 1,
							  "warehouseId": 1,
							  "expectedArrivalAt": "2026-04-21T10:00:00",
							  "remarks": "取消测试",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 6,
							      "sortOrder": 1,
							      "remarks": ""
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String inboundOrderId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/inbound-order/" + inboundOrderId + "/cancel")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("入库单已取消"));
	}

	@Test
	void shouldReturnProductArchiveOptions() throws Exception {
		mockMvc.perform(get("/api/product-archive/options").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].label").isNotEmpty())
				.andExpect(jsonPath("$.data[0].unitName").isNotEmpty());
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
