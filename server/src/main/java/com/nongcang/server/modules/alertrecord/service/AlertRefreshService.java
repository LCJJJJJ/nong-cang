package com.nongcang.server.modules.alertrecord.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.nongcang.server.modules.abnormalstock.repository.AbnormalStockRepository;
import com.nongcang.server.modules.alertrule.domain.entity.AlertRuleEntity;
import com.nongcang.server.modules.alertrule.repository.AlertRuleRepository;
import com.nongcang.server.modules.alertrecord.domain.entity.AlertRecordEntity;
import com.nongcang.server.modules.alertrecord.domain.vo.AlertRefreshResponse;
import com.nongcang.server.modules.alertrecord.repository.AlertRecordRepository;
import com.nongcang.server.modules.inboundrecord.repository.InboundRecordRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryBatchRepository;
import com.nongcang.server.modules.inventorystock.repository.InventoryStockQueryRepository;
import com.nongcang.server.modules.inventorystocktaking.repository.InventoryStocktakingRepository;
import com.nongcang.server.modules.messagenotice.repository.MessageNoticeRepository;
import com.nongcang.server.modules.outboundtask.repository.OutboundTaskRepository;
import com.nongcang.server.modules.putawaytask.repository.PutawayTaskRepository;
import com.nongcang.server.modules.qualityinspection.repository.QualityInspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertRefreshService {

	private static final int ALERT_ACTIVE = 1;
	private static final int ALERT_IGNORED = 2;
	private static final int ALERT_RESOLVED = 3;
	private static final int NOTICE_UNREAD = 1;
	private static final DateTimeFormatter ALERT_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final AlertRuleRepository alertRuleRepository;
	private final AlertRecordRepository alertRecordRepository;
	private final MessageNoticeRepository messageNoticeRepository;
	private final InventoryStockQueryRepository inventoryStockQueryRepository;
	private final InventoryBatchRepository inventoryBatchRepository;
	private final PutawayTaskRepository putawayTaskRepository;
	private final OutboundTaskRepository outboundTaskRepository;
	private final AbnormalStockRepository abnormalStockRepository;
	private final InventoryStocktakingRepository inventoryStocktakingRepository;
	private final InboundRecordRepository inboundRecordRepository;
	private final QualityInspectionRepository qualityInspectionRepository;

	public AlertRefreshService(
			AlertRuleRepository alertRuleRepository,
			AlertRecordRepository alertRecordRepository,
			MessageNoticeRepository messageNoticeRepository,
			InventoryStockQueryRepository inventoryStockQueryRepository,
			InventoryBatchRepository inventoryBatchRepository,
			PutawayTaskRepository putawayTaskRepository,
			OutboundTaskRepository outboundTaskRepository,
			AbnormalStockRepository abnormalStockRepository,
			InventoryStocktakingRepository inventoryStocktakingRepository,
			InboundRecordRepository inboundRecordRepository,
			QualityInspectionRepository qualityInspectionRepository) {
		this.alertRuleRepository = alertRuleRepository;
		this.alertRecordRepository = alertRecordRepository;
		this.messageNoticeRepository = messageNoticeRepository;
		this.inventoryStockQueryRepository = inventoryStockQueryRepository;
		this.inventoryBatchRepository = inventoryBatchRepository;
		this.putawayTaskRepository = putawayTaskRepository;
		this.outboundTaskRepository = outboundTaskRepository;
		this.abnormalStockRepository = abnormalStockRepository;
		this.inventoryStocktakingRepository = inventoryStocktakingRepository;
		this.inboundRecordRepository = inboundRecordRepository;
		this.qualityInspectionRepository = qualityInspectionRepository;
	}

	@Transactional
	public AlertRefreshResponse refreshAllAlerts() {
		List<AlertRuleEntity> enabledRules = alertRuleRepository.findEnabledRules();
		Map<String, AlertRecordEntity> existingMap = new HashMap<>();
		alertRecordRepository.findUnresolved().forEach(alert -> existingMap.put(buildKey(
				alert.ruleCode(),
				alert.sourceType(),
				alert.sourceId()), alert));

		int createdCount = 0;
		int resolvedCount = 0;
		Set<String> hitKeys = new HashSet<>();

		for (AlertRuleEntity rule : enabledRules) {
			for (AlertCandidate candidate : evaluateRule(rule)) {
				String key = buildKey(rule.ruleCode(), candidate.sourceType(), candidate.sourceId());
				hitKeys.add(key);
				AlertRecordEntity existing = existingMap.get(key);

				if (existing == null) {
					long alertRecordId = alertRecordRepository.insert(new AlertRecordEntity(
							null,
							generateAlertCode(),
							rule.id(),
							rule.ruleCode(),
							rule.alertType(),
							rule.severity(),
							candidate.sourceType(),
							candidate.sourceId(),
							candidate.sourceCode(),
							candidate.title(),
							candidate.content(),
							ALERT_ACTIVE,
							candidate.occurredAt(),
							null,
							null,
							null));
					messageNoticeRepository.insert(
							generateNoticeCode(),
							alertRecordId,
							"ALERT",
							rule.severity(),
							candidate.title(),
							candidate.content(),
							candidate.sourceType(),
							candidate.sourceId(),
							candidate.sourceCode(),
							NOTICE_UNREAD);
					createdCount += 1;
				}
			}
		}

		for (AlertRecordEntity existing : existingMap.values()) {
			String key = buildKey(existing.ruleCode(), existing.sourceType(), existing.sourceId());
			if (!hitKeys.contains(key)) {
				alertRecordRepository.resolve(existing.id(), LocalDateTime.now());
				resolvedCount += 1;
			}
		}

		long activeCount = alertRecordRepository.findAll().stream().filter(item -> item.status() == ALERT_ACTIVE).count();
		long ignoredCount = alertRecordRepository.findAll().stream().filter(item -> item.status() == ALERT_IGNORED).count();

		return new AlertRefreshResponse(createdCount, resolvedCount, (int) activeCount, (int) ignoredCount);
	}

	private List<AlertCandidate> evaluateRule(AlertRuleEntity rule) {
		return switch (rule.alertType()) {
			case "LOW_STOCK" -> inventoryStockQueryRepository.findAll().stream()
					.filter(stock -> stock.availableQuantity().compareTo(rule.thresholdValue()) <= 0)
					.map(stock -> new AlertCandidate(
							"INVENTORY_STOCK",
							stock.id(),
							"STOCK-" + stock.id(),
							stock.updatedAt(),
							stock.productName() + " 低库存预警",
							stock.productName() + " 在 " + stock.warehouseName() + "/" + stock.locationName()
									+ " 的可用库存为 " + stock.availableQuantity() + "，低于阈值 " + rule.thresholdValue()))
					.toList();
			case "PUTAWAY_TIMEOUT" -> putawayTaskRepository.findAll().stream()
					.filter(task -> task.status() == 1 || task.status() == 2)
					.filter(task -> exceededThreshold(task.createdAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(task -> new AlertCandidate(
							"PUTAWAY_TASK",
							task.id(),
							task.taskCode(),
							task.createdAt(),
							task.taskCode() + " 待上架超时",
							"上架任务 " + task.taskCode() + " 已超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未完成上架"))
					.toList();
			case "OUTBOUND_PICK_TIMEOUT" -> outboundTaskRepository.findAll().stream()
					.filter(task -> task.status() == 2)
					.filter(task -> exceededThreshold(task.updatedAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(task -> new AlertCandidate(
							"OUTBOUND_TASK",
							task.id(),
							task.taskCode(),
							task.updatedAt(),
							task.taskCode() + " 待拣货超时",
							"拣货任务 " + task.taskCode() + " 已超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未完成拣货"))
					.toList();
			case "OUTBOUND_SHIP_TIMEOUT" -> outboundTaskRepository.findAll().stream()
					.filter(task -> task.status() == 3)
					.filter(task -> exceededThreshold(task.pickedAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(task -> new AlertCandidate(
							"OUTBOUND_TASK",
							task.id(),
							task.taskCode(),
							task.pickedAt(),
							task.taskCode() + " 待出库超时",
							"拣货任务 " + task.taskCode() + " 已超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未完成出库"))
					.toList();
			case "ABNORMAL_STOCK_STAGNANT" -> abnormalStockRepository.findAll().stream()
					.filter(stock -> stock.status() == 1)
					.filter(stock -> exceededThreshold(stock.createdAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(stock -> new AlertCandidate(
							"ABNORMAL_STOCK",
							stock.id(),
							stock.abnormalCode(),
							stock.createdAt(),
							stock.abnormalCode() + " 异常库存滞留",
							"异常库存 " + stock.abnormalCode() + " 已锁定超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未处理"))
					.toList();
			case "STOCKTAKING_CONFIRM_TIMEOUT" -> inventoryStocktakingRepository.findAll().stream()
					.filter(order -> order.status() == 2)
					.filter(order -> exceededThreshold(order.updatedAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(order -> new AlertCandidate(
							"INVENTORY_STOCKTAKING_ORDER",
							order.id(),
							order.stocktakingCode(),
							order.updatedAt(),
							order.stocktakingCode() + " 待确认超时",
							"盘点单 " + order.stocktakingCode() + " 已超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未确认"))
					.toList();
			case "INBOUND_PENDING_INSPECTION" -> inboundRecordRepository.findAll().stream()
					.filter(record -> qualityInspectionRepository
							.sumInspectQuantityBySource("INBOUND_RECORD", record.id())
							.compareTo(record.quantity()) < 0)
					.filter(record -> exceededThreshold(record.occurredAt(), rule.thresholdValue(), rule.thresholdUnit()))
					.map(record -> new AlertCandidate(
							"INBOUND_RECORD",
							record.id(),
							record.recordCode(),
							record.occurredAt(),
							record.recordCode() + " 待质检超时",
							"入库记录 " + record.recordCode() + " 已超过 " + rule.thresholdValue() + " "
									+ thresholdUnitLabel(rule.thresholdUnit()) + " 未完成质检"))
					.toList();
			case "NEAR_EXPIRY" -> inventoryBatchRepository.findActiveForAlerting().stream()
					.filter(batch -> batch.warningAt() != null)
					.filter(batch -> batch.expectedExpireAt() != null)
					.filter(batch -> !batch.expectedExpireAt().isBefore(LocalDateTime.now()))
					.filter(batch -> !batch.warningAt().isAfter(LocalDateTime.now()))
					.map(batch -> new AlertCandidate(
							"INVENTORY_BATCH",
							batch.id(),
							batch.batchCode(),
							batch.warningAt(),
							batch.productName() + " 临期预警",
							batch.productName() + " 批次 " + batch.batchCode() + " 位于 "
									+ batch.warehouseName() + "/" + batch.locationName()
									+ "，剩余 " + batch.remainingQuantity() + "，将于 "
									+ batch.expectedExpireAt().atOffset(ZoneOffset.ofHours(8)) + " 到期"))
					.toList();
			case "EXPIRED" -> inventoryBatchRepository.findActiveForAlerting().stream()
					.filter(batch -> batch.expectedExpireAt() != null)
					.filter(batch -> !batch.expectedExpireAt().isAfter(LocalDateTime.now()))
					.map(batch -> new AlertCandidate(
							"INVENTORY_BATCH",
							batch.id(),
							batch.batchCode(),
							batch.expectedExpireAt(),
							batch.productName() + " 过期预警",
							batch.productName() + " 批次 " + batch.batchCode() + " 位于 "
									+ batch.warehouseName() + "/" + batch.locationName()
									+ "，剩余 " + batch.remainingQuantity() + "，已于 "
									+ batch.expectedExpireAt().atOffset(ZoneOffset.ofHours(8)) + " 过期"))
					.toList();
			default -> List.of();
		};
	}

	private boolean exceededThreshold(LocalDateTime baseTime, BigDecimal thresholdValue, String thresholdUnit) {
		if (baseTime == null) {
			return false;
		}
		long actualMillis = Duration.between(baseTime, LocalDateTime.now()).toMillis();
		BigDecimal thresholdMillis = switch (thresholdUnit) {
			case "MINUTE" -> thresholdValue.multiply(BigDecimal.valueOf(60_000L));
			case "HOUR" -> thresholdValue.multiply(BigDecimal.valueOf(3_600_000L));
			default -> BigDecimal.valueOf(Long.MAX_VALUE);
		};
		return BigDecimal.valueOf(actualMillis).compareTo(thresholdMillis) >= 0;
	}

	private String thresholdUnitLabel(String thresholdUnit) {
		return switch (thresholdUnit) {
			case "MINUTE" -> "分钟";
			case "HOUR" -> "小时";
			default -> thresholdUnit;
		};
	}

	private String buildKey(String ruleCode, String sourceType, Long sourceId) {
		return ruleCode + "|" + sourceType + "|" + sourceId;
	}

	private String generateAlertCode() {
		for (int index = 0; index < 20; index += 1) {
			String alertCode = "ALERT-" + LocalDateTime.now().format(ALERT_CODE_FORMATTER);
			if (index > 0) {
				alertCode += "-" + index;
			}
			if (!alertRecordRepository.existsByAlertCode(alertCode)) {
				return alertCode;
			}
		}
		throw new IllegalStateException("预警编号生成失败");
	}

	private String generateNoticeCode() {
		for (int index = 0; index < 20; index += 1) {
			String noticeCode = "MSG-" + LocalDateTime.now().format(ALERT_CODE_FORMATTER);
			if (index > 0) {
				noticeCode += "-" + index;
			}
			if (!messageNoticeRepository.existsByNoticeCode(noticeCode)) {
				return noticeCode;
			}
		}
		throw new IllegalStateException("消息编号生成失败");
	}

	private record AlertCandidate(
			String sourceType,
			Long sourceId,
			String sourceCode,
			LocalDateTime occurredAt,
			String title,
			String content) {
	}
}
