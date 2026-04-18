package com.nongcang.server.modules.shelfliferule.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.shelfliferule.domain.entity.ShelfLifeRuleEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ShelfLifeRuleRepository {

	private static final String SHELF_LIFE_RULE_SELECT = """
			SELECT
			  slr.id,
			  slr.rule_code,
			  slr.rule_name,
			  slr.category_id,
			  pc.category_name,
			  slr.storage_condition_id,
			  sc.condition_name AS storage_condition_name,
			  sc.storage_type,
			  slr.shelf_life_days,
			  slr.warning_days,
			  slr.status,
			  slr.sort_order,
			  slr.remarks,
			  slr.created_at,
			  slr.updated_at
			FROM shelf_life_rule slr
			LEFT JOIN product_category pc
			  ON pc.id = slr.category_id
			LEFT JOIN storage_condition sc
			  ON sc.id = slr.storage_condition_id
			""";

	private static final RowMapper<ShelfLifeRuleEntity> SHELF_LIFE_RULE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public ShelfLifeRuleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ShelfLifeRuleEntity(
					rs.getLong("id"),
					rs.getString("rule_code"),
					rs.getString("rule_name"),
					rs.getObject("category_id", Long.class),
					rs.getString("category_name"),
					rs.getObject("storage_condition_id", Long.class),
					rs.getString("storage_condition_name"),
					rs.getString("storage_type"),
					rs.getInt("shelf_life_days"),
					rs.getInt("warning_days"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ShelfLifeRuleRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<ShelfLifeRuleEntity> findAll() {
		return namedParameterJdbcTemplate.query(SHELF_LIFE_RULE_SELECT + """
				ORDER BY slr.sort_order ASC, slr.id ASC
				""", SHELF_LIFE_RULE_ROW_MAPPER);
	}

	public Optional<ShelfLifeRuleEntity> findById(Long id) {
		List<ShelfLifeRuleEntity> shelfLifeRules = namedParameterJdbcTemplate.query(SHELF_LIFE_RULE_SELECT + """
				WHERE slr.id = :id
				""", new MapSqlParameterSource("id", id), SHELF_LIFE_RULE_ROW_MAPPER);
		return shelfLifeRules.stream().findFirst();
	}

	public boolean existsByRuleCode(String ruleCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM shelf_life_rule
				WHERE rule_code = :ruleCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("ruleCode", ruleCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByRuleName(String ruleName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM shelf_life_rule
				WHERE rule_name = :ruleName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("ruleName", ruleName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(ShelfLifeRuleEntity shelfLifeRuleEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO shelf_life_rule (
				  rule_code,
				  rule_name,
				  category_id,
				  storage_condition_id,
				  shelf_life_days,
				  warning_days,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :ruleCode,
				  :ruleName,
				  :categoryId,
				  :storageConditionId,
				  :shelfLifeDays,
				  :warningDays,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(shelfLifeRuleEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(ShelfLifeRuleEntity shelfLifeRuleEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE shelf_life_rule
				SET rule_name = :ruleName,
				    category_id = :categoryId,
				    storage_condition_id = :storageConditionId,
				    shelf_life_days = :shelfLifeDays,
				    warning_days = :warningDays,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(shelfLifeRuleEntity).addValue("id", shelfLifeRuleEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE shelf_life_rule
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM shelf_life_rule
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countProductArchiveReferences(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_archive
				WHERE shelf_life_rule_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(ShelfLifeRuleEntity shelfLifeRuleEntity) {
		return new MapSqlParameterSource()
				.addValue("ruleCode", shelfLifeRuleEntity.ruleCode())
				.addValue("ruleName", shelfLifeRuleEntity.ruleName())
				.addValue("categoryId", shelfLifeRuleEntity.categoryId())
				.addValue("storageConditionId", shelfLifeRuleEntity.storageConditionId())
				.addValue("shelfLifeDays", shelfLifeRuleEntity.shelfLifeDays())
				.addValue("warningDays", shelfLifeRuleEntity.warningDays())
				.addValue("status", shelfLifeRuleEntity.status())
				.addValue("sortOrder", shelfLifeRuleEntity.sortOrder())
				.addValue("remarks", shelfLifeRuleEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
