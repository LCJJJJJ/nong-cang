package com.nongcang.server.modules.qualityinspection.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class QualityInspectionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldCreateInspectionFromInventoryStockAndGenerateAbnormalStock() throws Exception {
		MvcResult stockResult = mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "1")
					.param("warehouseId", "1")
					.param("zoneId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		String stockId = objectMapper.readTree(stockResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INVENTORY_STOCK",
							  "sourceId": %s,
							  "inspectQuantity": 10,
							  "unqualifiedQuantity": 3,
							  "remarks": "抽检发现异常"
							}
							""".formatted(stockId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.inspectionCode").value(Matchers.startsWith("QI-")))
				.andExpect(jsonPath("$.data.resultStatus").value(2))
				.andExpect(jsonPath("$.data.unqualifiedQuantity").value(3.0));

		mockMvc.perform(get("/api/abnormal-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].lockedQuantity").value(3.0));
	}

	@Test
	void shouldRejectInspectionWhenSourceAvailableInsufficient() throws Exception {
		MvcResult stockResult = mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("productId", "1")
					.param("warehouseId", "1")
					.param("zoneId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		String stockId = objectMapper.readTree(stockResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INVENTORY_STOCK",
							  "sourceId": %s,
							  "inspectQuantity": 999,
							  "unqualifiedQuantity": 1,
							  "remarks": "超量送检"
							}
							""".formatted(stockId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUALITY_INSPECTION_SOURCE_INSUFFICIENT"));
	}

	@Test
	void shouldRejectDecimalInspectionForIntegerUnitInboundRecord() throws Exception {
		MvcResult recordResult = mockMvc.perform(get("/api/inbound-record/list")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.param("recordCode", "IR-20260419121132300"))
				.andExpect(status().isOk())
				.andReturn();

		String recordId = objectMapper.readTree(recordResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INBOUND_RECORD",
							  "sourceId": %s,
							  "inspectQuantity": 1.5,
							  "unqualifiedQuantity": 0,
							  "remarks": "整数单位精度测试"
							}
							""".formatted(recordId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUANTITY_PRECISION_INVALID"));
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
