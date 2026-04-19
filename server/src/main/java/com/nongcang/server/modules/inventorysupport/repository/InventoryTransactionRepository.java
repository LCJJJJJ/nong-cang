package com.nongcang.server.modules.inventorysupport.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryTransactionRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryTransactionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void insertTransaction(
			String transactionCode,
			String transactionType,
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal quantity,
			String sourceType,
			Long sourceId,
			LocalDateTime occurredAt,
			String remarks) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_transaction (
				  transaction_code,
				  transaction_type,
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  quantity,
				  source_type,
				  source_id,
				  occurred_at,
				  remarks
				)
				VALUES (
				  :transactionCode,
				  :transactionType,
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :quantity,
				  :sourceType,
				  :sourceId,
				  :occurredAt,
				  :remarks
				)
		""", new MapSqlParameterSource()
				.addValue("transactionCode", transactionCode)
				.addValue("transactionType", transactionType)
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("quantity", quantity)
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId)
				.addValue("occurredAt", occurredAt)
				.addValue("remarks", remarks));
	}
}
