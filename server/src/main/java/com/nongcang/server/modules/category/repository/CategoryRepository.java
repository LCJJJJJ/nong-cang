package com.nongcang.server.modules.category.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.category.domain.entity.CategoryEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

	private static final String CATEGORY_SELECT_COLUMNS = """
			SELECT
			  pc.id,
			  pc.category_code,
			  pc.category_name,
			  pc.parent_id,
			  pc.category_level,
			  pc.ancestor_path,
			  pc.sort_order,
			  pc.status,
			  pc.default_storage_condition_id,
			  sc.storage_type AS default_storage_type,
			  sc.condition_name AS default_storage_condition,
			  pc.shelf_life_days,
			  pc.warning_days,
			  pc.remarks,
			  pc.created_at,
			  pc.updated_at
			FROM product_category pc
			LEFT JOIN storage_condition sc
			  ON sc.id = pc.default_storage_condition_id
			""";

	private static final RowMapper<CategoryEntity> CATEGORY_ROW_MAPPER = new RowMapper<>() {
		@Override
		public CategoryEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new CategoryEntity(
					rs.getLong("id"),
					rs.getString("category_code"),
					rs.getString("category_name"),
					rs.getObject("parent_id", Long.class),
					rs.getInt("category_level"),
					rs.getString("ancestor_path"),
					rs.getInt("sort_order"),
					rs.getInt("status"),
					rs.getObject("default_storage_condition_id", Long.class),
					rs.getString("default_storage_type"),
					rs.getString("default_storage_condition"),
					rs.getObject("shelf_life_days", Integer.class),
					rs.getObject("warning_days", Integer.class),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public CategoryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<CategoryEntity> findAll() {
		return namedParameterJdbcTemplate.query(CATEGORY_SELECT_COLUMNS + """
				ORDER BY pc.category_level ASC, pc.sort_order ASC, pc.id ASC
				""", CATEGORY_ROW_MAPPER);
	}

	public Optional<CategoryEntity> findById(Long id) {
		List<CategoryEntity> categoryEntities = namedParameterJdbcTemplate.query(CATEGORY_SELECT_COLUMNS + """
				WHERE pc.id = :id
				""", new MapSqlParameterSource("id", id), CATEGORY_ROW_MAPPER);

		return categoryEntities.stream().findFirst();
	}

	public Optional<CategoryEntity> findByCode(String categoryCode) {
		List<CategoryEntity> categoryEntities = namedParameterJdbcTemplate.query(CATEGORY_SELECT_COLUMNS + """
				WHERE pc.category_code = :categoryCode
				""", new MapSqlParameterSource("categoryCode", categoryCode), CATEGORY_ROW_MAPPER);

		return categoryEntities.stream().findFirst();
	}

	public boolean existsByCategoryCode(String categoryCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_category
				WHERE category_code = :categoryCode
				""";

		MapSqlParameterSource parameterSource = new MapSqlParameterSource("categoryCode", categoryCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameterSource.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameterSource, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsSiblingName(Long parentId, String categoryName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM product_category
				WHERE category_name = :categoryName
				  AND ((parent_id IS NULL AND :parentId IS NULL) OR parent_id = :parentId)
				""";

		MapSqlParameterSource parameterSource = new MapSqlParameterSource()
				.addValue("categoryName", categoryName)
				.addValue("parentId", parentId);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameterSource.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameterSource, Integer.class);
		return count != null && count > 0;
	}

	public long insert(CategoryEntity categoryEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO product_category (
				  category_code,
				  category_name,
				  parent_id,
				  category_level,
				  ancestor_path,
				  sort_order,
				  status,
				  default_storage_condition_id,
				  shelf_life_days,
				  warning_days,
				  remarks
				)
				VALUES (
				  :categoryCode,
				  :categoryName,
				  :parentId,
				  :categoryLevel,
				  :ancestorPath,
				  :sortOrder,
				  :status,
				  :defaultStorageConditionId,
				  :shelfLifeDays,
				  :warningDays,
				  :remarks
				)
				""", buildParameterSource(categoryEntity), generatedKeyHolder);

		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(CategoryEntity categoryEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_category
				SET category_code = :categoryCode,
				    category_name = :categoryName,
				    parent_id = :parentId,
				    category_level = :categoryLevel,
				    ancestor_path = :ancestorPath,
				    sort_order = :sortOrder,
				    status = :status,
				    default_storage_condition_id = :defaultStorageConditionId,
				    shelf_life_days = :shelfLifeDays,
				    warning_days = :warningDays,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameterSource(categoryEntity).addValue("id", categoryEntity.id()));
	}

	public void updateSubtreePathAndLevel(String oldPrefix, String newPrefix, int levelDelta, Long categoryId) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_category
				SET ancestor_path = CONCAT(:newPrefix, SUBSTRING(ancestor_path, :oldPrefixLength + 1)),
				    category_level = category_level + :levelDelta
				WHERE id <> :categoryId
				  AND ancestor_path LIKE CONCAT(:oldPrefix, '%')
				""", new MapSqlParameterSource()
				.addValue("newPrefix", newPrefix)
				.addValue("oldPrefix", oldPrefix)
				.addValue("oldPrefixLength", oldPrefix.length())
				.addValue("levelDelta", levelDelta)
				.addValue("categoryId", categoryId));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE product_category
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public long countChildren(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_category
				WHERE parent_id = :id
		""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	public long countProductArchiveReferences(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_archive
				WHERE category_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM product_category
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	private MapSqlParameterSource buildParameterSource(CategoryEntity categoryEntity) {
		return new MapSqlParameterSource()
				.addValue("categoryCode", categoryEntity.categoryCode())
				.addValue("categoryName", categoryEntity.categoryName())
				.addValue("parentId", categoryEntity.parentId())
				.addValue("categoryLevel", categoryEntity.categoryLevel())
				.addValue("ancestorPath", categoryEntity.ancestorPath())
				.addValue("sortOrder", categoryEntity.sortOrder())
				.addValue("status", categoryEntity.status())
				.addValue("defaultStorageConditionId", categoryEntity.defaultStorageConditionId())
				.addValue("shelfLifeDays", categoryEntity.shelfLifeDays())
				.addValue("warningDays", categoryEntity.warningDays())
				.addValue("remarks", categoryEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
