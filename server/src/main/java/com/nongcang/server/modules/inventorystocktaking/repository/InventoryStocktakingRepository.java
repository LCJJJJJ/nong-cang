package com.nongcang.server.modules.inventorystocktaking.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.inventorystocktaking.domain.entity.InventoryStocktakingItemEntity;
import com.nongcang.server.modules.inventorystocktaking.domain.entity.InventoryStocktakingOrderEntity;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingItemResponse;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingListItemResponse;
import com.nongcang.server.modules.inventorystock.domain.entity.InventoryStockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryStocktakingRepository {

	private static final RowMapper<InventoryStocktakingOrderEntity> ORDER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryStocktakingOrderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryStocktakingOrderEntity(
					rs.getLong("id"),
					rs.getString("stocktaking_code"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getObject("zone_id", Long.class),
					rs.getString("zone_name"),
					rs.getInt("status"),
					rs.getInt("item_count"),
					rs.getInt("counted_item_count"),
					rs.getBigDecimal("total_difference_quantity"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private static final RowMapper<InventoryStocktakingItemEntity> ITEM_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryStocktakingItemEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryStocktakingItemEntity(
					rs.getLong("id"),
					rs.getLong("stocktaking_order_id"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("product_specification"),
					rs.getLong("unit_id"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("system_quantity"),
					rs.getBigDecimal("counted_quantity"),
					rs.getBigDecimal("difference_quantity"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryStocktakingRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InventoryStocktakingOrderEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  so.id,
				  so.stocktaking_code,
				  so.warehouse_id,
				  w.warehouse_name,
				  so.zone_id,
				  wz.zone_name,
				  so.status,
				  COUNT(si.id) AS item_count,
				  SUM(CASE WHEN si.counted_quantity IS NOT NULL THEN 1 ELSE 0 END) AS counted_item_count,
				  COALESCE(SUM(si.difference_quantity), 0) AS total_difference_quantity,
				  so.remarks,
				  so.created_at,
				  so.updated_at
				FROM inventory_stocktaking_order so
				JOIN warehouse w ON w.id = so.warehouse_id
				LEFT JOIN warehouse_zone wz ON wz.id = so.zone_id
				LEFT JOIN inventory_stocktaking_item si ON si.stocktaking_order_id = so.id
				GROUP BY
				  so.id,
				  so.stocktaking_code,
				  so.warehouse_id,
				  w.warehouse_name,
				  so.zone_id,
				  wz.zone_name,
				  so.status,
				  so.remarks,
				  so.created_at,
				  so.updated_at
				ORDER BY so.created_at DESC, so.id DESC
				""", ORDER_ROW_MAPPER);
	}

	public Optional<InventoryStocktakingOrderEntity> findById(Long id) {
		List<InventoryStocktakingOrderEntity> list = namedParameterJdbcTemplate.query("""
				SELECT
				  so.id,
				  so.stocktaking_code,
				  so.warehouse_id,
				  w.warehouse_name,
				  so.zone_id,
				  wz.zone_name,
				  so.status,
				  COUNT(si.id) AS item_count,
				  SUM(CASE WHEN si.counted_quantity IS NOT NULL THEN 1 ELSE 0 END) AS counted_item_count,
				  COALESCE(SUM(si.difference_quantity), 0) AS total_difference_quantity,
				  so.remarks,
				  so.created_at,
				  so.updated_at
				FROM inventory_stocktaking_order so
				JOIN warehouse w ON w.id = so.warehouse_id
				LEFT JOIN warehouse_zone wz ON wz.id = so.zone_id
				LEFT JOIN inventory_stocktaking_item si ON si.stocktaking_order_id = so.id
				WHERE so.id = :id
				GROUP BY
				  so.id,
				  so.stocktaking_code,
				  so.warehouse_id,
				  w.warehouse_name,
				  so.zone_id,
				  wz.zone_name,
				  so.status,
				  so.remarks,
				  so.created_at,
				  so.updated_at
				""", new MapSqlParameterSource("id", id), ORDER_ROW_MAPPER);
		return list.stream().findFirst();
	}

	public List<InventoryStocktakingItemEntity> findItemsByOrderId(Long stocktakingOrderId) {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  si.id,
				  si.stocktaking_order_id,
				  si.product_id,
				  pa.product_code,
				  pa.product_name,
				  pa.product_specification,
				  pa.unit_id,
				  pu.unit_name,
				  pu.unit_symbol,
				  si.warehouse_id,
				  w.warehouse_name,
				  si.zone_id,
				  wz.zone_name,
				  si.location_id,
				  wl.location_name,
				  si.system_quantity,
				  si.counted_quantity,
				  si.difference_quantity,
				  si.remarks,
				  si.created_at,
				  si.updated_at
				FROM inventory_stocktaking_item si
				JOIN product_archive pa ON pa.id = si.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = si.warehouse_id
				JOIN warehouse_zone wz ON wz.id = si.zone_id
				JOIN warehouse_location wl ON wl.id = si.location_id
				WHERE si.stocktaking_order_id = :stocktakingOrderId
				ORDER BY si.id ASC
				""", new MapSqlParameterSource("stocktakingOrderId", stocktakingOrderId), ITEM_ROW_MAPPER);
	}

	public boolean existsByStocktakingCode(String stocktakingCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM inventory_stocktaking_order
				WHERE stocktaking_code = :stocktakingCode
				""", new MapSqlParameterSource("stocktakingCode", stocktakingCode), Integer.class);
		return count != null && count > 0;
	}

	public long insertOrder(Long warehouseId, Long zoneId, Integer status, String stocktakingCode, String remarks) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_stocktaking_order (
				  stocktaking_code,
				  warehouse_id,
				  zone_id,
				  status,
				  remarks
				)
				VALUES (
				  :stocktakingCode,
				  :warehouseId,
				  :zoneId,
				  :status,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("stocktakingCode", stocktakingCode)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("status", status)
				.addValue("remarks", remarks), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void insertItems(Long stocktakingOrderId, List<InventoryStockEntity> stocks) {
		for (InventoryStockEntity stock : stocks) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO inventory_stocktaking_item (
					  stocktaking_order_id,
					  product_id,
					  warehouse_id,
					  zone_id,
					  location_id,
					  system_quantity,
					  counted_quantity,
					  difference_quantity,
					  remarks
					)
					VALUES (
					  :stocktakingOrderId,
					  :productId,
					  :warehouseId,
					  :zoneId,
					  :locationId,
					  :systemQuantity,
					  NULL,
					  NULL,
					  NULL
					)
					""", new MapSqlParameterSource()
					.addValue("stocktakingOrderId", stocktakingOrderId)
					.addValue("productId", stock.productId())
					.addValue("warehouseId", stock.warehouseId())
					.addValue("zoneId", stock.zoneId())
					.addValue("locationId", stock.locationId())
					.addValue("systemQuantity", stock.stockQuantity()));
		}
	}

	public void updateItemCount(
			Long stocktakingOrderId,
			Long itemId,
			BigDecimal countedQuantity,
			BigDecimal differenceQuantity,
			String remarks) {
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_stocktaking_item
				SET counted_quantity = :countedQuantity,
				    difference_quantity = :differenceQuantity,
				    remarks = :remarks
				WHERE id = :itemId
				  AND stocktaking_order_id = :stocktakingOrderId
				""", new MapSqlParameterSource()
				.addValue("itemId", itemId)
				.addValue("stocktakingOrderId", stocktakingOrderId)
				.addValue("countedQuantity", countedQuantity)
				.addValue("differenceQuantity", differenceQuantity)
				.addValue("remarks", remarks));
	}

	public void updateOrderStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_stocktaking_order
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
