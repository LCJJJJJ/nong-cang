package com.nongcang.server.modules.messagenotice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.messagenotice.domain.dto.MessageNoticeListQueryRequest;
import com.nongcang.server.modules.messagenotice.domain.entity.MessageNoticeEntity;
import com.nongcang.server.modules.messagenotice.domain.vo.MessageNoticeListItemResponse;
import com.nongcang.server.modules.messagenotice.repository.MessageNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MessageNoticeService {

	private final MessageNoticeRepository messageNoticeRepository;

	public MessageNoticeService(MessageNoticeRepository messageNoticeRepository) {
		this.messageNoticeRepository = messageNoticeRepository;
	}

	public List<MessageNoticeListItemResponse> getMessageNoticeList(MessageNoticeListQueryRequest queryRequest) {
		return messageNoticeRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional
	public void markRead(Long id) {
		getExistingNotice(id);
		messageNoticeRepository.markRead(id, LocalDateTime.now());
	}

	@Transactional
	public void markAllRead() {
		messageNoticeRepository.markAllRead(LocalDateTime.now());
	}

	private MessageNoticeEntity getExistingNotice(Long id) {
		return messageNoticeRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.MESSAGE_NOTICE_NOT_FOUND));
	}

	private boolean matchesQuery(MessageNoticeEntity entity, MessageNoticeListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.noticeCode())
				&& !entity.noticeCode().contains(queryRequest.noticeCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.severity())
				&& !entity.severity().equals(queryRequest.severity().trim().toUpperCase())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private MessageNoticeListItemResponse toListItemResponse(MessageNoticeEntity entity) {
		return new MessageNoticeListItemResponse(
				entity.id(),
				entity.noticeCode(),
				entity.alertRecordId(),
				entity.noticeType(),
				entity.severity(),
				entity.title(),
				entity.content(),
				entity.sourceType(),
				entity.sourceId(),
				entity.sourceCode(),
				entity.status(),
				entity.status() == 1 ? "未读" : "已读",
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.readAt()));
	}

	private String toIsoDateTime(LocalDateTime value) {
		return value == null ? null : value.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
