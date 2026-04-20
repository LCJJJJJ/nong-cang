package com.nongcang.server.modules.security;

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
class WarehouseDataScopeControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldRestrictWarehouseAdminWarehouseListToAssignedWarehouse() throws Exception {
		mockMvc.perform(get("/api/warehouse/list")
					.header(HttpHeaders.AUTHORIZATION, warehouseAdminBearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data[0].id").value("1"))
				.andExpect(jsonPath("$.data[0].warehouseName").value("一号综合仓"));
	}

	@Test
	void shouldRejectInventoryAdminQueryingOtherWarehouseStock() throws Exception {
		mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, inventoryAdminBearerToken())
					.param("warehouseId", "2"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void shouldHideOtherWarehouseQualityInspectionDataFromQualityAdmin() throws Exception {
		MvcResult stockResult = mockMvc.perform(get("/api/inventory-stock/list")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.param("productId", "3")
					.param("warehouseId", "2")
					.param("zoneId", "2"))
				.andExpect(status().isOk())
				.andReturn();

		String stockId = objectMapper.readTree(stockResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		MvcResult inspectionResult = mockMvc.perform(post("/api/quality-inspection")
					.header(HttpHeaders.AUTHORIZATION, adminBearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "sourceType": "INVENTORY_STOCK",
							  "sourceId": %s,
							  "inspectQuantity": 2,
							  "unqualifiedQuantity": 1,
							  "remarks": "仓库二质检范围测试"
							}
							""".formatted(stockId)))
				.andExpect(status().isOk())
				.andReturn();

		String inspectionId = objectMapper.readTree(inspectionResult.getResponse().getContentAsString())
				.path("data")
				.path("id")
				.asText();

		mockMvc.perform(get("/api/quality-inspection/list")
					.header(HttpHeaders.AUTHORIZATION, qualityAdminBearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[*].warehouseId", Matchers.not(Matchers.hasItem("2"))));

		mockMvc.perform(get("/api/quality-inspection/" + inspectionId)
					.header(HttpHeaders.AUTHORIZATION, qualityAdminBearerToken()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private String adminBearerToken() throws Exception {
		return bearerToken("admin", "Admin@123456");
	}

	private String warehouseAdminBearerToken() throws Exception {
		return bearerToken("warehouse_admin", "Warehouse@123456");
	}

	private String inventoryAdminBearerToken() throws Exception {
		return bearerToken("inventory_admin", "Inventory@123456");
	}

	private String qualityAdminBearerToken() throws Exception {
		return bearerToken("quality_admin", "Quality@123456");
	}

	private String bearerToken(String account, String password) throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "account": "%s",
							  "password": "%s"
							}
							""".formatted(account, password)))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
		return "Bearer " + jsonNode.path("data").path("accessToken").asText();
	}
}
