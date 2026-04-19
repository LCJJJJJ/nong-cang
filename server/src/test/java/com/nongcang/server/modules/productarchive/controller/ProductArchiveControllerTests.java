package com.nongcang.server.modules.productarchive.controller;

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
class ProductArchiveControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnProductArchiveList() throws Exception {
		mockMvc.perform(get("/api/product-archive/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productCode", "PROD-202604190001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].productCode").value("PROD-202604190001"))
				.andExpect(jsonPath("$.data[0].productName").value("菠菜鲜菜"))
				.andExpect(jsonPath("$.data[0].shelfLifeDays").value(5))
				.andExpect(jsonPath("$.data[0].warningDays").value(1));
	}

	@Test
	void shouldReturnProductArchiveOptions() throws Exception {
		mockMvc.perform(get("/api/product-archive/options").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].label").isNotEmpty())
				.andExpect(jsonPath("$.data[0].unitName").isNotEmpty());
	}

	@Test
	void shouldCreateProductArchive() throws Exception {
		mockMvc.perform(post("/api/product-archive")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "productName": "测试产品档案",
							  "productSpecification": "500g/袋",
							  "categoryId": 2,
							  "unitId": 1,
							  "originId": 1,
							  "storageConditionId": 1,
							  "shelfLifeDays": 6,
							  "warningDays": 2,
							  "qualityGradeId": 1,
							  "status": 1,
							  "sortOrder": 60,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.productCode").value(Matchers.startsWith("PROD-")))
				.andExpect(jsonPath("$.data.productName").value("测试产品档案"))
				.andExpect(jsonPath("$.data.shelfLifeDays").value(6))
				.andExpect(jsonPath("$.data.warningDays").value(2));
	}

	@Test
	void shouldUpdateProductArchiveStatus() throws Exception {
		mockMvc.perform(patch("/api/product-archive/1/status")
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
	void shouldDeleteProductArchive() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/product-archive")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "productName": "删除测试产品档案",
							  "productSpecification": "1kg/箱",
							  "categoryId": 2,
							  "unitId": 1,
							  "originId": 1,
							  "storageConditionId": 1,
							  "shelfLifeDays": 4,
							  "warningDays": 1,
							  "qualityGradeId": 1,
							  "status": 1,
							  "sortOrder": 90,
							  "remarks": "删除测试"
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String productArchiveId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(delete("/api/product-archive/" + productArchiveId).header(HttpHeaders.AUTHORIZATION, bearerToken()))
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
