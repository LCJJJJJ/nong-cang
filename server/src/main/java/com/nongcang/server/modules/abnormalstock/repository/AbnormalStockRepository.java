package com.nongcang.server.modules.abnormalstock.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AbnormalStockRepository {

	private static final RowMapper<AbnormalStockEntity> ABNORMAL_STOCK_ROW_MAPPER = new RowMapper<>() {
		@Override
		public AbnormalStockEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new AbnormalStockEntity(
					rs.getLong("id"),
					rs.getString("abnormal_code"),
					rs.getLong("quality_inspection_id"),
					rs.getString("inspection_code"),
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
					rs.getBigDecimal("locked_quantity"),
					rs.getInt("status"),
					rs.getString("reason"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("processed_at")),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AbnormalStockRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<AbnormalStockEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  abs.id,
				  abs.abnormal_code,
				  abs.quality_inspection_id,
				  abs.inspection_code,
				  abs.product_id,
				  pa.product_code,
				  pa.product_name,
				  pu.unit_name,
				  pu.unit_symbol,
				  abs.warehouse_id,
				  w.warehouse_name,
				  abs.zone_id,
				  wz.zone_name,
				  abs.location_id,
				  wl.location_name,
				  abs.locked_quantity,
				  abs.status,
				  abs.reason,
				  abs.remarks,
				  abs.processed_at,
				  abs.created_at,
				  abs.updated_at
				FROM abnormal_stock abs
				JOIN product_archive pa ON pa.id = abs.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = abs.warehouse_id
				JOIN warehouse_zone wz ON wz.id = abs.zone_id
				JOIN warehouse_location wl ON wl.id = abs.location_id
				ORDER BY abs.created_at DESC, abs.id DESC
				""", ABNORMAL_STOCK_ROW_MAPPER);
	}

	public Optional<AbnormalStockEntity> findById(Long id) {
		List<AbnormalStockEntity> list = namedParameterJdbcTemplate.query("""
				SELECT
				  abs.id,
				  abs.abnormal_code,
				  abs.quality_inspection_id,
				  abs.inspection_code,
				  abs.product_id,
				  pa.product_code,
				  pa.product_name,
				  pu.unit_name,
				  pu.unit_symbol,
				  abs.warehouse_id,
				  w.warehouse_name,
				  abs.zone_id,
				  wz.zone_name,
				  abs.location_id,
				  wl.location_name,
				  abs.locked_quantity,
				  abs.status,
				  abs.reason,
				  abs.remarks,
				  abs.processed_at,
				  abs.created_at,
				  abs.updated_at
				FROM abnormal_stock abs
				JOIN product_archive pa ON pa.id = abs.product_id
				JOIN product_unit pu ON pu.id = pa.unit_id
				JOIN warehouse w ON w.id = abs.warehouse_id
				JOIN warehouse_zone wz ON wz.id = abs.zone_id
				JOIN warehouse_location wl ON wl.id = abs.location_id
				WHERE abs.id = :id
				""", new MapSqlParameterSource("id", id), ABNORMAL_STOCK_ROW_MAPPER);
		return list.stream().findFirst();
	}

	public boolean existsByAbnormalCode(String abnormalCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM abnormal_stock
				WHERE abnormal_code = :abnormalCode
				""", new MapSqlParameterSource("abnormalCode", abnormalCode), Integer.class);
		return count != null && count > 0;
	}

	public long insert(
			String abnormalCode,
			Long qualityInspectionId,
			String inspectionCode,
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal lockedQuantity,
			Integer status,
			String reason,
			String remarks) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO abnormal_stock (
				  abnormal_code,
				  quality_inspection_id,
				  inspection_code,
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  locked_quantity,
				  status,
				  reason,
				  remarks
				)
				VALUES (
				  :abnormalCode,
				  :qualityInspectionId,
				  :inspectionCode,
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :lockedQuantity,
				  :status,
				  :reason,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("abnormalCode", abnormalCode)
				.addValue("qualityInspectionId", qualityInspectionId)
				.addValue("inspectionCode", inspectionCode)
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("lockedQuantity", lockedQuantity)
				.addValue("status", status)
				.addValue("reason", reason)
				.addValue("remarks", remarks), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void updateStatus(Long id, Integer status, LocalDateTime processedAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE abnormal_stock
				SET status = :status,
				    processed_at = :processedAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status)
				.addValue("processedAt", processedAt));
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
