package com.nongcang.server.modules.productorigin.controller;

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
class ProductOriginControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnProductOriginList() throws Exception {
		mockMvc.perform(get("/api/product-origin/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("originCode", "ORI-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].originCode").value("ORI-202604190001"))
				.andExpect(jsonPath("$.data[0].originName").value("山东寿光"));
	}

	@Test
	void shouldCreateProductOrigin() throws Exception {
		mockMvc.perform(post("/api/product-origin")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "originName": "云南普洱",
							  "countryName": "中国",
							  "provinceName": "云南省",
							  "cityName": "普洱市",
							  "status": 1,
							  "sortOrder": 50,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.originCode").value(Matchers.startsWith("ORI-")))
				.andExpect(jsonPath("$.data.originName").value("云南普洱"));
	}

	@Test
	void shouldUpdateProductOriginStatus() throws Exception {
		mockMvc.perform(patch("/api/product-origin/1/status")
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
	void shouldDeleteProductOrigin() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/product-origin")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "originName": "删除测试产地",
							  "countryName": "中国",
							  "provinceName": "测试省",
							  "cityName": "测试市",
							  "status": 1,
							  "sortOrder": 90,
							  "remarks": "删除测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String productOriginId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(delete("/api/product-origin/" + productOriginId).header(HttpHeaders.AUTHORIZATION, bearerToken()))
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
