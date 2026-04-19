package com.nongcang.server.modules.customer.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.customer.domain.entity.CustomerEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

	private static final RowMapper<CustomerEntity> CUSTOMER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public CustomerEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new CustomerEntity(
					rs.getLong("id"),
					rs.getString("customer_code"),
					rs.getString("customer_name"),
					rs.getString("customer_type"),
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

	public CustomerRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<CustomerEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM customer
				ORDER BY sort_order ASC, id ASC
				""", CUSTOMER_ROW_MAPPER);
	}

	public Optional<CustomerEntity> findById(Long id) {
		List<CustomerEntity> customers = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM customer
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), CUSTOMER_ROW_MAPPER);
		return customers.stream().findFirst();
	}

	public boolean existsByCustomerCode(String customerCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM customer
				WHERE customer_code = :customerCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("customerCode", customerCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByCustomerName(String customerName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM customer
				WHERE customer_name = :customerName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("customerName", customerName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(CustomerEntity customerEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO customer (
				  customer_code,
				  customer_name,
				  customer_type,
				  contact_name,
				  contact_phone,
				  region_name,
				  address,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :customerCode,
				  :customerName,
				  :customerType,
				  :contactName,
				  :contactPhone,
				  :regionName,
				  :address,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(customerEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(CustomerEntity customerEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE customer
				SET customer_name = :customerName,
				    customer_type = :customerType,
				    contact_name = :contactName,
				    contact_phone = :contactPhone,
				    region_name = :regionName,
				    address = :address,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(customerEntity).addValue("id", customerEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE customer
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM customer
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private MapSqlParameterSource buildParameters(CustomerEntity customerEntity) {
		return new MapSqlParameterSource()
				.addValue("customerCode", customerEntity.customerCode())
				.addValue("customerName", customerEntity.customerName())
				.addValue("customerType", customerEntity.customerType())
				.addValue("contactName", customerEntity.contactName())
				.addValue("contactPhone", customerEntity.contactPhone())
				.addValue("regionName", customerEntity.regionName())
				.addValue("address", customerEntity.address())
				.addValue("status", customerEntity.status())
				.addValue("sortOrder", customerEntity.sortOrder())
				.addValue("remarks", customerEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
