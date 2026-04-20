package com.nongcang.server.modules.outboundtask.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.outboundtask.domain.entity.OutboundTaskBatchAllocationEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboundTaskBatchAllocationRepository {

	private static final RowMapper<OutboundTaskBatchAllocationEntity> OUTBOUND_TASK_BATCH_ALLOCATION_ROW_MAPPER =
			new RowMapper<>() {
				@Override
				public OutboundTaskBatchAllocationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new OutboundTaskBatchAllocationEntity(
							rs.getLong("id"),
							rs.getLong("outbound_task_id"),
							rs.getLong("inventory_batch_id"),
							rs.getBigDecimal("allocated_quantity"),
							toLocalDateTime(rs.getTimestamp("created_at")));
				}
			};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public OutboundTaskBatchAllocationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<OutboundTaskBatchAllocationEntity> findByOutboundTaskId(Long outboundTaskId) {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM outbound_task_batch_allocation
				WHERE outbound_task_id = :outboundTaskId
				ORDER BY id ASC
				""", new MapSqlParameterSource("outboundTaskId", outboundTaskId),
				OUTBOUND_TASK_BATCH_ALLOCATION_ROW_MAPPER);
	}

	public void replaceAllocations(Long outboundTaskId, List<OutboundTaskBatchAllocationEntity> allocations) {
		deleteByOutboundTaskId(outboundTaskId);
		for (OutboundTaskBatchAllocationEntity allocation : allocations) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO outbound_task_batch_allocation (
					  outbound_task_id,
					  inventory_batch_id,
					  allocated_quantity
					)
					VALUES (
					  :outboundTaskId,
					  :inventoryBatchId,
					  :allocatedQuantity
					)
					""", new MapSqlParameterSource()
					.addValue("outboundTaskId", allocation.outboundTaskId())
					.addValue("inventoryBatchId", allocation.inventoryBatchId())
					.addValue("allocatedQuantity", allocation.allocatedQuantity()));
		}
	}

	public void deleteByOutboundTaskId(Long outboundTaskId) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM outbound_task_batch_allocation
				WHERE outbound_task_id = :outboundTaskId
				""", new MapSqlParameterSource("outboundTaskId", outboundTaskId));
	}

	public BigDecimal sumReservedQuantityByBatchId(Long inventoryBatchId, Long excludeTaskId) {
		BigDecimal reservedQuantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(otba.allocated_quantity), 0)
				FROM outbound_task_batch_allocation otba
				JOIN outbound_task ot ON ot.id = otba.outbound_task_id
				WHERE otba.inventory_batch_id = :inventoryBatchId
				  AND ot.status IN (2, 3)
				  AND ot.id <> :excludeTaskId
				""", new MapSqlParameterSource()
				.addValue("inventoryBatchId", inventoryBatchId)
				.addValue("excludeTaskId", excludeTaskId == null ? -1L : excludeTaskId), BigDecimal.class);
		return reservedQuantity == null ? BigDecimal.ZERO : reservedQuantity;
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
