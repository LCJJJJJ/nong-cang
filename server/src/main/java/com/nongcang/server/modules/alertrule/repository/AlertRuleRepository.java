package com.nongcang.server.modules.alertrule.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.alertrule.domain.entity.AlertRuleEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRuleRepository {

	private static final RowMapper<AlertRuleEntity> ALERT_RULE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AlertRuleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AlertRuleEntity(
					rs.getLong("id"),
					rs.getString("rule_code"),
					rs.getString("rule_name"),
					rs.getString("alert_type"),
					rs.getString("severity"),
					rs.getBigDecimal("threshold_value"),
					rs.getString("threshold_unit"),
					rs.getInt("enabled"),
					rs.getString("description"),
					rs.getInt("sort_order"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AlertRuleRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<AlertRuleEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  rule_code,
				  rule_name,
				  alert_type,
				  severity,
				  threshold_value,
				  threshold_unit,
				  enabled,
				  description,
				  sort_order,
				  created_at,
				  updated_at
				FROM alert_rule
				ORDER BY sort_order ASC, id ASC
				""", ALERT_RULE_ROW_MAPPER);
	}

	public Optional<AlertRuleEntity> findById(Long id) {
		List<AlertRuleEntity> rules = namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  rule_code,
				  rule_name,
				  alert_type,
				  severity,
				  threshold_value,
				  threshold_unit,
				  enabled,
				  description,
				  sort_order,
				  created_at,
				  updated_at
				FROM alert_rule
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), ALERT_RULE_ROW_MAPPER);
		return rules.stream().findFirst();
	}

	public List<AlertRuleEntity> findEnabledRules() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  rule_code,
				  rule_name,
				  alert_type,
				  severity,
				  threshold_value,
				  threshold_unit,
				  enabled,
				  description,
				  sort_order,
				  created_at,
				  updated_at
				FROM alert_rule
				WHERE enabled = 1
				ORDER BY sort_order ASC, id ASC
				""", ALERT_RULE_ROW_MAPPER);
	}

	public void update(AlertRuleEntity entity) {
		namedParameterJdbcTemplate.update("""
				UPDATE alert_rule
				SET severity = :severity,
				    threshold_value = :thresholdValue,
				    threshold_unit = :thresholdUnit,
				    description = :description,
				    sort_order = :sortOrder
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", entity.id())
				.addValue("severity", entity.severity())
				.addValue("thresholdValue", entity.thresholdValue())
				.addValue("thresholdUnit", entity.thresholdUnit())
				.addValue("description", entity.description())
				.addValue("sortOrder", entity.sortOrder()));
	}

	public void updateEnabled(Long id, Integer enabled) {
		namedParameterJdbcTemplate.update("""
				UPDATE alert_rule
				SET enabled = :enabled
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("enabled", enabled));
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
