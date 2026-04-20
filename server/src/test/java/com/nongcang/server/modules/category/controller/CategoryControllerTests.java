package com.nongcang.server.modules.category.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnCategoryTree() throws Exception {
		mockMvc.perform(get("/api/category/tree")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("categoryName", "叶菜类"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].categoryCode").value("CAT-A00"))
				.andExpect(jsonPath("$.data[0].children[0].categoryCode").value("CAT-A01"))
				.andExpect(jsonPath("$.data[0].children[0].defaultStorageConditionId").value("1"))
				.andExpect(jsonPath("$.data[0].children[0].defaultStorageCondition").value("叶菜冷藏标准"))
				.andExpect(jsonPath("$.data[0].children[0].defaultStorageType").value("冷藏"))
				.andExpect(jsonPath("$.data[0].children[0].shelfLifeDays").doesNotExist())
				.andExpect(jsonPath("$.data[0].children[0].warningDays").doesNotExist());
	}

	@Test
	void shouldCreateCategory() throws Exception {
		mockMvc.perform(post("/api/category")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "categoryName": "测试分类",
							  "parentId": null,
							  "sortOrder": 99,
							  "status": 1,
							  "defaultStorageConditionId": 1,
							  "remarks": "接口测试"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("新增成功"))
				.andExpect(jsonPath("$.data.categoryCode").value(org.hamcrest.Matchers.startsWith("CAT-")))
				.andExpect(jsonPath("$.data.categoryLevel").value(1))
				.andExpect(jsonPath("$.data.defaultStorageConditionId").value("1"))
				.andExpect(jsonPath("$.data.defaultStorageCondition").value("叶菜冷藏标准"))
				.andExpect(jsonPath("$.data.defaultStorageType").value("冷藏"))
				.andExpect(jsonPath("$.data.shelfLifeDays").doesNotExist())
				.andExpect(jsonPath("$.data.warningDays").doesNotExist());
	}

	@Test
	void shouldUpdateCategoryStatus() throws Exception {
		mockMvc.perform(patch("/api/category/2/status")
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
	void shouldRejectDeleteWhenCategoryHasChildren() throws Exception {
		mockMvc.perform(delete("/api/category/1").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("CATEGORY_HAS_CHILDREN"));
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
