package com.nongcang.server.modules.inboundorder.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderEntity;
import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderItemEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InboundOrderRepository {

	private static final String INBOUND_ORDER_SELECT = """
			SELECT
			  io.id,
			  io.order_code,
			  io.supplier_id,
			  s.supplier_name,
			  io.warehouse_id,
			  w.warehouse_name,
			  io.expected_arrival_at,
			  io.actual_arrival_at,
			  io.total_item_count,
			  io.total_quantity,
			  io.status,
			  io.remarks,
			  io.created_at,
			  io.updated_at
			FROM inbound_order io
			JOIN supplier s ON s.id = io.supplier_id
			JOIN warehouse w ON w.id = io.warehouse_id
			""";

	private static final RowMapper<InboundOrderEntity> INBOUND_ORDER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InboundOrderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InboundOrderEntity(
					rs.getLong("id"),
					rs.getString("order_code"),
					rs.getLong("supplier_id"),
					rs.getString("supplier_name"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					toLocalDateTime(rs.getTimestamp("expected_arrival_at")),
					toLocalDateTime(rs.getTimestamp("actual_arrival_at")),
					rs.getInt("total_item_count"),
					rs.getBigDecimal("total_quantity"),
					rs.getInt("status"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private static final RowMapper<InboundOrderItemEntity> INBOUND_ORDER_ITEM_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InboundOrderItemEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InboundOrderItemEntity(
					rs.getLong("id"),
					rs.getLong("inbound_order_id"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("product_specification"),
					rs.getLong("unit_id"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getBigDecimal("quantity"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InboundOrderRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InboundOrderEntity> findAll() {
		return namedParameterJdbcTemplate.query(INBOUND_ORDER_SELECT + """
				ORDER BY io.created_at DESC, io.id DESC
				""", INBOUND_ORDER_ROW_MAPPER);
	}

	public Optional<InboundOrderEntity> findById(Long id) {
		List<InboundOrderEntity> inboundOrders = namedParameterJdbcTemplate.query(INBOUND_ORDER_SELECT + """
				WHERE io.id = :id
				""", new MapSqlParameterSource("id", id), INBOUND_ORDER_ROW_MAPPER);
		return inboundOrders.stream().findFirst();
	}

	public List<InboundOrderItemEntity> findItemsByOrderId(Long inboundOrderId) {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  ioi.id,
				  ioi.inbound_order_id,
				  ioi.product_id,
				  pa.product_code,
				  pa.product_name,
				  pa.product_specification,
				  pa.unit_id,
				  pu.unit_name,
				  pu.unit_symbol,
				  ioi.quantity,
				  ioi.sort_order,
				  ioi.remarks,
				  ioi.created_at,
				  ioi.updated_at
				FROM inbound_order_item ioi
				JOIN product_archive pa ON pa.id = ioi.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				WHERE ioi.inbound_order_id = :inboundOrderId
				ORDER BY ioi.sort_order ASC, ioi.id ASC
				""", new MapSqlParameterSource("inboundOrderId", inboundOrderId), INBOUND_ORDER_ITEM_ROW_MAPPER);
	}

	public long insertOrder(InboundOrderEntity inboundOrderEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO inbound_order (
				  order_code,
				  supplier_id,
				  warehouse_id,
				  expected_arrival_at,
				  actual_arrival_at,
				  total_item_count,
				  total_quantity,
				  status,
				  remarks
				)
				VALUES (
				  :orderCode,
				  :supplierId,
				  :warehouseId,
				  :expectedArrivalAt,
				  :actualArrivalAt,
				  :totalItemCount,
				  :totalQuantity,
				  :status,
				  :remarks
				)
				""", buildOrderParameters(inboundOrderEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void updateOrder(InboundOrderEntity inboundOrderEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE inbound_order
				SET supplier_id = :supplierId,
				    warehouse_id = :warehouseId,
				    expected_arrival_at = :expectedArrivalAt,
				    actual_arrival_at = :actualArrivalAt,
				    total_item_count = :totalItemCount,
				    total_quantity = :totalQuantity,
				    status = :status,
				    remarks = :remarks
				WHERE id = :id
				""", buildOrderParameters(inboundOrderEntity).addValue("id", inboundOrderEntity.id()));
	}

	public void updateStatus(Long id, Integer status, LocalDateTime actualArrivalAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE inbound_order
				SET status = :status,
				    actual_arrival_at = :actualArrivalAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("actualArrivalAt", actualArrivalAt));
	}

	public void insertOrderItems(Long inboundOrderId, List<InboundOrderItemEntity> items) {
		for (InboundOrderItemEntity item : items) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO inbound_order_item (
					  inbound_order_id,
					  product_id,
					  quantity,
					  sort_order,
					  remarks
					)
					VALUES (
					  :inboundOrderId,
					  :productId,
					  :quantity,
					  :sortOrder,
					  :remarks
					)
					""", new MapSqlParameterSource()
					.addValue("inboundOrderId", inboundOrderId)
					.addValue("productId", item.productId())
					.addValue("quantity", item.quantity())
					.addValue("sortOrder", item.sortOrder())
					.addValue("remarks", item.remarks()));
		}
	}

	public void deleteOrderItems(Long inboundOrderId) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM inbound_order_item
				WHERE inbound_order_id = :inboundOrderId
				""", new MapSqlParameterSource("inboundOrderId", inboundOrderId));
	}

	private MapSqlParameterSource buildOrderParameters(InboundOrderEntity inboundOrderEntity) {
		return new MapSqlParameterSource()
				.addValue("orderCode", inboundOrderEntity.orderCode())
				.addValue("supplierId", inboundOrderEntity.supplierId())
				.addValue("warehouseId", inboundOrderEntity.warehouseId())
				.addValue("expectedArrivalAt", inboundOrderEntity.expectedArrivalAt())
				.addValue("actualArrivalAt", inboundOrderEntity.actualArrivalAt())
				.addValue("totalItemCount", inboundOrderEntity.totalItemCount())
				.addValue("totalQuantity", inboundOrderEntity.totalQuantity())
				.addValue("status", inboundOrderEntity.status())
				.addValue("remarks", inboundOrderEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	public static BigDecimal sumQuantity(List<InboundOrderItemEntity> items) {
		return items.stream()
				.map(InboundOrderItemEntity::quantity)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
