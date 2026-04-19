package com.nongcang.server.modules.warehouse.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.warehouse.domain.entity.WarehouseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseRepository {

	private static final RowMapper<WarehouseEntity> WAREHOUSE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public WarehouseEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new WarehouseEntity(
					rs.getLong("id"),
					rs.getString("warehouse_code"),
					rs.getString("warehouse_name"),
					rs.getString("warehouse_type"),
					rs.getString("manager_name"),
					rs.getString("contact_phone"),
					rs.getString("address"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public WarehouseRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<WarehouseEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM warehouse
				ORDER BY sort_order ASC, id ASC
				""", WAREHOUSE_ROW_MAPPER);
	}

	public Optional<WarehouseEntity> findById(Long id) {
		List<WarehouseEntity> warehouses = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM warehouse
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), WAREHOUSE_ROW_MAPPER);
		return warehouses.stream().findFirst();
	}

	public boolean existsByWarehouseCode(String warehouseCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse
				WHERE warehouse_code = :warehouseCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("warehouseCode", warehouseCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByWarehouseName(String warehouseName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM warehouse
				WHERE warehouse_name = :warehouseName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("warehouseName", warehouseName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(WarehouseEntity warehouseEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO warehouse (
				  warehouse_code,
				  warehouse_name,
				  warehouse_type,
				  manager_name,
				  contact_phone,
				  address,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :warehouseCode,
				  :warehouseName,
				  :warehouseType,
				  :managerName,
				  :contactPhone,
				  :address,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(warehouseEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(WarehouseEntity warehouseEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse
				SET warehouse_name = :warehouseName,
				    warehouse_type = :warehouseType,
				    manager_name = :managerName,
				    contact_phone = :contactPhone,
				    address = :address,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(warehouseEntity).addValue("id", warehouseEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE warehouse
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM warehouse
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countZoneReferences(Long id) {
		if (!existsTable("warehouse_zone")) {
			return 0L;
		}

		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM warehouse_zone
				WHERE warehouse_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
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

	private MapSqlParameterSource buildParameters(WarehouseEntity warehouseEntity) {
		return new MapSqlParameterSource()
				.addValue("warehouseCode", warehouseEntity.warehouseCode())
				.addValue("warehouseName", warehouseEntity.warehouseName())
				.addValue("warehouseType", warehouseEntity.warehouseType())
				.addValue("managerName", warehouseEntity.managerName())
				.addValue("contactPhone", warehouseEntity.contactPhone())
				.addValue("address", warehouseEntity.address())
				.addValue("status", warehouseEntity.status())
				.addValue("sortOrder", warehouseEntity.sortOrder())
				.addValue("remarks", warehouseEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
