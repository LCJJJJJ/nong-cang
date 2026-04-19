package com.nongcang.server.modules.warehouse.controller;

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
class WarehouseControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnWarehouseList() throws Exception {
		mockMvc.perform(get("/api/warehouse/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("warehouseCode", "WH-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].warehouseCode").value("WH-202604190001"))
				.andExpect(jsonPath("$.data[0].warehouseName").value("一号综合仓"));
	}

	@Test
	void shouldCreateWarehouse() throws Exception {
		mockMvc.perform(post("/api/warehouse")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseName": "测试仓库",
							  "warehouseType": "综合仓",
							  "managerName": "张三",
							  "contactPhone": "13800000001",
							  "address": "山东省寿光市测试路1号",
							  "status": 1,
							  "sortOrder": 30,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.warehouseCode").value(Matchers.startsWith("WH-")))
				.andExpect(jsonPath("$.data.warehouseName").value("测试仓库"));
	}

	@Test
	void shouldUpdateWarehouseStatus() throws Exception {
		mockMvc.perform(patch("/api/warehouse/2/status")
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
	void shouldDeleteWarehouse() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/warehouse")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "warehouseName": "删除测试仓库",
							  "warehouseType": "综合仓",
							  "managerName": "李四",
							  "contactPhone": "13800000002",
							  "address": "测试地址",
							  "status": 1,
							  "sortOrder": 90,
							  "remarks": "删除测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String warehouseId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(delete("/api/warehouse/" + warehouseId).header(HttpHeaders.AUTHORIZATION, bearerToken()))
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
