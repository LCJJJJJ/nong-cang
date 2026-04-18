package com.nongcang.server.modules.productunit.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.productunit.domain.entity.ProductUnitEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductUnitRepository {

	private static final RowMapper<ProductUnitEntity> PRODUCT_UNIT_ROW_MAPPER = new RowMapper<>() {
		@Override
		public ProductUnitEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ProductUnitEntity(
					rs.getLong("id"),
					rs.getString("unit_code"),
					rs.getString("unit_name"),
					rs.getString("unit_symbol"),
					rs.getString("unit_type"),
					rs.getInt("precision_digits"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ProductUnitRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<ProductUnitEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM product_unit
				ORDER BY sort_order ASC, id ASC
				""", PRODUCT_UNIT_ROW_MAPPER);
	}

	public Optional<ProductUnitEntity> findById(Long id) {
		List<ProductUnitEntity> productUnits = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM product_unit
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), PRODUCT_UNIT_ROW_MAPPER);
		return productUnits.stream().findFirst();
	}

	public boolean existsByUnitCode(String unitCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_unit
				WHERE unit_code = :unitCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("unitCode", unitCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByUnitName(String unitName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_unit
				WHERE unit_name = :unitName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("unitName", unitName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(ProductUnitEntity productUnitEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO product_unit (
				  unit_code,
				  unit_name,
				  unit_symbol,
				  unit_type,
				  precision_digits,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :unitCode,
				  :unitName,
				  :unitSymbol,
				  :unitType,
				  :precisionDigits,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(productUnitEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(ProductUnitEntity productUnitEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_unit
				SET unit_name = :unitName,
				    unit_symbol = :unitSymbol,
				    unit_type = :unitType,
				    precision_digits = :precisionDigits,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(productUnitEntity).addValue("id", productUnitEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_unit
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM product_unit
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private MapSqlParameterSource buildParameters(ProductUnitEntity productUnitEntity) {
		return new MapSqlParameterSource()
				.addValue("unitCode", productUnitEntity.unitCode())
				.addValue("unitName", productUnitEntity.unitName())
				.addValue("unitSymbol", productUnitEntity.unitSymbol())
				.addValue("unitType", productUnitEntity.unitType())
				.addValue("precisionDigits", productUnitEntity.precisionDigits())
				.addValue("status", productUnitEntity.status())
				.addValue("sortOrder", productUnitEntity.sortOrder())
				.addValue("remarks", productUnitEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
