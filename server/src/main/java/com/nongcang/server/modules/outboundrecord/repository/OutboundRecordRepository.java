package com.nongcang.server.modules.outboundrecord.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.outboundrecord.domain.entity.OutboundRecordEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboundRecordRepository {

	private static final String OUTBOUND_RECORD_SELECT = """
			SELECT
			  orc.id,
			  orc.record_code,
			  orc.outbound_order_id,
			  oo.order_code,
			  orc.outbound_task_id,
			  orc.customer_id,
			  c.customer_name,
			  orc.warehouse_id,
			  w.warehouse_name,
			  orc.zone_id,
			  wz.zone_name,
			  orc.location_id,
			  wl.location_name,
			  orc.product_id,
			  pa.product_code,
			  pa.product_name,
			  orc.quantity,
			  orc.occurred_at,
			  orc.remarks,
			  orc.created_at
			FROM outbound_record orc
			JOIN outbound_order oo ON oo.id = orc.outbound_order_id
			JOIN customer c ON c.id = orc.customer_id
			JOIN warehouse w ON w.id = orc.warehouse_id
			JOIN warehouse_zone wz ON wz.id = orc.zone_id
			JOIN warehouse_location wl ON wl.id = orc.location_id
			JOIN product_archive pa ON pa.id = orc.product_id
			""";

	private static final RowMapper<OutboundRecordEntity> OUTBOUND_RECORD_ROW_MAPPER = new RowMapper<>() {
		@Override
		public OutboundRecordEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new OutboundRecordEntity(
					rs.getLong("id"),
					rs.getString("record_code"),
					rs.getLong("outbound_order_id"),
					rs.getString("order_code"),
					rs.getLong("outbound_task_id"),
					rs.getLong("customer_id"),
					rs.getString("customer_name"),
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

	public OutboundRecordRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<OutboundRecordEntity> findAll() {
		return namedParameterJdbcTemplate.query(OUTBOUND_RECORD_SELECT + """
				ORDER BY orc.occurred_at DESC, orc.id DESC
				""", OUTBOUND_RECORD_ROW_MAPPER);
	}

	public boolean existsByRecordCode(String recordCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM outbound_record
				WHERE record_code = :recordCode
				""", new MapSqlParameterSource("recordCode", recordCode), Integer.class);
		return count != null && count > 0;
	}

	public void insertRecord(
			String recordCode,
			Long outboundOrderId,
			Long outboundTaskId,
			Long customerId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			Long productId,
			BigDecimal quantity,
			LocalDateTime occurredAt,
			String remarks) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO outbound_record (
				  record_code,
				  outbound_order_id,
				  outbound_task_id,
				  customer_id,
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
				  :outboundOrderId,
				  :outboundTaskId,
				  :customerId,
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
				.addValue("outboundOrderId", outboundOrderId)
				.addValue("outboundTaskId", outboundTaskId)
				.addValue("customerId", customerId)
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
