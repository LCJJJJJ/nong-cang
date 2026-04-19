package com.nongcang.server.modules.assistant.controller;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.modules.assistant.service.AssistantLlmClient;
import com.nongcang.server.modules.assistant.service.AssistantLlmResponse;
import com.nongcang.server.modules.assistant.service.AssistantToolCall;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AssistantControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AssistantLlmClient assistantLlmClient;

	@Test
	void shouldChatWithDirectAnswer() throws Exception {
		when(assistantLlmClient.chat(anyList(), anyList()))
				.thenReturn(new AssistantLlmResponse("当前系统共有 2 个仓库。", List.of()));

		mockMvc.perform(post("/api/assistant/chat")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "message": "有哪些仓库",
							  "routePath": "/warehouses",
							  "routeTitle": "仓库信息管理"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.session.id").isNotEmpty())
				.andExpect(jsonPath("$.data.userMessage.role").value("user"))
				.andExpect(jsonPath("$.data.assistantMessage.role").value("assistant"))
				.andExpect(jsonPath("$.data.assistantMessage.content").value("当前系统共有 2 个仓库。"));
	}

	@Test
	void shouldChatWithToolResultAndQueryHistory() throws Exception {
		when(assistantLlmClient.chat(anyList(), anyList()))
				.thenReturn(
						new AssistantLlmResponse(
								"",
								List.of(new AssistantToolCall(
										"tool-call-1",
										"query_warehouse_data",
										"""
												{
												  "entityType": "warehouse",
												  "keyword": "一号综合仓",
												  "limit": 5
												}
												"""))),
						new AssistantLlmResponse("已为你找到 1 个匹配仓库：一号综合仓。", List.of()));

		MvcResult chatResult = mockMvc.perform(post("/api/assistant/chat")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "message": "查一下 一号综合仓",
							  "routePath": "/warehouses",
							  "routeTitle": "仓库信息管理"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.assistantMessage.messageType").value("RESULT"))
				.andExpect(jsonPath("$.data.assistantMessage.resultBlocks", Matchers.hasSize(1)))
				.andExpect(jsonPath("$.data.assistantMessage.resultBlocks[0].title").value("仓库查询"))
				.andExpect(jsonPath("$.data.assistantMessage.resultBlocks[0].rows[0].warehouseName").value("一号综合仓"))
				.andReturn();

		JsonNode responseJson = objectMapper.readTree(chatResult.getResponse().getContentAsString());
		String sessionId = responseJson.path("data").path("session").path("id").asText();

		mockMvc.perform(get("/api/assistant/sessions")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(1)));

		mockMvc.perform(get("/api/assistant/sessions/" + sessionId + "/messages")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.data[1].resultBlocks[0].routePath").value("/warehouses"));
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
