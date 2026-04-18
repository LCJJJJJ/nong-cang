package com.nongcang.server.modules.storagecondition.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.storagecondition.domain.entity.StorageConditionEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class StorageConditionRepository {

	private static final RowMapper<StorageConditionEntity> STORAGE_CONDITION_ROW_MAPPER = new RowMapper<>() {
		@Override
		public StorageConditionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new StorageConditionEntity(
					rs.getLong("id"),
					rs.getString("condition_code"),
					rs.getString("condition_name"),
					rs.getString("storage_type"),
					rs.getBigDecimal("temperature_min"),
					rs.getBigDecimal("temperature_max"),
					rs.getBigDecimal("humidity_min"),
					rs.getBigDecimal("humidity_max"),
					rs.getString("light_requirement"),
					rs.getString("ventilation_requirement"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public StorageConditionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<StorageConditionEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM storage_condition
				ORDER BY sort_order ASC, id ASC
				""", STORAGE_CONDITION_ROW_MAPPER);
	}

	public Optional<StorageConditionEntity> findById(Long id) {
		List<StorageConditionEntity> storageConditions = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM storage_condition
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), STORAGE_CONDITION_ROW_MAPPER);
		return storageConditions.stream().findFirst();
	}

	public boolean existsByConditionCode(String conditionCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM storage_condition
				WHERE condition_code = :conditionCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("conditionCode", conditionCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByConditionName(String conditionName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM storage_condition
				WHERE condition_name = :conditionName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("conditionName", conditionName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(StorageConditionEntity storageConditionEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO storage_condition (
				  condition_code,
				  condition_name,
				  storage_type,
				  temperature_min,
				  temperature_max,
				  humidity_min,
				  humidity_max,
				  light_requirement,
				  ventilation_requirement,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :conditionCode,
				  :conditionName,
				  :storageType,
				  :temperatureMin,
				  :temperatureMax,
				  :humidityMin,
				  :humidityMax,
				  :lightRequirement,
				  :ventilationRequirement,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(storageConditionEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(StorageConditionEntity storageConditionEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE storage_condition
				SET condition_name = :conditionName,
				    storage_type = :storageType,
				    temperature_min = :temperatureMin,
				    temperature_max = :temperatureMax,
				    humidity_min = :humidityMin,
				    humidity_max = :humidityMax,
				    light_requirement = :lightRequirement,
				    ventilation_requirement = :ventilationRequirement,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(storageConditionEntity).addValue("id", storageConditionEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE storage_condition
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM storage_condition
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countCategoryReferences(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_category
				WHERE default_storage_condition_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(StorageConditionEntity storageConditionEntity) {
		return new MapSqlParameterSource()
				.addValue("conditionCode", storageConditionEntity.conditionCode())
				.addValue("conditionName", storageConditionEntity.conditionName())
				.addValue("storageType", storageConditionEntity.storageType())
				.addValue("temperatureMin", storageConditionEntity.temperatureMin())
				.addValue("temperatureMax", storageConditionEntity.temperatureMax())
				.addValue("humidityMin", storageConditionEntity.humidityMin())
				.addValue("humidityMax", storageConditionEntity.humidityMax())
				.addValue("lightRequirement", storageConditionEntity.lightRequirement())
				.addValue("ventilationRequirement", storageConditionEntity.ventilationRequirement())
				.addValue("status", storageConditionEntity.status())
				.addValue("sortOrder", storageConditionEntity.sortOrder())
				.addValue("remarks", storageConditionEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
