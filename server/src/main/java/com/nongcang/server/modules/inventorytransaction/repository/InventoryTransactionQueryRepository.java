package com.nongcang.server.modules.inventorytransaction.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.inventorytransaction.domain.entity.InventoryTransactionEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryTransactionQueryRepository {

	private static final RowMapper<InventoryTransactionEntity> INVENTORY_TRANSACTION_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryTransactionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryTransactionEntity(
					rs.getLong("id"),
					rs.getString("transaction_code"),
					rs.getString("transaction_type"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("quantity"),
					rs.getString("source_type"),
					rs.getLong("source_id"),
					toLocalDateTime(rs.getTimestamp("occurred_at")),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryTransactionQueryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InventoryTransactionEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  it.id,
				  it.transaction_code,
				  it.transaction_type,
				  it.product_id,
				  pa.product_code,
				  pa.product_name,
				  it.warehouse_id,
				  w.warehouse_name,
				  it.zone_id,
				  wz.zone_name,
				  it.location_id,
				  wl.location_name,
				  it.quantity,
				  it.source_type,
				  it.source_id,
				  it.occurred_at,
				  it.remarks,
				  it.created_at
				FROM inventory_transaction it
				JOIN product_archive pa ON pa.id = it.product_id
				JOIN warehouse w ON w.id = it.warehouse_id
				JOIN warehouse_zone wz ON wz.id = it.zone_id
				JOIN warehouse_location wl ON wl.id = it.location_id
				ORDER BY it.occurred_at DESC, it.id DESC
				""", INVENTORY_TRANSACTION_ROW_MAPPER);
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
