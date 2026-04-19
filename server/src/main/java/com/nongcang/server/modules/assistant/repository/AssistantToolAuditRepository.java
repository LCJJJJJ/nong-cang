package com.nongcang.server.modules.assistant.repository;

import com.nongcang.server.modules.assistant.domain.entity.AssistantToolAuditEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssistantToolAuditRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AssistantToolAuditRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void insert(AssistantToolAuditEntity entity) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO assistant_tool_audit (
				  session_id,
				  message_id,
				  tool_name,
				  tool_arguments_json,
				  tool_result_json,
				  success
				)
				VALUES (
				  :sessionId,
				  :messageId,
				  :toolName,
				  :toolArgumentsJson,
				  :toolResultJson,
				  :success
				)
				""", new MapSqlParameterSource()
				.addValue("sessionId", entity.sessionId())
				.addValue("messageId", entity.messageId())
				.addValue("toolName", entity.toolName())
				.addValue("toolArgumentsJson", entity.toolArgumentsJson())
				.addValue("toolResultJson", entity.toolResultJson())
				.addValue("success", entity.success()));
	}
}
