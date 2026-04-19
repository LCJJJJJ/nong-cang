package com.nongcang.server.modules.putawaytask.controller;

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
class PutawayTaskControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnPutawayTaskList() throws Exception {
		mockMvc.perform(get("/api/putaway-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("taskCode", "PT-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].taskCode").value("PT-202604190001"));
	}

	@Test
	void shouldAssignPutawayTask() throws Exception {
		mockMvc.perform(patch("/api/putaway-task/1/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 2,
							  "locationId": 2
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("分配库位成功"))
				.andExpect(jsonPath("$.data.zoneId").value("2"))
				.andExpect(jsonPath("$.data.locationId").value("2"));
	}

	@Test
	void shouldCompletePutawayTask() throws Exception {
		mockMvc.perform(patch("/api/putaway-task/1/assign")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "zoneId": 2,
							  "locationId": 2
							}
							"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/putaway-task/1/complete")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("上架完成"));
	}

	@Test
	void shouldCancelPutawayTask() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/inbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "supplierId": 1,
							  "warehouseId": 1,
							  "expectedArrivalAt": "2026-04-22T09:00:00",
							  "remarks": "生成取消任务",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 10,
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

		MvcResult listResult = mockMvc.perform(get("/api/putaway-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].taskCode").isNotEmpty())
				.andReturn();

		String putawayTaskId = objectMapper.readTree(listResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/putaway-task/" + putawayTaskId + "/cancel")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("任务已取消"));
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
