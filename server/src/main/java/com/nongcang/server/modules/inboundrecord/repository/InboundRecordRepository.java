package com.nongcang.server.modules.inboundrecord.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.inboundrecord.domain.entity.InboundRecordEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InboundRecordRepository {

	private static final String INBOUND_RECORD_SELECT = """
			SELECT
			  ir.id,
			  ir.record_code,
			  ir.inbound_order_id,
			  io.order_code,
			  ir.putaway_task_id,
			  ir.supplier_id,
			  s.supplier_name,
			  ir.warehouse_id,
			  w.warehouse_name,
			  ir.zone_id,
			  wz.zone_name,
			  ir.location_id,
			  wl.location_name,
			  ir.product_id,
			  pa.product_code,
			  pa.product_name,
			  ir.quantity,
			  ir.occurred_at,
			  ir.remarks,
			  ir.created_at
			FROM inbound_record ir
			JOIN inbound_order io ON io.id = ir.inbound_order_id
			JOIN supplier s ON s.id = ir.supplier_id
			JOIN warehouse w ON w.id = ir.warehouse_id
			JOIN warehouse_zone wz ON wz.id = ir.zone_id
			JOIN warehouse_location wl ON wl.id = ir.location_id
			JOIN product_archive pa ON pa.id = ir.product_id
			""";

	private static final RowMapper<InboundRecordEntity> INBOUND_RECORD_ROW_MAPPER = new RowMapper<>() {
		@Override
		public InboundRecordEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new InboundRecordEntity(
					rs.getLong("id"),
					rs.getString("record_code"),
					rs.getLong("inbound_order_id"),
					rs.getString("order_code"),
					rs.getLong("putaway_task_id"),
					rs.getLong("supplier_id"),
					rs.getString("supplier_name"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getBigDecimal("quantity"),
					toLocalDateTime(rs.getTimestamp("occurred_at")),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public InboundRecordRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<InboundRecordEntity> findAll() {
		return namedParameterJdbcTemplate.query(INBOUND_RECORD_SELECT + """
				ORDER BY ir.occurred_at DESC, ir.id DESC
				""", INBOUND_RECORD_ROW_MAPPER);
	}

	public boolean existsByRecordCode(String recordCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM inbound_record
				WHERE record_code = :recordCode
				""", new MapSqlParameterSource("recordCode", recordCode), Integer.class);
		return count != null && count > 0;
	}

	public void insertRecord(
			String recordCode,
			Long inboundOrderId,
			Long putawayTaskId,
			Long supplierId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			Long productId,
			BigDecimal quantity,
			LocalDateTime occurredAt,
			String remarks) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO inbound_record (
				  record_code,
				  inbound_order_id,
				  putaway_task_id,
				  supplier_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  product_id,
				  quantity,
				  occurred_at,
				  remarks
				)
				VALUES (
				  :recordCode,
				  :inboundOrderId,
				  :putawayTaskId,
				  :supplierId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :productId,
				  :quantity,
				  :occurredAt,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("recordCode", recordCode)
				.addValue("inboundOrderId", inboundOrderId)
				.addValue("putawayTaskId", putawayTaskId)
				.addValue("supplierId", supplierId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("productId", productId)
				.addValue("quantity", quantity)
				.addValue("occurredAt", occurredAt)
				.addValue("remarks", remarks));
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
