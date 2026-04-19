package com.nongcang.server.modules.inventorystock.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.inventorystock.domain.entity.InventoryStockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryStockQueryRepository {

	private static final RowMapper<InventoryStockEntity> INVENTORY_STOCK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryStockEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryStockEntity(
					rs.getLong("id"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("product_specification"),
					rs.getLong("unit_id"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getInt("precision_digits"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("stock_quantity"),
					rs.getBigDecimal("reserved_quantity"),
					rs.getBigDecimal("locked_quantity"),
					rs.getBigDecimal("available_quantity"),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryStockQueryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InventoryStockEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  s.id,
				  s.product_id,
				  pa.product_code,
				  pa.product_name,
				  pa.product_specification,
				  pa.unit_id,
				  pu.unit_name,
				  pu.unit_symbol,
				  pu.precision_digits,
				  s.warehouse_id,
				  w.warehouse_name,
				  s.zone_id,
				  wz.zone_name,
				  s.location_id,
				  wl.location_name,
				  s.quantity AS stock_quantity,
				  COALESCE(reserved.reserved_quantity, 0) AS reserved_quantity,
				  COALESCE(abnormal.locked_quantity, 0) AS locked_quantity,
				  GREATEST(
				    s.quantity - COALESCE(reserved.reserved_quantity, 0) - COALESCE(abnormal.locked_quantity, 0),
				    0
				  ) AS available_quantity,
				  s.updated_at
				FROM inventory_stock s
				JOIN product_archive pa ON pa.id = s.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = s.warehouse_id
				JOIN warehouse_zone wz ON wz.id = s.zone_id
				JOIN warehouse_location wl ON wl.id = s.location_id
				LEFT JOIN (
				  SELECT
				    product_id,
				    location_id,
				    SUM(quantity) AS reserved_quantity
				  FROM outbound_task
				  WHERE status IN (2, 3)
				  GROUP BY product_id, location_id
				) reserved
				  ON reserved.product_id = s.product_id
				 AND reserved.location_id = s.location_id
				LEFT JOIN (
				  SELECT
				    product_id,
				    location_id,
				    SUM(locked_quantity) AS locked_quantity
				  FROM abnormal_stock
				  WHERE status = 1
				  GROUP BY product_id, location_id
				) abnormal
				  ON abnormal.product_id = s.product_id
				 AND abnormal.location_id = s.location_id
				WHERE s.quantity > 0
				ORDER BY s.updated_at DESC, s.id DESC
				""", INVENTORY_STOCK_ROW_MAPPER);
	}

	public Optional<InventoryStockEntity> findById(Long id) {
		List<InventoryStockEntity> stocks = namedParameterJdbcTemplate.query("""
				SELECT
				  s.id,
				  s.product_id,
				  pa.product_code,
				  pa.product_name,
				  pa.product_specification,
				  pa.unit_id,
				  pu.unit_name,
				  pu.unit_symbol,
				  pu.precision_digits,
				  s.warehouse_id,
				  w.warehouse_name,
				  s.zone_id,
				  wz.zone_name,
				  s.location_id,
				  wl.location_name,
				  s.quantity AS stock_quantity,
				  COALESCE(reserved.reserved_quantity, 0) AS reserved_quantity,
				  COALESCE(abnormal.locked_quantity, 0) AS locked_quantity,
				  GREATEST(
				    s.quantity - COALESCE(reserved.reserved_quantity, 0) - COALESCE(abnormal.locked_quantity, 0),
				    0
				  ) AS available_quantity,
				  s.updated_at
				FROM inventory_stock s
				JOIN product_archive pa ON pa.id = s.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = s.warehouse_id
				JOIN warehouse_zone wz ON wz.id = s.zone_id
				JOIN warehouse_location wl ON wl.id = s.location_id
				LEFT JOIN (
				  SELECT
				    product_id,
				    location_id,
				    SUM(quantity) AS reserved_quantity
				  FROM outbound_task
				  WHERE status IN (2, 3)
				  GROUP BY product_id, location_id
				) reserved
				  ON reserved.product_id = s.product_id
				 AND reserved.location_id = s.location_id
				LEFT JOIN (
				  SELECT
				    product_id,
				    location_id,
				    SUM(locked_quantity) AS locked_quantity
				  FROM abnormal_stock
				  WHERE status = 1
				  GROUP BY product_id, location_id
				) abnormal
				  ON abnormal.product_id = s.product_id
				 AND abnormal.location_id = s.location_id
				WHERE s.id = :id
				""", new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id), INVENTORY_STOCK_ROW_MAPPER);
		return stocks.stream().findFirst();
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
