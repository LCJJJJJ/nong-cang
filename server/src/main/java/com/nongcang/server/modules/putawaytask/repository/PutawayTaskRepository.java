package com.nongcang.server.modules.putawaytask.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.putawaytask.domain.entity.PutawayTaskEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PutawayTaskRepository {

	private static final String PUTAWAY_TASK_SELECT = """
			SELECT
			  pt.id,
			  pt.task_code,
			  pt.inbound_order_id,
			  io.order_code,
			  pt.inbound_order_item_id,
			  pt.supplier_id,
			  s.supplier_name,
			  pt.warehouse_id,
			  w.warehouse_name,
			  pt.zone_id,
			  wz.zone_name,
			  pt.location_id,
			  wl.location_name,
			  pt.product_id,
			  pa.product_code,
			  pa.product_name,
			  pt.quantity,
			  pt.status,
			  pt.remarks,
			  pt.completed_at,
			  pt.created_at,
			  pt.updated_at
			FROM putaway_task pt
			JOIN inbound_order io ON io.id = pt.inbound_order_id
			JOIN supplier s ON s.id = pt.supplier_id
			JOIN warehouse w ON w.id = pt.warehouse_id
			JOIN product_archive pa ON pa.id = pt.product_id
			LEFT JOIN warehouse_zone wz ON wz.id = pt.zone_id
			LEFT JOIN warehouse_location wl ON wl.id = pt.location_id
			""";

	private static final RowMapper<PutawayTaskEntity> PUTAWAY_TASK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public PutawayTaskEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new PutawayTaskEntity(
					rs.getLong("id"),
					rs.getString("task_code"),
					rs.getLong("inbound_order_id"),
					rs.getString("order_code"),
					rs.getLong("inbound_order_item_id"),
					rs.getLong("supplier_id"),
					rs.getString("supplier_name"),
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
					toLocalDateTime(rs.getTimestamp("completed_at")),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public PutawayTaskRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<PutawayTaskEntity> findAll() {
		return namedParameterJdbcTemplate.query(PUTAWAY_TASK_SELECT + """
				ORDER BY pt.created_at DESC, pt.id DESC
				""", PUTAWAY_TASK_ROW_MAPPER);
	}

	public Optional<PutawayTaskEntity> findById(Long id) {
		List<PutawayTaskEntity> putawayTasks = namedParameterJdbcTemplate.query(PUTAWAY_TASK_SELECT + """
				WHERE pt.id = :id
				""", new MapSqlParameterSource("id", id), PUTAWAY_TASK_ROW_MAPPER);
		return putawayTasks.stream().findFirst();
	}

	public boolean existsByTaskCode(String taskCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM putaway_task
				WHERE task_code = :taskCode
				""", new MapSqlParameterSource("taskCode", taskCode), Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByInboundOrderId(Long inboundOrderId) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM putaway_task
				WHERE inbound_order_id = :inboundOrderId
				""", new MapSqlParameterSource("inboundOrderId", inboundOrderId), Integer.class);
		return count != null && count > 0;
	}

	public void insertTasks(List<PutawayTaskEntity> tasks) {
		for (PutawayTaskEntity task : tasks) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO putaway_task (
					  task_code,
					  inbound_order_id,
					  inbound_order_item_id,
					  supplier_id,
					  warehouse_id,
					  zone_id,
					  location_id,
					  product_id,
					  quantity,
					  status,
					  remarks,
					  completed_at
					)
					VALUES (
					  :taskCode,
					  :inboundOrderId,
					  :inboundOrderItemId,
					  :supplierId,
					  :warehouseId,
					  :zoneId,
					  :locationId,
					  :productId,
					  :quantity,
					  :status,
					  :remarks,
					  :completedAt
					)
					""", buildParameters(task));
		}
	}

	public void assignLocation(Long id, Long zoneId, Long locationId, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE putaway_task
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

	public void updateStatus(Long id, Integer status, LocalDateTime completedAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE putaway_task
				SET status = :status,
				    completed_at = :completedAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("completedAt", completedAt));
	}

	public long countOpenTasksByOrderId(Long inboundOrderId) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM putaway_task
				WHERE inbound_order_id = :inboundOrderId
				  AND status <> 3
				  AND status <> 4
				""", new MapSqlParameterSource("inboundOrderId", inboundOrderId), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(PutawayTaskEntity task) {
		return new MapSqlParameterSource()
				.addValue("taskCode", task.taskCode())
				.addValue("inboundOrderId", task.inboundOrderId())
				.addValue("inboundOrderItemId", task.inboundOrderItemId())
				.addValue("supplierId", task.supplierId())
				.addValue("warehouseId", task.warehouseId())
				.addValue("zoneId", task.zoneId())
				.addValue("locationId", task.locationId())
				.addValue("productId", task.productId())
				.addValue("quantity", task.quantity())
				.addValue("status", task.status())
				.addValue("remarks", task.remarks())
				.addValue("completedAt", task.completedAt());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
