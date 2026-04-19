package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.Map;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.assistant.domain.entity.AssistantSessionEntity;
import com.nongcang.server.modules.assistant.repository.AssistantSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class AssistantBasicInfoActionServiceTests {

	@Autowired
	private AssistantBasicInfoActionService assistantBasicInfoActionService;

	@Autowired
	private AssistantSessionRepository assistantSessionRepository;

	@Test
	void shouldPrepareDraftCreateProductUnitActionWhenRequiredFieldsMissing() {
		AssistantActionPlanResult result = assistantBasicInfoActionService.prepareAction(
				createSession(),
				1L,
				new AssistantWriteActionToolArguments(
						null,
						"product_unit",
						"create",
						null,
						Map.of("unitName", "助手测试单位")));

		assertThat(result.status()).isEqualTo("DRAFT");
		assertThat(result.actionCard()).isNotNull();
		assertThat(result.actionCard().missingFields()).extracting(AssistantActionFieldPrompt::field)
				.contains("unitSymbol", "unitType", "precisionDigits", "status", "sortOrder");
	}

	@Test
	void shouldExecuteCreateProductUnitActionAfterConfirmation() {
		AssistantActionPlanResult draft = assistantBasicInfoActionService.prepareAction(
				createSession(),
				1L,
				new AssistantWriteActionToolArguments(
						null,
						"product_unit",
						"create",
						null,
						Map.of(
								"unitName", "助手测试单位A",
								"unitSymbol", "ATU",
								"unitType", "包装单位",
								"precisionDigits", 0,
								"status", 1,
								"sortOrder", 88)));

		assertThat(draft.status()).isEqualTo("READY");

		AssistantActionExecutionResult executionResult = assistantBasicInfoActionService.executeAction(
				draft.actionCode(),
				1L,
				null);

		assertThat(executionResult.status()).isEqualTo("EXECUTED");
		assertThat(executionResult.message()).contains("产品单位");
	}

	@Test
	void shouldPrepareReadyUpdateProductArchiveAction() {
		AssistantActionPlanResult result = assistantBasicInfoActionService.prepareAction(
				createSession(),
				1L,
				new AssistantWriteActionToolArguments(
						null,
						"product_archive",
						"update",
						"内酯豆腐",
						Map.of("productName", "内酯豆腐（助手改名）", "warningDays", 3)));

		assertThat(result.status()).isEqualTo("READY");
		assertThat(result.actionCard()).isNotNull();
		assertThat(result.actionCard().previewFields()).extracting(AssistantActionFieldValue::field)
				.contains("productName", "warningDays");
	}

	@Test
	void shouldBlockDeleteCategoryWhenCategoryInUse() {
		AssistantActionPlanResult result = assistantBasicInfoActionService.prepareAction(
				createSession(),
				1L,
				new AssistantWriteActionToolArguments(
						null,
						"category",
						"delete",
						"豆制品",
						Map.of()));

		assertThat(result.status()).isEqualTo("FAILED");
		assertThat(result.summary()).contains("不能删除");

		assertThatThrownBy(() -> assistantBasicInfoActionService.executeAction(result.actionCode(), 1L, "确认删除"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("不能删除");
	}

	private Long createSession() {
		long sessionId = assistantSessionRepository.insert(new AssistantSessionEntity(
				null,
				"TEST-ACTION-" + System.nanoTime(),
				1L,
				"测试动作会话",
				"/",
				"产品分类管理",
				1,
				null,
				null,
				null));
		return sessionId;
	}
}
