package com.nongcang.server.modules.qualitygrade.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.qualitygrade.domain.entity.QualityGradeEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class QualityGradeRepository {

	private static final RowMapper<QualityGradeEntity> QUALITY_GRADE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public QualityGradeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new QualityGradeEntity(
					rs.getLong("id"),
					rs.getString("grade_code"),
					rs.getString("grade_name"),
					rs.getBigDecimal("score_min"),
					rs.getBigDecimal("score_max"),
					rs.getInt("status"),
					rs.getInt("sort_order"),
					rs.getString("remarks"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("updated_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public QualityGradeRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<QualityGradeEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT *
				FROM quality_grade
				ORDER BY sort_order ASC, id ASC
				""", QUALITY_GRADE_ROW_MAPPER);
	}

	public Optional<QualityGradeEntity> findById(Long id) {
		List<QualityGradeEntity> qualityGrades = namedParameterJdbcTemplate.query("""
				SELECT *
				FROM quality_grade
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), QUALITY_GRADE_ROW_MAPPER);
		return qualityGrades.stream().findFirst();
	}

	public boolean existsByGradeCode(String gradeCode, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM quality_grade
				WHERE grade_code = :gradeCode
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("gradeCode", gradeCode);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public boolean existsByGradeName(String gradeName, Long excludeId) {
		String sql = """
				SELECT COUNT(1)
				FROM quality_grade
				WHERE grade_name = :gradeName
				""";
		MapSqlParameterSource parameters = new MapSqlParameterSource("gradeName", gradeName);

		if (excludeId != null) {
			sql += " AND id <> :excludeId";
			parameters.addValue("excludeId", excludeId);
		}

		Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
		return count != null && count > 0;
	}

	public long insert(QualityGradeEntity qualityGradeEntity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update("""
				INSERT INTO quality_grade (
				  grade_code,
				  grade_name,
				  score_min,
				  score_max,
				  status,
				  sort_order,
				  remarks
				)
				VALUES (
				  :gradeCode,
				  :gradeName,
				  :scoreMin,
				  :scoreMax,
				  :status,
				  :sortOrder,
				  :remarks
				)
				""", buildParameters(qualityGradeEntity), generatedKeyHolder);
		Number generatedId = generatedKeyHolder.getKey();
		return generatedId == null ? 0L : generatedId.longValue();
	}

	public void update(QualityGradeEntity qualityGradeEntity) {
		namedParameterJdbcTemplate.update("""
				UPDATE quality_grade
				SET grade_name = :gradeName,
				    score_min = :scoreMin,
				    score_max = :scoreMax,
				    status = :status,
				    sort_order = :sortOrder,
				    remarks = :remarks
				WHERE id = :id
				""", buildParameters(qualityGradeEntity).addValue("id", qualityGradeEntity.id()));
	}

	public void updateStatus(Long id, Integer status) {
		namedParameterJdbcTemplate.update("""
				UPDATE quality_grade
				SET status = :status
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("status", status));
	}

	public void deleteById(Long id) {
		namedParameterJdbcTemplate.update("""
				DELETE FROM quality_grade
				WHERE id = :id
				""", new MapSqlParameterSource("id", id));
	}

	public long countProductArchiveReferences(Long id) {
		Long count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM product_archive
				WHERE quality_grade_id = :id
				""", new MapSqlParameterSource("id", id), Long.class);
		return count == null ? 0L : count;
	}

	private MapSqlParameterSource buildParameters(QualityGradeEntity qualityGradeEntity) {
		return new MapSqlParameterSource()
				.addValue("gradeCode", qualityGradeEntity.gradeCode())
				.addValue("gradeName", qualityGradeEntity.gradeName())
				.addValue("scoreMin", qualityGradeEntity.scoreMin())
				.addValue("scoreMax", qualityGradeEntity.scoreMax())
				.addValue("status", qualityGradeEntity.status())
				.addValue("sortOrder", qualityGradeEntity.sortOrder())
				.addValue("remarks", qualityGradeEntity.remarks());
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
