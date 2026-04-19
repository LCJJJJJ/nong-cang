package com.nongcang.server.modules.assistant.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.assistant.domain.entity.AssistantSessionEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AssistantSessionRepository {

	private static final RowMapper<AssistantSessionEntity> ASSISTANT_SESSION_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AssistantSessionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AssistantSessionEntity(
					rs.getLong("id"),
					rs.getString("session_code"),
					rs.getLong("user_id"),
					rs.getString("title"),
					rs.getString("route_path"),
					rs.getString("route_title"),
					rs.getInt("status"),
					rs.getString("last_message_preview"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AssistantSessionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public long insert(AssistantSessionEntity entity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO assistant_session (
				  session_code,
				  user_id,
				  title,
				  route_path,
				  route_title,
				  status
				)
				VALUES (
				  :sessionCode,
				  :userId,
				  :title,
				  :routePath,
				  :routeTitle,
				  :status
				)
				""", new MapSqlParameterSource()
				.addValue("sessionCode", entity.sessionCode())
				.addValue("userId", entity.userId())
				.addValue("title", entity.title())
				.addValue("routePath", entity.routePath())
				.addValue("routeTitle", entity.routeTitle())
				.addValue("status", entity.status()), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public Optional<AssistantSessionEntity> findByIdAndUserId(Long id, Long userId) {
		List<AssistantSessionEntity> sessions = namedParameterJdbcTemplate.query(baseSessionSelect() + """
				WHERE s.id = :id
				  AND s.user_id = :userId
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("userId", userId), ASSISTANT_SESSION_ROW_MAPPER);
		return sessions.stream().findFirst();
	}

	public List<AssistantSessionEntity> findRecentByUserId(Long userId, int limit) {
		return namedParameterJdbcTemplate.query(baseSessionSelect() + """
				WHERE s.user_id = :userId
				  AND s.status = 1
				ORDER BY s.updated_at DESC, s.id DESC
				LIMIT :limit
				""", new MapSqlParameterSource()
				.addValue("userId", userId)
				.addValue("limit", limit), ASSISTANT_SESSION_ROW_MAPPER);
	}

	public void updateContext(Long id, String routePath, String routeTitle) {
		namedParameterJdbcTemplate.update("""
				UPDATE assistant_session
				SET route_path = :routePath,
				    route_title = :routeTitle,
				    updated_at = CURRENT_TIMESTAMP
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("routePath", routePath)
				.addValue("routeTitle", routeTitle));
	}

	private String baseSessionSelect() {
		return """
				SELECT
				  s.id,
				  s.session_code,
				  s.user_id,
				  s.title,
				  s.route_path,
				  s.route_title,
				  s.status,
				  (
				    SELECT SUBSTRING(m.content, 1, 120)
				    FROM assistant_message m
				    WHERE m.session_id = s.id
				    ORDER BY m.id DESC
				    LIMIT 1
				  ) AS last_message_preview,
				  s.created_at,
				  s.updated_at
				FROM assistant_session s
				""";
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
