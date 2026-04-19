package com.nongcang.server.modules.assistant.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.assistant.domain.entity.AssistantActionPlanEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AssistantActionPlanRepository {

	private static final RowMapper<AssistantActionPlanEntity> ASSISTANT_ACTION_PLAN_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AssistantActionPlanEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AssistantActionPlanEntity(
					rs.getLong("id"),
					rs.getString("action_code"),
					rs.getLong("session_id"),
					rs.getLong("user_id"),
					rs.getString("resource_type"),
					rs.getString("action_type"),
					rs.getObject("target_id", Long.class),
					rs.getString("target_label"),
					rs.getString("fields_json"),
					rs.getString("missing_fields_json"),
					rs.getString("risk_level"),
					rs.getString("confirmation_mode"),
					rs.getString("status"),
					rs.getString("summary"),
					rs.getString("error_code"),
					rs.getString("error_message"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")),
					toLocalDateTime(rs.getTimestamp("executed_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AssistantActionPlanRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public long insert(AssistantActionPlanEntity entity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO assistant_action_plan (
				  action_code,
				  session_id,
				  user_id,
				  resource_type,
				  action_type,
				  target_id,
				  target_label,
				  fields_json,
				  missing_fields_json,
				  risk_level,
				  confirmation_mode,
				  status,
				  summary,
				  error_code,
				  error_message,
				  executed_at
				)
				VALUES (
				  :actionCode,
				  :sessionId,
				  :userId,
				  :resourceType,
				  :actionType,
				  :targetId,
				  :targetLabel,
				  :fieldsJson,
				  :missingFieldsJson,
				  :riskLevel,
				  :confirmationMode,
				  :status,
				  :summary,
				  :errorCode,
				  :errorMessage,
				  :executedAt
				)
				""", buildParameters(entity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(AssistantActionPlanEntity entity) {
		namedParameterJdbcTemplate.update("""
				UPDATE assistant_action_plan
				SET target_id = :targetId,
				    target_label = :targetLabel,
				    fields_json = :fieldsJson,
				    missing_fields_json = :missingFieldsJson,
				    risk_level = :riskLevel,
				    confirmation_mode = :confirmationMode,
				    status = :status,
				    summary = :summary,
				    error_code = :errorCode,
				    error_message = :errorMessage,
				    executed_at = :executedAt
				WHERE id = :id
				""", buildParameters(entity).addValue("id", entity.id()));
	}

	public Optional<AssistantActionPlanEntity> findByActionCodeAndUserId(String actionCode, Long userId) {
		List<AssistantActionPlanEntity> plans = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM assistant_action_plan
				WHERE action_code = :actionCode
				  AND user_id = :userId
				""", new MapSqlParameterSource()
				.addValue("actionCode", actionCode)
				.addValue("userId", userId), ASSISTANT_ACTION_PLAN_ROW_MAPPER);
		return plans.stream().findFirst();
	}

	public Optional<AssistantActionPlanEntity> findLatestOpenPlan(Long sessionId, Long userId) {
		List<AssistantActionPlanEntity> plans = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM assistant_action_plan
				WHERE session_id = :sessionId
				  AND user_id = :userId
				  AND status IN ('DRAFT', 'READY')
				ORDER BY id DESC
				LIMIT 1
				""", new MapSqlParameterSource()
				.addValue("sessionId", sessionId)
				.addValue("userId", userId), ASSISTANT_ACTION_PLAN_ROW_MAPPER);
		return plans.stream().findFirst();
	}

	private MapSqlParameterSource buildParameters(AssistantActionPlanEntity entity) {
		return new MapSqlParameterSource()
				.addValue("actionCode", entity.actionCode())
				.addValue("sessionId", entity.sessionId())
				.addValue("userId", entity.userId())
				.addValue("resourceType", entity.resourceType())
				.addValue("actionType", entity.actionType())
				.addValue("targetId", entity.targetId())
				.addValue("targetLabel", entity.targetLabel())
				.addValue("fieldsJson", entity.fieldsJson())
				.addValue("missingFieldsJson", entity.missingFieldsJson())
				.addValue("riskLevel", entity.riskLevel())
				.addValue("confirmationMode", entity.confirmationMode())
				.addValue("status", entity.status())
				.addValue("summary", entity.summary())
				.addValue("errorCode", entity.errorCode())
				.addValue("errorMessage", entity.errorMessage())
				.addValue("executedAt", entity.executedAt());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
