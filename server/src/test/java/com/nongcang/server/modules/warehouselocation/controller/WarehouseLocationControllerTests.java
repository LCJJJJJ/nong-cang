package com.nongcang.server.modules.warehouselocation.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class WarehouseLocationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnWarehouseLocationList() throws Exception {
		mockMvc.perform(get("/api/warehouse-location/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("locationCode", "LOC-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].locationCode").value("LOC-202604190001"))
				.andExpect(jsonPath("$.data[0].locationName").value("A-01-01"));
	}

	@Test
	void shouldCreateWarehouseLocation() throws Exception {
		mockMvc.perform(post("/api/warehouse-location")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 2,
							  "zoneId": 2,
							  "locationName": "C-03-01",
							  "locationType": "货架位",
							  "capacity": 20,
							  "status": 1,
							  "sortOrder": 60,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.locationCode").value(Matchers.startsWith("LOC-")))
				.andExpect(jsonPath("$.data.locationName").value("C-03-01"));
	}

	@Test
	void shouldUpdateWarehouseLocationStatus() throws Exception {
		mockMvc.perform(patch("/api/warehouse-location/1/status")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "status": 0
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("状态更新成功"));
	}

	@Test
	void shouldDeleteWarehouseLocation() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/warehouse-location")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 2,
							  "zoneId": 2,
							  "locationName": "删除测试库位",
							  "locationType": "货架位",
							  "capacity": 10,
							  "status": 1,
							  "sortOrder": 90,
							  "remarks": "删除测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String warehouseLocationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(delete("/api/warehouse-location/" + warehouseLocationId).header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("删除成功"));
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
