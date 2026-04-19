package com.nongcang.server.modules.messagenotice.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.messagenotice.domain.entity.MessageNoticeEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageNoticeRepository {

	private static final RowMapper<MessageNoticeEntity> MESSAGE_NOTICE_ROW_MAPPER = new RowMapper<>() {
		@Override
		public MessageNoticeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new MessageNoticeEntity(
					rs.getLong("id"),
					rs.getString("notice_code"),
					rs.getObject("alert_record_id", Long.class),
					rs.getString("notice_type"),
					rs.getString("severity"),
					rs.getString("title"),
					rs.getString("content"),
					rs.getString("source_type"),
					rs.getLong("source_id"),
					rs.getString("source_code"),
					rs.getInt("status"),
					toLocalDateTime(rs.getTimestamp("created_at")),
					toLocalDateTime(rs.getTimestamp("read_at")));
		}
	};

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public MessageNoticeRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<MessageNoticeEntity> findAll() {
		return namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  notice_code,
				  alert_record_id,
				  notice_type,
				  severity,
				  title,
				  content,
				  source_type,
				  source_id,
				  source_code,
				  status,
				  created_at,
				  read_at
				FROM message_notice
				ORDER BY created_at DESC, id DESC
				""", MESSAGE_NOTICE_ROW_MAPPER);
	}

	public Optional<MessageNoticeEntity> findById(Long id) {
		List<MessageNoticeEntity> notices = namedParameterJdbcTemplate.query("""
				SELECT
				  id,
				  notice_code,
				  alert_record_id,
				  notice_type,
				  severity,
				  title,
				  content,
				  source_type,
				  source_id,
				  source_code,
				  status,
				  created_at,
				  read_at
				FROM message_notice
				WHERE id = :id
				""", new MapSqlParameterSource("id", id), MESSAGE_NOTICE_ROW_MAPPER);
		return notices.stream().findFirst();
	}

	public boolean existsByNoticeCode(String noticeCode) {
		Integer count = namedParameterJdbcTemplate.queryForObject("""
				SELECT COUNT(1)
				FROM message_notice
				WHERE notice_code = :noticeCode
				""", new MapSqlParameterSource("noticeCode", noticeCode), Integer.class);
		return count != null && count > 0;
	}

	public void insert(
			String noticeCode,
			Long alertRecordId,
			String noticeType,
			String severity,
			String title,
			String content,
			String sourceType,
			Long sourceId,
			String sourceCode,
			Integer status) {
		namedParameterJdbcTemplate.update("""
				INSERT INTO message_notice (
				  notice_code,
				  alert_record_id,
				  notice_type,
				  severity,
				  title,
				  content,
				  source_type,
				  source_id,
				  source_code,
				  status
				)
				VALUES (
				  :noticeCode,
				  :alertRecordId,
				  :noticeType,
				  :severity,
				  :title,
				  :content,
				  :sourceType,
				  :sourceId,
				  :sourceCode,
				  :status
				)
				""", new MapSqlParameterSource()
				.addValue("noticeCode", noticeCode)
				.addValue("alertRecordId", alertRecordId)
				.addValue("noticeType", noticeType)
				.addValue("severity", severity)
				.addValue("title", title)
				.addValue("content", content)
				.addValue("sourceType", sourceType)
				.addValue("sourceId", sourceId)
				.addValue("sourceCode", sourceCode)
				.addValue("status", status));
	}

	public void markRead(Long id, LocalDateTime readAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE message_notice
				SET status = 2,
				    read_at = :readAt
				WHERE id = :id
				""", new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("readAt", readAt));
	}

	public void markAllRead(LocalDateTime readAt) {
		namedParameterJdbcTemplate.update("""
				UPDATE message_notice
				SET status = 2,
				    read_at = :readAt
				WHERE status = 1
				""", new MapSqlParameterSource("readAt", readAt));
	}

	private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}
}
