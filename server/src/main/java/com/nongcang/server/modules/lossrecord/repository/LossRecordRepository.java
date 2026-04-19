package com.nongcang.server.modules.lossrecord.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.nongcang.server.modules.lossrecord.domain.entity.LossRecordEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class LossRecordRepository {

	private static final RowMapper<LossRecordEntity> LOSS_RECORD_ROW_MAPPER = new RowMapper<>() {
		@Override
		public LossRecordEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new LossRecordEntity(
					rs.getLong("id"),
					rs.getString("loss_code"),
					rs.getString("source_type"),
					rs.getObject("source_id", Long.class),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("quantity"),
					rs.getString("loss_reason"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public LossRecordRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public boolean existsByLossCode(String lossCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM loss_record
				WHERE loss_code = :lossCode
				""", new MapSqlParameterSource("lossCode", lossCode), Integer.class);
		return count != null && count > 0;
	}

	public List<LossRecordEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  lr.id,
				  lr.loss_code,
				  lr.source_type,
				  lr.source_id,
				  lr.product_id,
				  pa.product_code,
				  pa.product_name,
				  pu.unit_name,
				  pu.unit_symbol,
				  lr.warehouse_id,
				  w.warehouse_name,
				  lr.zone_id,
				  wz.zone_name,
				  lr.location_id,
				  wl.location_name,
				  lr.quantity,
				  lr.loss_reason,
				  lr.remarks,
				  lr.created_at
				FROM loss_record lr
				JOIN product_archive pa ON pa.id = lr.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = lr.warehouse_id
				JOIN warehouse_zone wz ON wz.id = lr.zone_id
				JOIN warehouse_location wl ON wl.id = lr.location_id
				ORDER BY lr.created_at DESC, lr.id DESC
				""", LOSS_RECORD_ROW_MAPPER);
	}

	public long insert(
			String lossCode,
			String sourceType,
			Long sourceId,
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal quantity,
			String lossReason,
			String remarks) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO loss_record (
				  loss_code,
				  source_type,
				  source_id,
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  quantity,
				  loss_reason,
				  remarks
				)
				VALUES (
				  :lossCode,
				  :sourceType,
				  :sourceId,
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :quantity,
				  :lossReason,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("lossCode", lossCode)
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId)
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("quantity", quantity)
				.addValue("lossReason", lossReason)
				.addValue("remarks", remarks), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
