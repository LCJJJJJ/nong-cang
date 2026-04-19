package com.nongcang.server.modules.assistant.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.assistant.domain.entity.AssistantMessageEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AssistantMessageRepository {

	private static final RowMapper<AssistantMessageEntity> ASSISTANT_MESSAGE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AssistantMessageEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AssistantMessageEntity(
					rs.getLong("id"),
					rs.getLong("session_id"),
					rs.getString("role"),
					rs.getString("content"),
					rs.getString("message_type"),
					rs.getString("metadata_json"),
					toLocalDateTime(rs.getTimestamp("created_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AssistantMessageRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public long insert(AssistantMessageEntity entity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO assistant_message (
				  session_id,
				  role,
				  content,
				  message_type,
				  metadata_json
				)
				VALUES (
				  :sessionId,
				  :role,
				  :content,
				  :messageType,
				  :metadataJson
				)
				""", new MapSqlParameterSource()
				.addValue("sessionId", entity.sessionId())
				.addValue("role", entity.role())
				.addValue("content", entity.content())
				.addValue("messageType", entity.messageType())
				.addValue("metadataJson", entity.metadataJson()), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public List<AssistantMessageEntity> findAllBySessionId(Long sessionId) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM assistant_message
				WHERE session_id = :sessionId
				ORDER BY id ASC
				""", new MapSqlParameterSource("sessionId", sessionId), ASSISTANT_MESSAGE_ROW_MAPPER);
	}

	public List<AssistantMessageEntity> findRecentBySessionId(Long sessionId, int limit) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM (
				  SELECT *
				  FROM assistant_message
				  WHERE session_id = :sessionId
				  ORDER BY id DESC
				  LIMIT :limit
				) recent_messages
				ORDER BY id ASC
				""", new MapSqlParameterSource()
				.addValue("sessionId", sessionId)
				.addValue("limit", limit), ASSISTANT_MESSAGE_ROW_MAPPER);
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
