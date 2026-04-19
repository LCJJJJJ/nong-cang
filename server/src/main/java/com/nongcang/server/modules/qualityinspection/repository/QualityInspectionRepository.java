package com.nongcang.server.modules.qualityinspection.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.qualityinspection.domain.entity.QualityInspectionEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class QualityInspectionRepository {

	private static final String QUALITY_INSPECTION_SELECT = """
			SELECT
			  qi.id,
			  qi.inspection_code,
			  qi.source_type,
			  qi.source_id,
			  qi.source_code,
			  qi.source_label,
			  qi.product_id,
			  pa.product_code,
			  pa.product_name,
			  pu.unit_name,
			  pu.unit_symbol,
			  pu.precision_digits,
			  qi.warehouse_id,
			  w.warehouse_name,
			  qi.zone_id,
			  wz.zone_name,
			  qi.location_id,
			  wl.location_name,
			  qi.inspect_quantity,
			  qi.qualified_quantity,
			  qi.unqualified_quantity,
			  qi.result_status,
			  qi.remarks,
			  qi.created_at,
			  qi.updated_at
			FROM quality_inspection qi
			JOIN product_archive pa ON pa.id = qi.product_id
			JOIN product_unit pu ON pu.id = pa.unit_id
			JOIN warehouse w ON w.id = qi.warehouse_id
			JOIN warehouse_zone wz ON wz.id = qi.zone_id
			JOIN warehouse_location wl ON wl.id = qi.location_id
			""";

	private static final RowMapper<QualityInspectionEntity> QUALITY_INSPECTION_ROW_MAPPER = new RowMapper<>() {
		@Override
		public QualityInspectionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new QualityInspectionEntity(
					rs.getLong("id"),
					rs.getString("inspection_code"),
					rs.getString("source_type"),
					rs.getLong("source_id"),
					rs.getString("source_code"),
					rs.getString("source_label"),
					rs.getLong("product_id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getInt("precision_digits"),
					rs.getLong("warehouse_id"),
					rs.getString("warehouse_name"),
					rs.getLong("zone_id"),
					rs.getString("zone_name"),
					rs.getLong("location_id"),
					rs.getString("location_name"),
					rs.getBigDecimal("inspect_quantity"),
					rs.getBigDecimal("qualified_quantity"),
					rs.getBigDecimal("unqualified_quantity"),
					rs.getInt("result_status"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public QualityInspectionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<QualityInspectionEntity> findAll() {
		return namedParameterJdbcTemplate.query(QUALITY_INSPECTION_SELECT + """
				ORDER BY qi.created_at DESC, qi.id DESC
				""", QUALITY_INSPECTION_ROW_MAPPER);
	}

	public Optional<QualityInspectionEntity> findById(Long id) {
		List<QualityInspectionEntity> list = namedParameterJdbcTemplate.query(QUALITY_INSPECTION_SELECT + """
				WHERE qi.id = :id
				""", new MapSqlParameterSource("id", id), QUALITY_INSPECTION_ROW_MAPPER);
		return list.stream().findFirst();
	}

	public boolean existsByInspectionCode(String inspectionCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM quality_inspection
				WHERE inspection_code = :inspectionCode
				""", new MapSqlParameterSource("inspectionCode", inspectionCode), Integer.class);
		return count != null && count > 0;
	}

	public BigDecimal sumInspectQuantityBySource(String sourceType, Long sourceId) {
		BigDecimal quantity = namedParameterJdbcTemplate.queryForObject("""
				SELECT COALESCE(SUM(inspect_quantity), 0)
				FROM quality_inspection
				WHERE source_type = :sourceType
				  AND source_id = :sourceId
				""", new MapSqlParameterSource()
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId), BigDecimal.class);
		return quantity == null ? BigDecimal.ZERO : quantity;
	}

	public long insert(
			String inspectionCode,
			String sourceType,
			Long sourceId,
			String sourceCode,
			String sourceLabel,
			Long productId,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal inspectQuantity,
			BigDecimal qualifiedQuantity,
			BigDecimal unqualifiedQuantity,
			Integer resultStatus,
			String remarks) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO quality_inspection (
				  inspection_code,
				  source_type,
				  source_id,
				  source_code,
				  source_label,
				  product_id,
				  warehouse_id,
				  zone_id,
				  location_id,
				  inspect_quantity,
				  qualified_quantity,
				  unqualified_quantity,
				  result_status,
				  remarks
				)
				VALUES (
				  :inspectionCode,
				  :sourceType,
				  :sourceId,
				  :sourceCode,
				  :sourceLabel,
				  :productId,
				  :warehouseId,
				  :zoneId,
				  :locationId,
				  :inspectQuantity,
				  :qualifiedQuantity,
				  :unqualifiedQuantity,
				  :resultStatus,
				  :remarks
				)
				""", new MapSqlParameterSource()
				.addValue("inspectionCode", inspectionCode)
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId)
				.addValue("sourceCode", sourceCode)
				.addValue("sourceLabel", sourceLabel)
				.addValue("productId", productId)
				.addValue("warehouseId", warehouseId)
				.addValue("zoneId", zoneId)
				.addValue("locationId", locationId)
				.addValue("inspectQuantity", inspectQuantity)
				.addValue("qualifiedQuantity", qualifiedQuantity)
				.addValue("unqualifiedQuantity", unqualifiedQuantity)
				.addValue("resultStatus", resultStatus)
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
