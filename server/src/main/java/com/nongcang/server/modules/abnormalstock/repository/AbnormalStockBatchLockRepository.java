package com.nongcang.server.modules.abnormalstock.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockBatchLockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AbnormalStockBatchLockRepository {

	private static final RowMapper<AbnormalStockBatchLockEntity> ABNORMAL_STOCK_BATCH_LOCK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AbnormalStockBatchLockEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AbnormalStockBatchLockEntity(
					rs.getLong("id"),
					rs.getLong("abnormal_stock_id"),
					rs.getLong("inventory_batch_id"),
					rs.getBigDecimal("locked_quantity"),
					toLocalDateTime(rs.getTimestamp("created_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AbnormalStockBatchLockRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void insertLocks(List<AbnormalStockBatchLockEntity> locks) {
		for (AbnormalStockBatchLockEntity lock : locks) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO abnormal_stock_batch_lock (
					  abnormal_stock_id,
					  inventory_batch_id,
					  locked_quantity
					)
					VALUES (
					  :abnormalStockId,
					  :inventoryBatchId,
					  :lockedQuantity
					)
					""", new MapSqlParameterSource()
					.addValue("abnormalStockId", lock.abnormalStockId())
					.addValue("inventoryBatchId", lock.inventoryBatchId())
					.addValue("lockedQuantity", lock.lockedQuantity()));
		}
	}

	public List<AbnormalStockBatchLockEntity> findByAbnormalStockId(Long abnormalStockId) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM abnormal_stock_batch_lock
				WHERE abnormal_stock_id = :abnormalStockId
				ORDER BY id ASC
				""", new MapSqlParameterSource("abnormalStockId", abnormalStockId), ABNORMAL_STOCK_BATCH_LOCK_ROW_MAPPER);
	}

	public BigDecimal sumActiveLockedQuantityByBatchId(Long inventoryBatchId, Long excludeAbnormalStockId) {
		BigDecimal lockedQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(asbl.locked_quantity), 0)
				FROM abnormal_stock_batch_lock asbl
				JOIN abnormal_stock abs ON abs.id = asbl.abnormal_stock_id
				WHERE asbl.inventory_batch_id = :inventoryBatchId
				  AND abs.status = 1
				  AND abs.id <> :excludeAbnormalStockId
				""", new MapSqlParameterSource()
				.addValue("inventoryBatchId", inventoryBatchId)
				.addValue("excludeAbnormalStockId", excludeAbnormalStockId == null ? -1L : excludeAbnormalStockId), BigDecimal.class);
		return lockedQuantity == null ? BigDecimal.ZERO : lockedQuantity;
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
