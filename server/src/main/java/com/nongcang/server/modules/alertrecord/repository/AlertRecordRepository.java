package com.nongcang.server.modules.alertrecord.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.alertrecord.domain.entity.AlertRecordEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRecordRepository {

	private static final RowMapper<AlertRecordEntity> ALERT_RECORD_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AlertRecordEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AlertRecordEntity(
					rs.getLong("id"),
					rs.getString("alert_code"),
					rs.getLong("rule_id"),
					rs.getString("rule_code"),
					rs.getString("alert_type"),
					rs.getString("severity"),
					rs.getString("source_type"),
					rs.getLong("source_id"),
					rs.getString("source_code"),
					rs.getString("title"),
					rs.getString("content"),
					rs.getInt("status"),
					toLocalDateTime(rs.getTimestamp("occurred_at")),
					toLocalDateTime(rs.getTimestamp("resolved_at")),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AlertRecordRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<AlertRecordEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  alert_code,
				  rule_id,
				  rule_code,
				  alert_type,
				  severity,
				  source_type,
				  source_id,
				  source_code,
				  title,
				  content,
				  status,
				  occurred_at,
				  resolved_at,
				  created_at,
				  updated_at
				FROM alert_record
				ORDER BY occurred_at DESC, id DESC
				""", ALERT_RECORD_ROW_MAPPER);
	}

	public List<AlertRecordEntity> findUnresolved() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  alert_code,
				  rule_id,
				  rule_code,
				  alert_type,
				  severity,
				  source_type,
				  source_id,
				  source_code,
				  title,
				  content,
				  status,
				  occurred_at,
				  resolved_at,
				  created_at,
				  updated_at
				FROM alert_record
				WHERE status IN (1, 2)
				ORDER BY occurred_at DESC, id DESC
				""", ALERT_RECORD_ROW_MAPPER);
	}

	public Optional<AlertRecordEntity> findById(Long id) {
		List<AlertRecordEntity> list = namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  alert_code,
				  rule_id,
				  rule_code,
				  alert_type,
				  severity,
				  source_type,
				  source_id,
				  source_code,
				  title,
				  content,
				  status,
				  occurred_at,
				  resolved_at,
				  created_at,
				  updated_at
				FROM alert_record
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), ALERT_RECORD_ROW_MAPPER);
		return list.stream().findFirst();
	}

	public boolean existsByAlertCode(String alertCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM alert_record
				WHERE alert_code = :alertCode
				""", new MapSqlParameterSource("alertCode", alertCode), Integer.class);
		return count != null && count > 0;
	}

	public long insert(AlertRecordEntity entity) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO alert_record (
				  alert_code,
				  rule_id,
				  rule_code,
				  alert_type,
				  severity,
				  source_type,
				  source_id,
				  source_code,
				  title,
				  content,
				  status,
				  occurred_at,
				  resolved_at
				)
				VALUES (
				  :alertCode,
				  :ruleId,
				  :ruleCode,
				  :alertType,
				  :severity,
				  :sourceType,
				  :sourceId,
				  :sourceCode,
				  :title,
				  :content,
				  :status,
				  :occurredAt,
				  :resolvedAt
				)
				""", new MapSqlParameterSource()
				.addValue("alertCode", entity.alertCode())
				.addValue("ruleId", entity.ruleId())
				.addValue("ruleCode", entity.ruleCode())
				.addValue("alertType", entity.alertType())
				.addValue("severity", entity.severity())
				.addValue("sourceType", entity.sourceType())
				.addValue("sourceId", entity.sourceId())
				.addValue("sourceCode", entity.sourceCode())
				.addValue("title", entity.title())
				.addValue("content", entity.content())
				.addValue("status", entity.status())
				.addValue("occurredAt", entity.occurredAt())
				.addValue("resolvedAt", entity.resolvedAt()), keyHolder);
		Number generatedId = keyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void resolve(Long id, LocalDateTime resolvedAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE alert_record
				SET status = 3,
				    resolved_at = :resolvedAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("resolvedAt", resolvedAt));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE alert_record
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
