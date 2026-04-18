package com.nongcang.server.modules.productarchive.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.productarchive.domain.entity.ProductArchiveEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductArchiveRepository {

	private static final String PRODUCT_ARCHIVE_SELECT = """
			SELECT
			  pa.id,
			  pa.product_code,
			  pa.product_name,
			  pa.product_specification,
			  pa.category_id,
			  pc.category_name,
			  pa.unit_id,
			  pu.unit_name,
			  pu.unit_symbol,
			  pa.origin_id,
			  po.origin_name,
			  pa.storage_condition_id,
			  sc.condition_name AS storage_condition_name,
			  pa.shelf_life_rule_id,
			  slr.rule_name AS shelf_life_rule_name,
			  pa.quality_grade_id,
			  qg.grade_name AS quality_grade_name,
			  pa.status,
			  pa.sort_order,
			  pa.remarks,
			  pa.created_at,
			  pa.updated_at
			FROM product_archive pa
			JOIN product_category pc ON pc.id = pa.category_id
			JOIN product_unit pu ON pu.id = pa.unit_id
			JOIN product_origin po ON po.id = pa.origin_id
			JOIN storage_condition sc ON sc.id = pa.storage_condition_id
			JOIN shelf_life_rule slr ON slr.id = pa.shelf_life_rule_id
			JOIN quality_grade qg ON qg.id = pa.quality_grade_id
			""";

	private static final RowMapper<ProductArchiveEntity> PRODUCT_ARCHIVE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public ProductArchiveEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ProductArchiveEntity(
					rs.getLong("id"),
					rs.getString("product_code"),
					rs.getString("product_name"),
					rs.getString("product_specification"),
					rs.getLong("category_id"),
					rs.getString("category_name"),
					rs.getLong("unit_id"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getLong("origin_id"),
					rs.getString("origin_name"),
					rs.getLong("storage_condition_id"),
					rs.getString("storage_condition_name"),
					rs.getLong("shelf_life_rule_id"),
					rs.getString("shelf_life_rule_name"),
					rs.getLong("quality_grade_id"),
					rs.getString("quality_grade_name"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ProductArchiveRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<ProductArchiveEntity> findAll() {
		return namedParameterJdbcTemplate.query(PRODUCT_ARCHIVE_SELECT + """
				ORDER BY pa.sort_order ASC, pa.id ASC
				""", PRODUCT_ARCHIVE_ROW_MAPPER);
	}

	public Optional<ProductArchiveEntity> findById(Long id) {
		List<ProductArchiveEntity> productArchives = namedParameterJdbcTemplate.query(PRODUCT_ARCHIVE_SELECT + """
				WHERE pa.id = :id
				""", new MapSqlParameterSource("id", id), PRODUCT_ARCHIVE_ROW_MAPPER);
		return productArchives.stream().findFirst();
	}

	public boolean existsByProductCode(String productCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_archive
				WHERE product_code = :productCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("productCode", productCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByProductName(String productName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_archive
				WHERE product_name = :productName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("productName", productName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(ProductArchiveEntity productArchiveEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO product_archive (
				  product_code,
				  product_name,
				  product_specification,
				  category_id,
				  unit_id,
				  origin_id,
				  storage_condition_id,
				  shelf_life_rule_id,
				  quality_grade_id,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :productCode,
				  :productName,
				  :productSpecification,
				  :categoryId,
				  :unitId,
				  :originId,
				  :storageConditionId,
				  :shelfLifeRuleId,
				  :qualityGradeId,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(productArchiveEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(ProductArchiveEntity productArchiveEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_archive
				SET product_name = :productName,
				    product_specification = :productSpecification,
				    category_id = :categoryId,
				    unit_id = :unitId,
				    origin_id = :originId,
				    storage_condition_id = :storageConditionId,
				    shelf_life_rule_id = :shelfLifeRuleId,
				    quality_grade_id = :qualityGradeId,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(productArchiveEntity).addValue("id", productArchiveEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_archive
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM product_archive
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countByCategoryId(Long categoryId) {
		return countByReference("category_id", categoryId);
	}

	public long countByUnitId(Long unitId) {
		return countByReference("unit_id", unitId);
	}

	public long countByOriginId(Long originId) {
		return countByReference("origin_id", originId);
	}

	public long countByStorageConditionId(Long storageConditionId) {
		return countByReference("storage_condition_id", storageConditionId);
	}

	public long countByShelfLifeRuleId(Long shelfLifeRuleId) {
		return countByReference("shelf_life_rule_id", shelfLifeRuleId);
	}

	public long countByQualityGradeId(Long qualityGradeId) {
		return countByReference("quality_grade_id", qualityGradeId);
	}

	private long countByReference(String fieldName, Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_archive
				WHERE """ + fieldName + " = :id", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(ProductArchiveEntity productArchiveEntity) {
		return new MapSqlParameterSource()
				.addValue("productCode", productArchiveEntity.productCode())
				.addValue("productName", productArchiveEntity.productName())
				.addValue("productSpecification", productArchiveEntity.productSpecification())
				.addValue("categoryId", productArchiveEntity.categoryId())
				.addValue("unitId", productArchiveEntity.unitId())
				.addValue("originId", productArchiveEntity.originId())
				.addValue("storageConditionId", productArchiveEntity.storageConditionId())
				.addValue("shelfLifeRuleId", productArchiveEntity.shelfLifeRuleId())
				.addValue("qualityGradeId", productArchiveEntity.qualityGradeId())
				.addValue("status", productArchiveEntity.status())
				.addValue("sortOrder", productArchiveEntity.sortOrder())
				.addValue("remarks", productArchiveEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
