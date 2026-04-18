package com.nongcang.server.modules.productorigin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.productorigin.domain.entity.ProductOriginEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductOriginRepository {

	private static final RowMapper<ProductOriginEntity> PRODUCT_ORIGIN_ROW_MAPPER = new RowMapper<>() {
		@Override
		public ProductOriginEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ProductOriginEntity(
					rs.getLong("id"),
					rs.getString("origin_code"),
					rs.getString("origin_name"),
					rs.getString("country_name"),
					rs.getString("province_name"),
					rs.getString("city_name"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ProductOriginRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<ProductOriginEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM product_origin
				ORDER BY sort_order ASC, id ASC
				""", PRODUCT_ORIGIN_ROW_MAPPER);
	}

	public Optional<ProductOriginEntity> findById(Long id) {
		List<ProductOriginEntity> productOrigins = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM product_origin
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), PRODUCT_ORIGIN_ROW_MAPPER);
		return productOrigins.stream().findFirst();
	}

	public boolean existsByOriginCode(String originCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_origin
				WHERE origin_code = :originCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("originCode", originCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByOriginName(String originName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_origin
				WHERE origin_name = :originName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("originName", originName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(ProductOriginEntity productOriginEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO product_origin (
				  origin_code,
				  origin_name,
				  country_name,
				  province_name,
				  city_name,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :originCode,
				  :originName,
				  :countryName,
				  :provinceName,
				  :cityName,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(productOriginEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(ProductOriginEntity productOriginEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_origin
				SET origin_name = :originName,
				    country_name = :countryName,
				    province_name = :provinceName,
				    city_name = :cityName,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(productOriginEntity).addValue("id", productOriginEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_origin
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM product_origin
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countProductArchiveReferences(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_archive
				WHERE origin_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(ProductOriginEntity productOriginEntity) {
		return new MapSqlParameterSource()
				.addValue("originCode", productOriginEntity.originCode())
				.addValue("originName", productOriginEntity.originName())
				.addValue("countryName", productOriginEntity.countryName())
				.addValue("provinceName", productOriginEntity.provinceName())
				.addValue("cityName", productOriginEntity.cityName())
				.addValue("status", productOriginEntity.status())
				.addValue("sortOrder", productOriginEntity.sortOrder())
				.addValue("remarks", productOriginEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
