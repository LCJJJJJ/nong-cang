package com.nongcang.server.modules.outboundtask.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.outboundtask.domain.entity.OutboundTaskEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboundTaskRepository {

	private static final String OUTBOUND_TASK_SELECT = """
			SELECT
			  ot.id,
			  ot.task_code,
			  ot.outbound_order_id,
			  oo.order_code,
			  ot.outbound_order_item_id,
			  ot.customer_id,
			  c.customer_name,
			  ot.warehouse_id,
			  w.warehouse_name,
			  ot.zone_id,
			  wz.zone_name,
			  ot.location_id,
			  wl.location_name,
			  ot.product_id,
			  pa.product_code,
			  pa.product_name,
			  ot.quantity,
			  ot.status,
			  ot.remarks,
			  ot.picked_at,
			  ot.completed_at,
			  ot.created_at,
			  ot.updated_at
			FROM outbound_task ot
			JOIN outbound_order oo ON oo.id = ot.outbound_order_id
			JOIN customer c ON c.id = ot.customer_id
			JOIN warehouse w ON w.id = ot.warehouse_id
			JOIN product_archive pa ON pa.id = ot.product_id
			LEFT JOIN warehouse_zone wz ON wz.id = ot.zone_id
			LEFT JOIN warehouse_location wl ON wl.id = ot.location_id
			""";

	private static final RowMapper<OutboundTaskEntity> OUTBOUND_TASK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public OutboundTaskEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new OutboundTaskEntity(
					rs.getLong("id"),
					rs.getString("task_code"),
					rs.getLong("outbound_order_id"),
					rs.getString("order_code"),
					rs.getLong("outbound_order_item_id"),
					rs.getLong("customer_id"),
					rs.getString("customer_name"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getObject("zone_id", Long.class),
					rs.getString("zone_name"),
					rs.getObject("location_id", Long.class),
					rs.getString("location_name"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getBigDecimal("quantity"),
					rs.getInt("status"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("picked_at")),
					toLocalDateTime(rs.getTimestamp("completed_at")),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public OutboundTaskRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<OutboundTaskEntity> findAll() {
		return namedParameterJdbcTemplate.query(OUTBOUND_TASK_SELECT + """
				ORDER BY ot.created_at DESC, ot.id DESC
				""", OUTBOUND_TASK_ROW_MAPPER);
	}

	public Optional<OutboundTaskEntity> findById(Long id) {
		List<OutboundTaskEntity> outboundTasks = namedParameterJdbcTemplate.query(OUTBOUND_TASK_SELECT + """
				WHERE ot.id = :id
				""", new MapSqlParameterSource("id", id), OUTBOUND_TASK_ROW_MAPPER);
		return outboundTasks.stream().findFirst();
	}

	public List<OutboundTaskEntity> findByOutboundOrderId(Long outboundOrderId) {
		return namedParameterJdbcTemplate.query(OUTBOUND_TASK_SELECT + """
				WHERE ot.outbound_order_id = :outboundOrderId
				ORDER BY ot.created_at ASC, ot.id ASC
				""", new MapSqlParameterSource("outboundOrderId", outboundOrderId), OUTBOUND_TASK_ROW_MAPPER);
	}

	public boolean existsByTaskCode(String taskCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM outbound_task
				WHERE task_code = :taskCode
				""", new MapSqlParameterSource("taskCode", taskCode), Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByOutboundOrderId(Long outboundOrderId) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM outbound_task
				WHERE outbound_order_id = :outboundOrderId
				""", new MapSqlParameterSource("outboundOrderId", outboundOrderId), Integer.class);
		return count != null && count > 0;
	}

	public void insertTasks(List<OutboundTaskEntity> tasks) {
		for (OutboundTaskEntity task : tasks) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO outbound_task (
					  task_code,
					  outbound_order_id,
					  outbound_order_item_id,
					  customer_id,
					  warehouse_id,
					  zone_id,
					  location_id,
					  product_id,
					  quantity,
					  status,
					  remarks,
					  picked_at,
					  completed_at
					)
					VALUES (
					  :taskCode,
					  :outboundOrderId,
					  :outboundOrderItemId,
					  :customerId,
					  :warehouseId,
					  :zoneId,
					  :locationId,
					  :productId,
					  :quantity,
					  :status,
					  :remarks,
					  :pickedAt,
					  :completedAt
					)
					""", buildParameters(task));
		}
	}

	public void assignLocation(Long id, Long zoneId, Long locationId, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_task
				SET zone_id = :zoneId,
				    location_id = :locationId,
				    status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("status", status));
	}

	public void updatePicked(Long id, Integer status, LocalDateTime pickedAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_task
				SET status = :status,
				    picked_at = :pickedAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("pickedAt", pickedAt));
	}

	public void updateCompleted(Long id, Integer status, LocalDateTime completedAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_task
				SET status = :status,
				    completed_at = :completedAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("completedAt", completedAt));
	}

	public void updateCancelled(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_task
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	private MapSqlParameterSource buildParameters(OutboundTaskEntity task) {
		return new MapSqlParameterSource()
				.addValue("taskCode", task.taskCode())
				.addValue("outboundOrderId", task.outboundOrderId())
				.addValue("outboundOrderItemId", task.outboundOrderItemId())
				.addValue("customerId", task.customerId())
				.addValue("warehouseId", task.warehouseId())
				.addValue("zoneId", task.zoneId())
				.addValue("locationId", task.locationId())
				.addValue("productId", task.productId())
				.addValue("quantity", task.quantity())
				.addValue("status", task.status())
				.addValue("remarks", task.remarks())
				.addValue("pickedAt", task.pickedAt())
				.addValue("completedAt", task.completedAt());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
