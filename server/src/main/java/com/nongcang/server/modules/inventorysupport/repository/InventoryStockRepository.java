package com.nongcang.server.modules.inventorysupport.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryLocationStockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryStockRepository {

	private static final RowMapper<InventoryLocationStockEntity> INVENTORY_LOCATION_STOCK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryLocationStockEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryLocationStockEntity(
					rs.getLong("warehouse_id"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("stock_quantity"),
					rs.getBigDecimal("reserved_quantity"),
					rs.getBigDecimal("locked_quantity"),
					rs.getBigDecimal("available_quantity"));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryStockRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void increaseStock(
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal quantity) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_stock (
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  quantity
				)
				VALUES (
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :quantity
				)
				ON DUPLICATE KEY UPDATE
				  quantity = quantity + VALUES(quantity)
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("quantity", quantity));
	}

	public boolean decreaseStock(
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal quantity) {
		int updatedRows = namedParameterJdbcTemplate.update("""
				UPDATE inventory_stock
				SET quantity = quantity - :quantity
				WHERE product_id = :productId
				  AND warehouse_id = :warehouseId
				  AND zone_id = :zoneId
				  AND location_id = :locationId
				  AND quantity >= :quantity
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("quantity", quantity));
		return updatedRows > 0;
	}

	public void setStockQuantity(
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal quantity) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_stock (
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  quantity
				)
				VALUES (
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :quantity
				)
				ON DUPLICATE KEY UPDATE
				  quantity = VALUES(quantity)
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("quantity", quantity));
	}

	public BigDecimal getReservedQuantity(Long productId, Long locationId) {
		BigDecimal quantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(quantity), 0)
				FROM outbound_task
				WHERE product_id = :productId
				  AND location_id = :locationId
				  AND status IN (2, 3)
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("locationId", locationId), BigDecimal.class);
		return quantity == null ? BigDecimal.ZERO : quantity;
	}

	public BigDecimal getLockedQuantity(Long productId, Long locationId) {
		BigDecimal quantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(locked_quantity), 0)
				FROM abnormal_stock
				WHERE product_id = :productId
				  AND location_id = :locationId
				  AND status = 1
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("locationId", locationId), BigDecimal.class);
		return quantity == null ? BigDecimal.ZERO : quantity;
	}

	public List<InventoryLocationStockEntity> findAvailableStocks(
			Long productId,
			Long warehouseId,
			Long excludeTaskId) {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  s.warehouse_id,
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
				  ) AS available_quantity
				FROM inventory_stock s
				JOIN warehouse_zone wz ON wz.id = s.zone_id
				JOIN warehouse_location wl ON wl.id = s.location_id
				LEFT JOIN (
				  SELECT
				    product_id,
				    location_id,
				    SUM(quantity) AS reserved_quantity
				  FROM outbound_task
				  WHERE status IN (2, 3)
				    AND id <> :excludeTaskId
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
				WHERE s.product_id = :productId
				  AND s.warehouse_id = :warehouseId
				  AND s.quantity > 0
				ORDER BY wz.zone_name ASC, wl.location_name ASC
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("excludeTaskId", excludeTaskId == null ? -1L : excludeTaskId), INVENTORY_LOCATION_STOCK_ROW_MAPPER);
	}
}
