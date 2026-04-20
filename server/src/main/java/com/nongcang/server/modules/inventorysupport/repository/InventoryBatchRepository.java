package com.nongcang.server.modules.inventorysupport.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryBatchEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryBatchRepository {

	private static final RowMapper<InventoryBatchEntity> INVENTORY_BATCH_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InventoryBatchEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InventoryBatchEntity(
					rs.getLong("id"),
					rs.getString("batch_code"),
					rs.getString("source_type"),
					rs.getLong("source_id"),
					rs.getLong("product_id"),
					rs.getLong("warehouse_id"),
					rs.getLong("zone_id"),
					rs.getLong("location_id"),
					toLocalDateTime(rs.getTimestamp("base_occurred_at")),
					rs.getInt("shelf_life_days_snapshot"),
					rs.getInt("warning_days_snapshot"),
					toLocalDateTime(rs.getTimestamp("warning_at")),
					toLocalDateTime(rs.getTimestamp("expected_expire_at")),
					rs.getBigDecimal("initial_quantity"),
					rs.getBigDecimal("remaining_quantity"),
					rs.getString("status"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InventoryBatchRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public long insert(InventoryBatchEntity inventoryBatchEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO inventory_batch (
				  batch_code,
				  source_type,
				  source_id,
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  base_occurred_at,
				  shelf_life_days_snapshot,
				  warning_days_snapshot,
				  warning_at,
				  expected_expire_at,
				  initial_quantity,
				  remaining_quantity,
				  status
				)
				VALUES (
				  :batchCode,
				  :sourceType,
				  :sourceId,
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :baseOccurredAt,
				  :shelfLifeDaysSnapshot,
				  :warningDaysSnapshot,
				  :warningAt,
				  :expectedExpireAt,
				  :initialQuantity,
				  :remainingQuantity,
				  :status
				)
				""", buildParameters(inventoryBatchEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public List<InventoryBatchEntity> findActiveByProductId(Long productId) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM inventory_batch
				WHERE product_id = :productId
				  AND status = 'ACTIVE'
				ORDER BY expected_expire_at ASC, id ASC
				""", new MapSqlParameterSource("productId", productId), INVENTORY_BATCH_ROW_MAPPER);
	}

	public List<InventoryBatchEntity> findActiveByProductAndLocation(Long productId, Long warehouseId, Long locationId) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM inventory_batch
				WHERE product_id = :productId
				  AND warehouse_id = :warehouseId
				  AND location_id = :locationId
				  AND status = 'ACTIVE'
				ORDER BY expected_expire_at ASC, id ASC
				""", new MapSqlParameterSource()
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("locationId", locationId), INVENTORY_BATCH_ROW_MAPPER);
	}

	public Optional<InventoryBatchEntity> findById(Long id) {
		List<InventoryBatchEntity> batches = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM inventory_batch
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), INVENTORY_BATCH_ROW_MAPPER);
		return batches.stream().findFirst();
	}

	public Optional<InventoryBatchEntity> findBySource(String sourceType, Long sourceId) {
		List<InventoryBatchEntity> batches = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM inventory_batch
				WHERE source_type = :sourceType
				  AND source_id = :sourceId
				""", new MapSqlParameterSource()
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId), INVENTORY_BATCH_ROW_MAPPER);
		return batches.stream().findFirst();
	}

	public void updateShelfLifeSnapshot(
			Long id,
			Integer shelfLifeDaysSnapshot,
			Integer warningDaysSnapshot,
			LocalDateTime warningAt,
			LocalDateTime expectedExpireAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_batch
				SET shelf_life_days_snapshot = :shelfLifeDaysSnapshot,
				    warning_days_snapshot = :warningDaysSnapshot,
				    warning_at = :warningAt,
				    expected_expire_at = :expectedExpireAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("shelfLifeDaysSnapshot", shelfLifeDaysSnapshot)
				.addValue("warningDaysSnapshot", warningDaysSnapshot)
				.addValue("warningAt", warningAt)
				.addValue("expectedExpireAt", expectedExpireAt));
	}

	public boolean decreaseRemainingQuantity(Long id, BigDecimal quantity) {
		int updatedRows = namedParameterJdbcTemplate.update("""
				UPDATE inventory_batch
				SET remaining_quantity = remaining_quantity - :quantity
				WHERE id = :id
				  AND status = 'ACTIVE'
				  AND remaining_quantity >= :quantity
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("quantity", quantity));
		return updatedRows > 0;
	}

	public void updateStatus(Long id, String status) {
		namedParameterJdbcTemplate.update("""
				UPDATE inventory_batch
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	private MapSqlParameterSource buildParameters(InventoryBatchEntity inventoryBatchEntity) {
		return new MapSqlParameterSource()
				.addValue("batchCode", inventoryBatchEntity.batchCode())
				.addValue("sourceType", inventoryBatchEntity.sourceType())
				.addValue("sourceId", inventoryBatchEntity.sourceId())
				.addValue("productId", inventoryBatchEntity.productId())
				.addValue("warehouseId", inventoryBatchEntity.warehouseId())
				.addValue("zoneId", inventoryBatchEntity.zoneId())
				.addValue("locationId", inventoryBatchEntity.locationId())
				.addValue("baseOccurredAt", inventoryBatchEntity.baseOccurredAt())
				.addValue("shelfLifeDaysSnapshot", inventoryBatchEntity.shelfLifeDaysSnapshot())
				.addValue("warningDaysSnapshot", inventoryBatchEntity.warningDaysSnapshot())
				.addValue("warningAt", inventoryBatchEntity.warningAt())
				.addValue("expectedExpireAt", inventoryBatchEntity.expectedExpireAt())
				.addValue("initialQuantity", inventoryBatchEntity.initialQuantity())
				.addValue("remainingQuantity", inventoryBatchEntity.remainingQuantity())
				.addValue("status", inventoryBatchEntity.status());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
