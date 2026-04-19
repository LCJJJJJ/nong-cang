package com.nongcang.server.modules.warehousezone.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseZoneRepository {

	private static final String WAREHOUSE_ZONE_SELECT = """
			SELECT
			  wz.id,
			  wz.zone_code,
			  wz.warehouse_id,
			  wh.warehouse_name,
			  wz.zone_name,
			  wz.zone_type,
			  wz.temperature_min,
			  wz.temperature_max,
			  wz.status,
			  wz.sort_order,
			  wz.remarks,
			  wz.created_at,
			  wz.updated_at
			FROM warehouse_zone wz
			JOIN warehouse wh ON wh.id = wz.warehouse_id
			""";

	private static final RowMapper<WarehouseZoneEntity> WAREHOUSE_ZONE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public WarehouseZoneEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new WarehouseZoneEntity(
					rs.getLong("id"),
					rs.getString("zone_code"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getString("zone_name"),
					rs.getString("zone_type"),
					rs.getBigDecimal("temperature_min"),
					rs.getBigDecimal("temperature_max"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public WarehouseZoneRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<WarehouseZoneEntity> findAll() {
		return namedParameterJdbcTemplate.query(WAREHOUSE_ZONE_SELECT + """
				ORDER BY wz.sort_order ASC, wz.id ASC
				""", WAREHOUSE_ZONE_ROW_MAPPER);
	}

	public Optional<WarehouseZoneEntity> findById(Long id) {
		List<WarehouseZoneEntity> warehouseZones = namedParameterJdbcTemplate.query(WAREHOUSE_ZONE_SELECT + """
				WHERE wz.id = :id
				""", new MapSqlParameterSource("id", id), WAREHOUSE_ZONE_ROW_MAPPER);
		return warehouseZones.stream().findFirst();
	}

	public long countLocationReferences(Long id) {
		if (!existsTable("warehouse_location")) {
			return 0L;
		}

		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM warehouse_location
				WHERE zone_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	public boolean existsByZoneCode(String zoneCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse_zone
				WHERE zone_code = :zoneCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("zoneCode", zoneCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByWarehouseAndZoneName(Long warehouseId, String zoneName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse_zone
				WHERE warehouse_id = :warehouseId
				  AND zone_name = :zoneName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("warehouseId", warehouseId)
				.addValue("zoneName", zoneName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(WarehouseZoneEntity warehouseZoneEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO warehouse_zone (
				  zone_code,
				  warehouse_id,
				  zone_name,
				  zone_type,
				  temperature_min,
				  temperature_max,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :zoneCode,
				  :warehouseId,
				  :zoneName,
				  :zoneType,
				  :temperatureMin,
				  :temperatureMax,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(warehouseZoneEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(WarehouseZoneEntity warehouseZoneEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse_zone
				SET warehouse_id = :warehouseId,
				    zone_name = :zoneName,
				    zone_type = :zoneType,
				    temperature_min = :temperatureMin,
				    temperature_max = :temperatureMax,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(warehouseZoneEntity).addValue("id", warehouseZoneEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse_zone
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM warehouse_zone
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private boolean existsTable(String tableName) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM information_schema.TABLES
				WHERE TABLE_SCHEMA = DATABASE()
				  AND TABLE_NAME = :tableName
				""", new MapSqlParameterSource("tableName", tableName), Integer.class);
		return count != null && count > 0;
	}

	private MapSqlParameterSource buildParameters(WarehouseZoneEntity warehouseZoneEntity) {
		return new MapSqlParameterSource()
				.addValue("zoneCode", warehouseZoneEntity.zoneCode())
				.addValue("warehouseId", warehouseZoneEntity.warehouseId())
				.addValue("zoneName", warehouseZoneEntity.zoneName())
				.addValue("zoneType", warehouseZoneEntity.zoneType())
				.addValue("temperatureMin", warehouseZoneEntity.temperatureMin())
				.addValue("temperatureMax", warehouseZoneEntity.temperatureMax())
				.addValue("status", warehouseZoneEntity.status())
				.addValue("sortOrder", warehouseZoneEntity.sortOrder())
				.addValue("remarks", warehouseZoneEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
