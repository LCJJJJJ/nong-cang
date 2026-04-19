package com.nongcang.server.modules.inventorysupport.repository;

import java.math.BigDecimal;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryStockRepository {

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
}
