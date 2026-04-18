package com.nongcang.server.modules.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SystemDemoControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnUnifiedSuccessResponse() throws Exception {
		mockMvc.perform(get("/api/system/ping"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(header().exists("X-Trace-Id"))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value("OK"))
				.andExpect(jsonPath("$.data.demoId").value("20260418001"))
				.andExpect(jsonPath("$.data.service").value("server"))
				.andExpect(jsonPath("$.data.status").value("UP"))
				.andExpect(jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void shouldReturnValidationErrorResponse() throws Exception {
		mockMvc.perform(post("/api/system/echo").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "content": ""
				}
				"""))
				.andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[0].field").value("content"));
	}

	@Test
	void shouldReturnBusinessErrorResponse() throws Exception {
		mockMvc.perform(get("/api/system/business-error"))
				.andDo(print())
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("DEMO_BUSINESS_ERROR"))
				.andExpect(jsonPath("$.message").value("这是一个演示业务错误，用于验证前后端统一错误处理"));
	}

	@Test
	void shouldReturnUnifiedNotFoundResponse() throws Exception {
		mockMvc.perform(get("/api/system/not-found"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("请求的资源不存在"));
	}
}
