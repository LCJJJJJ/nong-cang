package com.nongcang.server.modules.inventoryadjustment.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.inventoryadjustment.domain.entity.InventoryAdjustmentEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryAdjustmentRepository {

	private static final RowMapper<InventoryAdjustmentEntity> INVENTORY_ADJUSTMENT_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryAdjustmentEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryAdjustmentEntity(
					rs.getLong("id"),
					rs.getString("adjustment_code"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("adjustment_type"),
					rs.getBigDecimal("quantity"),
					rs.getString("reason"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryAdjustmentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InventoryAdjustmentEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  ia.id,
				  ia.adjustment_code,
				  ia.warehouse_id,
				  w.warehouse_name,
				  ia.zone_id,
				  wz.zone_name,
				  ia.location_id,
				  wl.location_name,
				  ia.product_id,
				  pa.product_code,
				  pa.product_name,
				  ia.adjustment_type,
				  ia.quantity,
				  ia.reason,
				  ia.remarks,
				  ia.created_at,
				  ia.updated_at
				FROM inventory_adjustment ia
				JOIN warehouse w ON w.id = ia.warehouse_id
				JOIN warehouse_zone wz ON wz.id = ia.zone_id
				JOIN warehouse_location wl ON wl.id = ia.location_id
				JOIN product_archive pa ON pa.id = ia.product_id
				ORDER BY ia.created_at DESC, ia.id DESC
				""", INVENTORY_ADJUSTMENT_ROW_MAPPER);
	}

	public Optional<InventoryAdjustmentEntity> findById(Long id) {
		List<InventoryAdjustmentEntity> list = namedParameterJdbcTemplate.query("""
				SELECT
				  ia.id,
				  ia.adjustment_code,
				  ia.warehouse_id,
				  w.warehouse_name,
				  ia.zone_id,
				  wz.zone_name,
				  ia.location_id,
				  wl.location_name,
				  ia.product_id,
				  pa.product_code,
				  pa.product_name,
				  ia.adjustment_type,
				  ia.quantity,
				  ia.reason,
				  ia.remarks,
				  ia.created_at,
				  ia.updated_at
				FROM inventory_adjustment ia
				JOIN warehouse w ON w.id = ia.warehouse_id
				JOIN warehouse_zone wz ON wz.id = ia.zone_id
				JOIN warehouse_location wl ON wl.id = ia.location_id
				JOIN product_archive pa ON pa.id = ia.product_id
				WHERE ia.id = :id
				""", new MapSqlParameterSource("id", id), INVENTORY_ADJUSTMENT_ROW_MAPPER);
		return list.stream().findFirst();
	}

	public boolean existsByAdjustmentCode(String adjustmentCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM inventory_adjustment
				WHERE adjustment_code = :adjustmentCode
				""", new MapSqlParameterSource("adjustmentCode", adjustmentCode), Integer.class);
		return count != null && count > 0;
	}

	public long insert(
			String adjustmentCode,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			Long productId,
			String adjustmentType,
			BigDecimal quantity,
			String reason,
			String remarks) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_adjustment (
				  adjustment_code,
				  warehouse_id,
				  zone_id,
				  location_id,
				  product_id,
				  adjustment_type,
				  quantity,
				  reason,
				  remarks
				)
				VALUES (
				  :adjustmentCode,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :productId,
				  :adjustmentType,
				  :quantity,
				  :reason,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("adjustmentCode", adjustmentCode)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("productId", productId)
				.addValue("adjustmentType", adjustmentType)
				.addValue("quantity", quantity)
				.addValue("reason", reason)
				.addValue("remarks", remarks), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
