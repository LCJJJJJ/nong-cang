package com.nongcang.server.modules.inventorytransaction.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.modules.inventorytransaction.domain.dto.InventoryTransactionListQueryRequest;
import com.nongcang.server.modules.inventorytransaction.domain.entity.InventoryTransactionEntity;
import com.nongcang.server.modules.inventorytransaction.domain.vo.InventoryTransactionListItemResponse;
import com.nongcang.server.modules.inventorytransaction.repository.InventoryTransactionQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InventoryTransactionQueryService {

	private final InventoryTransactionQueryRepository inventoryTransactionQueryRepository;

	public InventoryTransactionQueryService(InventoryTransactionQueryRepository inventoryTransactionQueryRepository) {
		this.inventoryTransactionQueryRepository = inventoryTransactionQueryRepository;
	}

	public List<InventoryTransactionListItemResponse> getInventoryTransactionList(
			InventoryTransactionListQueryRequest queryRequest) {
		return inventoryTransactionQueryRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	private boolean matchesQuery(
			InventoryTransactionEntity entity,
			InventoryTransactionListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.transactionCode())
				&& !entity.transactionCode().contains(queryRequest.transactionCode().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.transactionType())
				&& !entity.transactionType().equals(queryRequest.transactionType().trim())) {
			return false;
		}

		return true;
	}

	private InventoryTransactionListItemResponse toListItemResponse(InventoryTransactionEntity entity) {
		return new InventoryTransactionListItemResponse(
				entity.id(),
				entity.transactionCode(),
				entity.transactionType(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				InventoryTransactionQueryRepository.toDouble(entity.quantity()),
				entity.sourceType(),
				entity.sourceId(),
				toIsoDateTime(entity.occurredAt()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
