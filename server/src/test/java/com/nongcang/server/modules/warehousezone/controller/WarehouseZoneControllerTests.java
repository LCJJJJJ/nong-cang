package com.nongcang.server.modules.warehousezone.controller;

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
class WarehouseZoneControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnWarehouseZoneList() throws Exception {
		mockMvc.perform(get("/api/warehouse-zone/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("zoneCode", "ZONE-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].zoneCode").value("ZONE-202604190001"))
				.andExpect(jsonPath("$.data[0].zoneName").value("蔬菜收货区"));
	}

	@Test
	void shouldCreateWarehouseZone() throws Exception {
		mockMvc.perform(post("/api/warehouse-zone")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 1,
							  "zoneName": "测试库区",
							  "zoneType": "暂存区",
							  "temperatureMin": 2.0,
							  "temperatureMax": 8.0,
							  "status": 1,
							  "sortOrder": 50,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.zoneCode").value(Matchers.startsWith("ZONE-")))
				.andExpect(jsonPath("$.data.zoneName").value("测试库区"));
	}

	@Test
	void shouldReturnWarehouseZoneOptionsWithWarehouseId() throws Exception {
		mockMvc.perform(get("/api/warehouse-zone/options").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].warehouseId").isNotEmpty());
	}

	@Test
	void shouldUpdateWarehouseZoneStatus() throws Exception {
		mockMvc.perform(patch("/api/warehouse-zone/1/status")
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
	void shouldDeleteWarehouseZone() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/warehouse-zone")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseId": 1,
							  "zoneName": "删除测试库区",
							  "zoneType": "暂存区",
							  "temperatureMin": null,
							  "temperatureMax": null,
							  "status": 1,
							  "sortOrder": 90,
							  "remarks": "删除测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String warehouseZoneId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(delete("/api/warehouse-zone/" + warehouseZoneId).header(HttpHeaders.AUTHORIZATION, bearerToken()))
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
