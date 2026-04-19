package com.nongcang.server.modules.systemuser.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.systemuser.domain.entity.SystemUserEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SystemUserRepository {

	private static final RowMapper<SystemUserEntity> SYSTEM_USER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public SystemUserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SystemUserEntity(
					rs.getLong("id"),
					rs.getString("user_code"),
					rs.getString("username"),
					rs.getString("password_hash"),
					rs.getString("display_name"),
					rs.getString("phone"),
					rs.getString("role_code"),
					rs.getObject("warehouse_id", Long.class),
					rs.getString("warehouse_name"),
					rs.getInt("status"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public SystemUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<SystemUserEntity> findAll() {
		return namedParameterJdbcTemplate.query(baseSelect() + """
				ORDER BY u.updated_at DESC, u.id DESC
				""", SYSTEM_USER_ROW_MAPPER);
	}

	public Optional<SystemUserEntity> findById(Long id) {
		List<SystemUserEntity> users = namedParameterJdbcTemplate.query(baseSelect() + """
				WHERE u.id = :id
				""", new MapSqlParameterSource("id", id), SYSTEM_USER_ROW_MAPPER);
		return users.stream().findFirst();
	}

	public Optional<SystemUserEntity> findByAccount(String account) {
		List<SystemUserEntity> users = namedParameterJdbcTemplate.query(baseSelect() + """
				WHERE u.username = :account
				   OR u.phone = :account
				LIMIT 1
				""", new MapSqlParameterSource("account", account), SYSTEM_USER_ROW_MAPPER);
		return users.stream().findFirst();
	}

	public boolean existsByUsername(String username, Long excludeId) {
		return existsByField("username", username, excludeId);
	}

	public boolean existsByPhone(String phone, Long excludeId) {
		return existsByField("phone", phone, excludeId);
	}

	public long insert(SystemUserEntity entity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO sys_user (
				  user_code,
				  username,
				  password_hash,
				  display_name,
				  phone,
				  role_code,
				  warehouse_id,
				  status,
				  remarks
				)
				VALUES (
				  :userCode,
				  :username,
				  :passwordHash,
				  :displayName,
				  :phone,
				  :roleCode,
				  :warehouseId,
				  :status,
				  :remarks
				)
				""", buildParameters(entity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(SystemUserEntity entity) {
		namedParameterJdbcTemplate.update("""
				UPDATE sys_user
				SET display_name = :displayName,
				    phone = :phone,
				    role_code = :roleCode,
				    warehouse_id = :warehouseId,
				    status = :status,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(entity).addValue("id", entity.id()));
	}

	public void updatePassword(Long id, String passwordHash) {
		namedParameterJdbcTemplate.update("""
				UPDATE sys_user
				SET password_hash = :passwordHash
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("passwordHash", passwordHash));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE sys_user
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM sys_user
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private String baseSelect() {
		return """
				SELECT
				  u.*,
				  w.warehouse_name
				FROM sys_user u
				LEFT JOIN warehouse w ON w.id = u.warehouse_id
				""";
	}

	private boolean existsByField(String field, String value, Long excludeId) {
		String sql = "SELECT COUNT(1) FROM sys_user WHERE " + field + " = :value";
		MapSqlParameterSource parameters = new MapSqlParameterSource("value", value);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	private MapSqlParameterSource buildParameters(SystemUserEntity entity) {
		return new MapSqlParameterSource()
				.addValue("userCode", entity.userCode())
				.addValue("username", entity.username())
				.addValue("passwordHash", entity.passwordHash())
				.addValue("displayName", entity.displayName())
				.addValue("phone", entity.phone())
				.addValue("roleCode", entity.roleCode())
				.addValue("warehouseId", entity.warehouseId())
				.addValue("status", entity.status())
				.addValue("remarks", entity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
