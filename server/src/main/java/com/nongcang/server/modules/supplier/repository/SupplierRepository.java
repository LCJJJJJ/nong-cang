package com.nongcang.server.modules.supplier.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.supplier.domain.entity.SupplierEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SupplierRepository {

	private static final RowMapper<SupplierEntity> SUPPLIER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public SupplierEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SupplierEntity(
					rs.getLong("id"),
					rs.getString("supplier_code"),
					rs.getString("supplier_name"),
					rs.getString("supplier_type"),
					rs.getString("contact_name"),
					rs.getString("contact_phone"),
					rs.getString("region_name"),
					rs.getString("address"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public SupplierRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<SupplierEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM supplier
				ORDER BY sort_order ASC, id ASC
				""", SUPPLIER_ROW_MAPPER);
	}

	public Optional<SupplierEntity> findById(Long id) {
		List<SupplierEntity> suppliers = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM supplier
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), SUPPLIER_ROW_MAPPER);
		return suppliers.stream().findFirst();
	}

	public boolean existsBySupplierCode(String supplierCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM supplier
				WHERE supplier_code = :supplierCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("supplierCode", supplierCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsBySupplierName(String supplierName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM supplier
				WHERE supplier_name = :supplierName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("supplierName", supplierName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(SupplierEntity supplierEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO supplier (
				  supplier_code,
				  supplier_name,
				  supplier_type,
				  contact_name,
				  contact_phone,
				  region_name,
				  address,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :supplierCode,
				  :supplierName,
				  :supplierType,
				  :contactName,
				  :contactPhone,
				  :regionName,
				  :address,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(supplierEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(SupplierEntity supplierEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE supplier
				SET supplier_name = :supplierName,
				    supplier_type = :supplierType,
				    contact_name = :contactName,
				    contact_phone = :contactPhone,
				    region_name = :regionName,
				    address = :address,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(supplierEntity).addValue("id", supplierEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE supplier
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM supplier
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private MapSqlParameterSource buildParameters(SupplierEntity supplierEntity) {
		return new MapSqlParameterSource()
				.addValue("supplierCode", supplierEntity.supplierCode())
				.addValue("supplierName", supplierEntity.supplierName())
				.addValue("supplierType", supplierEntity.supplierType())
				.addValue("contactName", supplierEntity.contactName())
				.addValue("contactPhone", supplierEntity.contactPhone())
				.addValue("regionName", supplierEntity.regionName())
				.addValue("address", supplierEntity.address())
				.addValue("status", supplierEntity.status())
				.addValue("sortOrder", supplierEntity.sortOrder())
				.addValue("remarks", supplierEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
