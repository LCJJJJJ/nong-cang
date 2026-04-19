package com.nongcang.server.modules.lossrecord.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class LossRecordRepository {

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
}
