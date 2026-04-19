package com.nongcang.server.modules.messagenotice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MessageNoticeControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldListMessageNoticesAfterRefresh() throws Exception {
		mockMvc.perform(post("/api/alert-record/refresh")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/message-notice/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.data[0].noticeCode").isNotEmpty());
	}

	@Test
	void shouldMarkReadAndReadAll() throws Exception {
		mockMvc.perform(post("/api/alert-record/refresh")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk());

		MvcResult listResult = mockMvc.perform(get("/api/message-notice/list")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andReturn();

		String noticeId = objectMapper.readTree(listResult.getResponse().getContentAsString())
				.path("data")
				.path(0)
				.path("id")
				.asText();

		mockMvc.perform(patch("/api/message-notice/" + noticeId + "/read")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("已标记已读"));

		mockMvc.perform(patch("/api/message-notice/read-all")
					.header("Authorization", bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("全部消息已读"));
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
