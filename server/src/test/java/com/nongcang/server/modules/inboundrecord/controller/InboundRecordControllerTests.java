package com.nongcang.server.modules.inboundrecord.controller;

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
class InboundRecordControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnInboundRecordList() throws Exception {
		mockMvc.perform(get("/api/inbound-record/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("recordCode", "IR-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].recordCode").value("IR-202604190001"));
	}

	@Test
	void shouldGenerateInboundRecordAfterPutawayCompleted() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/inbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "supplierId": 1,
							  "warehouseId": 1,
							  "expectedArrivalAt": "2026-04-23T09:00:00",
							  "remarks": "记录测试",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 8,
							      "sortOrder": 1,
							      "remarks": "测试"
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

		mockMvc.perform(patch("/api/inbound-order/" + inboundOrderId + "/arrive")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		MvcResult taskListResult = mockMvc.perform(get("/api/putaway-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String taskId = objectMapper.readTree(taskListResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/putaway-task/" + taskId + "/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 1,
							  "locationId": 1
							}
							"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/putaway-task/" + taskId + "/complete")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/inbound-record/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("orderCode", objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("orderCode").asText()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].inboundOrderId").isNotEmpty());
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
