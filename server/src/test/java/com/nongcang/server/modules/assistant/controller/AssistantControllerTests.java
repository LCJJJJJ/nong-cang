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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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
				.andExpect(jsonPath("$.data[?(@.id=='" + sessionId + "')]").isNotEmpty());

		mockMvc.perform(get("/api/assistant/sessions/" + sessionId + "/messages")
					.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.data[1].resultBlocks[0].routePath").value("/warehouses"));
	}

	@Test
	void shouldResolveChainedToolCallsForOutboundRecordQuery() throws Exception {
		when(assistantLlmClient.chat(anyList(), anyList()))
				.thenReturn(
						new AssistantLlmResponse(
								"",
								List.of(new AssistantToolCall(
										"tool-call-1",
										"query_basic_master_data",
										"""
												{
												  "entityType": "product",
												  "keyword": "内酯豆腐",
												  "limit": 5
												}
												"""))),
						new AssistantLlmResponse(
								"",
								List.of(new AssistantToolCall(
										"tool-call-2",
										"query_outbound_data",
										"""
												{
												  "entityType": "outbound_record",
												  "keyword": "PROD-20260419083640261",
												  "limit": 20
												}
												"""))),
						new AssistantLlmResponse("已找到内酯豆腐的出库记录。", List.of()));

		mockMvc.perform(post("/api/assistant/chat")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "message": "查一下内酯豆腐的出库记录",
							  "routePath": "/outbound-records",
							  "routeTitle": "出库记录查询"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.assistantMessage.resultBlocks[0].title").value("出库记录查询"))
				.andExpect(jsonPath("$.data.assistantMessage.resultBlocks[0].rows[0].recordCode")
						.value("OR-202604190001"));
	}

	@Test
	void shouldStreamAssistantResponse() throws Exception {
		when(assistantLlmClient.chat(anyList(), anyList()))
				.thenReturn(new AssistantLlmResponse("正在为你整理仓库结果。", List.of()));

		MvcResult mvcResult = mockMvc.perform(post("/api/assistant/chat/stream")
					.header(HttpHeaders.AUTHORIZATION, bearerToken())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "message": "有哪些仓库",
							  "routePath": "/warehouses",
							  "routeTitle": "仓库信息管理"
							}
							"""))
				.andExpect(request().asyncStarted())
				.andReturn();

		mvcResult.getAsyncResult(5000);

		org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
		org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getContentType())
				.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
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
