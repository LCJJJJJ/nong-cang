package com.nongcang.server.modules.outboundorder.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderEntity;
import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderItemEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OutboundOrderRepository {

	private static final String OUTBOUND_ORDER_SELECT = """
			SELECT
			  oo.id,
			  oo.order_code,
			  oo.customer_id,
			  c.customer_name,
			  oo.warehouse_id,
			  w.warehouse_name,
			  oo.expected_delivery_at,
			  oo.actual_outbound_at,
			  oo.total_item_count,
			  oo.total_quantity,
			  oo.status,
			  oo.remarks,
			  oo.created_at,
			  oo.updated_at
			FROM outbound_order oo
			JOIN customer c ON c.id = oo.customer_id
			JOIN warehouse w ON w.id = oo.warehouse_id
			""";

	private static final RowMapper<OutboundOrderEntity> OUTBOUND_ORDER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public OutboundOrderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new OutboundOrderEntity(
					rs.getLong("id"),
					rs.getString("order_code"),
					rs.getLong("customer_id"),
					rs.getString("customer_name"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					toLocalDateTime(rs.getTimestamp("expected_delivery_at")),
					toLocalDateTime(rs.getTimestamp("actual_outbound_at")),
					rs.getInt("total_item_count"),
					rs.getBigDecimal("total_quantity"),
					rs.getInt("status"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private static final RowMapper<OutboundOrderItemEntity> OUTBOUND_ORDER_ITEM_ROW_MAPPER = new RowMapper<>() {
		@Override
		public OutboundOrderItemEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new OutboundOrderItemEntity(
					rs.getLong("id"),
					rs.getLong("outbound_order_id"),
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

	public OutboundOrderRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<OutboundOrderEntity> findAll() {
		return namedParameterJdbcTemplate.query(OUTBOUND_ORDER_SELECT + """
				ORDER BY oo.created_at DESC, oo.id DESC
				""", OUTBOUND_ORDER_ROW_MAPPER);
	}

	public Optional<OutboundOrderEntity> findById(Long id) {
		List<OutboundOrderEntity> outboundOrders = namedParameterJdbcTemplate.query(OUTBOUND_ORDER_SELECT + """
				WHERE oo.id = :id
				""", new MapSqlParameterSource("id", id), OUTBOUND_ORDER_ROW_MAPPER);
		return outboundOrders.stream().findFirst();
	}

	public boolean existsByOrderCode(String orderCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM outbound_order
				WHERE order_code = :orderCode
				""", new MapSqlParameterSource("orderCode", orderCode), Integer.class);
		return count != null && count > 0;
	}

	public List<OutboundOrderItemEntity> findItemsByOrderId(Long outboundOrderId) {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  ooi.id,
				  ooi.outbound_order_id,
				  ooi.product_id,
				  pa.product_code,
				  pa.product_name,
				  pa.product_specification,
				  pa.unit_id,
				  pu.unit_name,
				  pu.unit_symbol,
				  ooi.quantity,
				  ooi.sort_order,
				  ooi.remarks,
				  ooi.created_at,
				  ooi.updated_at
				FROM outbound_order_item ooi
				JOIN product_archive pa ON pa.id = ooi.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				WHERE ooi.outbound_order_id = :outboundOrderId
				ORDER BY ooi.sort_order ASC, ooi.id ASC
				""", new MapSqlParameterSource("outboundOrderId", outboundOrderId), OUTBOUND_ORDER_ITEM_ROW_MAPPER);
	}

	public long insertOrder(OutboundOrderEntity outboundOrderEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO outbound_order (
				  order_code,
				  customer_id,
				  warehouse_id,
				  expected_delivery_at,
				  actual_outbound_at,
				  total_item_count,
				  total_quantity,
				  status,
				  remarks
				)
				VALUES (
				  :orderCode,
				  :customerId,
				  :warehouseId,
				  :expectedDeliveryAt,
				  :actualOutboundAt,
				  :totalItemCount,
				  :totalQuantity,
				  :status,
				  :remarks
				)
				""", buildOrderParameters(outboundOrderEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void updateOrder(OutboundOrderEntity outboundOrderEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_order
				SET customer_id = :customerId,
				    warehouse_id = :warehouseId,
				    expected_delivery_at = :expectedDeliveryAt,
				    actual_outbound_at = :actualOutboundAt,
				    total_item_count = :totalItemCount,
				    total_quantity = :totalQuantity,
				    status = :status,
				    remarks = :remarks
				WHERE id = :id
				""", buildOrderParameters(outboundOrderEntity).addValue("id", outboundOrderEntity.id()));
	}

	public void updateStatus(Long id, Integer status, LocalDateTime actualOutboundAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE outbound_order
				SET status = :status,
				    actual_outbound_at = :actualOutboundAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("actualOutboundAt", actualOutboundAt));
	}

	public void insertOrderItems(Long outboundOrderId, List<OutboundOrderItemEntity> items) {
		for (OutboundOrderItemEntity item : items) {
			namedParameterJdbcTemplate.update("""
					INSERT INTO outbound_order_item (
					  outbound_order_id,
					  product_id,
					  quantity,
					  sort_order,
					  remarks
					)
					VALUES (
					  :outboundOrderId,
					  :productId,
					  :quantity,
					  :sortOrder,
					  :remarks
					)
					""", new MapSqlParameterSource()
					.addValue("outboundOrderId", outboundOrderId)
					.addValue("productId", item.productId())
					.addValue("quantity", item.quantity())
					.addValue("sortOrder", item.sortOrder())
					.addValue("remarks", item.remarks()));
		}
	}

	public void deleteOrderItems(Long outboundOrderId) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM outbound_order_item
				WHERE outbound_order_id = :outboundOrderId
				""", new MapSqlParameterSource("outboundOrderId", outboundOrderId));
	}

	private MapSqlParameterSource buildOrderParameters(OutboundOrderEntity outboundOrderEntity) {
		return new MapSqlParameterSource()
				.addValue("orderCode", outboundOrderEntity.orderCode())
				.addValue("customerId", outboundOrderEntity.customerId())
				.addValue("warehouseId", outboundOrderEntity.warehouseId())
				.addValue("expectedDeliveryAt", outboundOrderEntity.expectedDeliveryAt())
				.addValue("actualOutboundAt", outboundOrderEntity.actualOutboundAt())
				.addValue("totalItemCount", outboundOrderEntity.totalItemCount())
				.addValue("totalQuantity", outboundOrderEntity.totalQuantity())
				.addValue("status", outboundOrderEntity.status())
				.addValue("remarks", outboundOrderEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	public static BigDecimal sumQuantity(List<OutboundOrderItemEntity> items) {
		return items.stream()
				.map(OutboundOrderItemEntity::quantity)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
