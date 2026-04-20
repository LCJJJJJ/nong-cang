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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
	void shouldRecalculateActiveInventoryBatchWhenShelfLifeUpdated() throws Exception {
		MvcResult createInboundOrderResult = mockMvc.perform(post("/api/inbound-order")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "supplierId": 1,
							  "warehouseId": 1,
							  "expectedArrivalAt": "2026-04-23T09:00:00",
							  "remarks": "批次重算测试",
							  "items": [
							    {
							      "productId": 1,
							      "quantity": 6,
							      "sortOrder": 1,
							      "remarks": "测试"
							    }
							  ]
							}
							"""))
				.andExpect(status().isOk())
				.andReturn();

		String inboundOrderId = objectMapper.readTree(createInboundOrderResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/inbound-order/" + inboundOrderId + "/arrive")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		MvcResult putawayTaskResult = mockMvc.perform(get("/api/putaway-task/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String taskId = objectMapper.readTree(putawayTaskResult.getResponse().getContentAsString())
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

		MvcResult inboundRecordResult = mockMvc.perform(get("/api/inbound-record/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("orderCode", objectMapper.readTree(createInboundOrderResult.getResponse().getContentAsString())
							.path("data")
							.path("orderCode")
							.asText()))
				.andExpect(status().isOk())
				.andReturn();

		String inboundRecordId = objectMapper.readTree(inboundRecordResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		MvcResult productDetailResult = mockMvc.perform(get("/api/product-archive/1")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode productDetail = objectMapper.readTree(productDetailResult.getResponse().getContentAsString()).path("data");

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/product-archive/1")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "productName": "%s",
							  "productSpecification": %s,
							  "categoryId": %s,
							  "unitId": %s,
							  "originId": %s,
							  "storageConditionId": %s,
							  "shelfLifeDays": 9,
							  "warningDays": 3,
							  "qualityGradeId": %s,
							  "status": %s,
							  "sortOrder": %s,
							  "remarks": %s
							}
							""".formatted(
								escapeJson(productDetail.path("productName").asText()),
								escapeNullableJson(productDetail.path("productSpecification").isNull()
										? null
										: productDetail.path("productSpecification").asText()),
								productDetail.path("categoryId").asText(),
								productDetail.path("unitId").asText(),
								productDetail.path("originId").asText(),
								productDetail.path("storageConditionId").asText(),
								productDetail.path("qualityGradeId").asText(),
								productDetail.path("status").asText(),
								productDetail.path("sortOrder").asText(),
								escapeNullableJson(productDetail.path("remarks").isNull()
										? null
										: productDetail.path("remarks").asText()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.shelfLifeDays").value(9))
				.andExpect(jsonPath("$.data.warningDays").value(3));

		Integer shelfLifeDaysSnapshot = namedParameterJdbcTemplate.queryForObject("""
				SELECT shelf_life_days_snapshot
				FROM inventory_batch
				WHERE source_type = 'INBOUND_RECORD'
				  AND source_id = :sourceId
				""", new MapSqlParameterSource("sourceId", inboundRecordId), Integer.class);
		Integer warningDaysSnapshot = namedParameterJdbcTemplate.queryForObject("""
				SELECT warning_days_snapshot
				FROM inventory_batch
				WHERE source_type = 'INBOUND_RECORD'
				  AND source_id = :sourceId
				""", new MapSqlParameterSource("sourceId", inboundRecordId), Integer.class);

		org.junit.jupiter.api.Assertions.assertEquals(9, shelfLifeDaysSnapshot);
		org.junit.jupiter.api.Assertions.assertEquals(3, warningDaysSnapshot);
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

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private String escapeNullableJson(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + escapeJson(value) + "\"";
	}
}
