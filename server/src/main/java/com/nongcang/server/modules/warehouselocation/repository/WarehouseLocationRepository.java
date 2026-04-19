package com.nongcang.server.modules.warehouselocation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseLocationRepository {

	private static final String WAREHOUSE_LOCATION_SELECT = """
			SELECT
			  wl.id,
			  wl.location_code,
			  wl.warehouse_id,
			  wh.warehouse_name,
			  wl.zone_id,
			  wz.zone_name,
			  wl.location_name,
			  wl.location_type,
			  wl.capacity,
			  wl.status,
			  wl.sort_order,
			  wl.remarks,
			  wl.created_at,
			  wl.updated_at
			FROM warehouse_location wl
			JOIN warehouse wh ON wh.id = wl.warehouse_id
			JOIN warehouse_zone wz ON wz.id = wl.zone_id
			""";

	private static final RowMapper<WarehouseLocationEntity> WAREHOUSE_LOCATION_ROW_MAPPER = new RowMapper<>() {
		@Override
		public WarehouseLocationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new WarehouseLocationEntity(
					rs.getLong("id"),
					rs.getString("location_code"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getString("location_name"),
					rs.getString("location_type"),
					rs.getObject("capacity", Integer.class),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public WarehouseLocationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<WarehouseLocationEntity> findAll() {
		return namedParameterJdbcTemplate.query(WAREHOUSE_LOCATION_SELECT + """
				ORDER BY wl.sort_order ASC, wl.id ASC
				""", WAREHOUSE_LOCATION_ROW_MAPPER);
	}

	public Optional<WarehouseLocationEntity> findById(Long id) {
		List<WarehouseLocationEntity> warehouseLocations = namedParameterJdbcTemplate.query(WAREHOUSE_LOCATION_SELECT + """
				WHERE wl.id = :id
				""", new MapSqlParameterSource("id", id), WAREHOUSE_LOCATION_ROW_MAPPER);
		return warehouseLocations.stream().findFirst();
	}

	public boolean existsByLocationCode(String locationCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse_location
				WHERE location_code = :locationCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("locationCode", locationCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByZoneAndLocationName(Long zoneId, String locationName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse_location
				WHERE zone_id = :zoneId
				  AND location_name = :locationName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("zoneId", zoneId)
				.addValue("locationName", locationName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(WarehouseLocationEntity warehouseLocationEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO warehouse_location (
				  location_code,
				  warehouse_id,
				  zone_id,
				  location_name,
				  location_type,
				  capacity,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :locationCode,
				  :warehouseId,
				  :zoneId,
				  :locationName,
				  :locationType,
				  :capacity,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(warehouseLocationEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(WarehouseLocationEntity warehouseLocationEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse_location
				SET warehouse_id = :warehouseId,
				    zone_id = :zoneId,
				    location_name = :locationName,
				    location_type = :locationType,
				    capacity = :capacity,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(warehouseLocationEntity).addValue("id", warehouseLocationEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse_location
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM warehouse_location
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private MapSqlParameterSource buildParameters(WarehouseLocationEntity warehouseLocationEntity) {
		return new MapSqlParameterSource()
				.addValue("locationCode", warehouseLocationEntity.locationCode())
				.addValue("warehouseId", warehouseLocationEntity.warehouseId())
				.addValue("zoneId", warehouseLocationEntity.zoneId())
				.addValue("locationName", warehouseLocationEntity.locationName())
				.addValue("locationType", warehouseLocationEntity.locationType())
				.addValue("capacity", warehouseLocationEntity.capacity())
				.addValue("status", warehouseLocationEntity.status())
				.addValue("sortOrder", warehouseLocationEntity.sortOrder())
				.addValue("remarks", warehouseLocationEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
