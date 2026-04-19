package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.nongcang.server.modules.assistant.domain.dto.AssistantChatRequest;
import com.nongcang.server.modules.assistant.domain.vo.AssistantChatResponse;
import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AssistantServiceTests {

	@Autowired
	private AssistantService assistantService;

	@MockBean
	private AssistantLlmClient assistantLlmClient;

	@Test
	void shouldUseFinalToolResultBlocksForStreamChat() {
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
						new AssistantLlmResponse("规划完成", List.of()));

		doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			java.util.function.Consumer<String> consumer =
					(java.util.function.Consumer<String>) invocation.getArgument(2);
			consumer.accept("已找到");
			consumer.accept("内酯豆腐的出库记录。");
			return null;
		}).when(assistantLlmClient).streamChat(anyList(), anyList(), any());

		AtomicReference<AssistantChatResponse> responseReference = new AtomicReference<>();

		assistantService.streamChat(
				new AssistantChatRequest(null, "查一下内酯豆腐的出库记录", "/outbound-records", "出库记录查询"),
				authentication(),
				new AssistantStreamListener() {
					@Override
					public void onDone(AssistantChatResponse response) {
						responseReference.set(response);
					}
				});

		AssistantChatResponse response = responseReference.get();
		assertThat(response).isNotNull();
		assertThat(response.assistantMessage().resultBlocks()).isNotNull();
		assertThat(response.assistantMessage().resultBlocks()).hasSize(1);
		assertThat(response.assistantMessage().resultBlocks().get(0).title()).isEqualTo("出库记录查询");
	}

	private Authentication authentication() {
		return new UsernamePasswordAuthenticationToken(
				new AuthenticatedUser(
						1L,
						"admin",
						"系统管理员",
						"13800000000",
						"ADMIN",
						null,
						null,
						List.of("ADMIN")),
				null,
				List.of());
	}
}
