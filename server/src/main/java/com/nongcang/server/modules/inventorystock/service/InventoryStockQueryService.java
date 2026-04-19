package com.nongcang.server.modules.inventorystock.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.modules.inventorystock.domain.dto.InventoryStockListQueryRequest;
import com.nongcang.server.modules.inventorystock.domain.entity.InventoryStockEntity;
import com.nongcang.server.modules.inventorystock.domain.vo.InventoryStockListItemResponse;
import com.nongcang.server.modules.inventorystock.repository.InventoryStockQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryStockQueryService {

	private final InventoryStockQueryRepository inventoryStockQueryRepository;

	public InventoryStockQueryService(InventoryStockQueryRepository inventoryStockQueryRepository) {
		this.inventoryStockQueryRepository = inventoryStockQueryRepository;
	}

	public List<InventoryStockListItemResponse> getInventoryStockList(InventoryStockListQueryRequest queryRequest) {
		return inventoryStockQueryRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	private boolean matchesQuery(InventoryStockEntity entity, InventoryStockListQueryRequest queryRequest) {
		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.zoneId() != null && !Objects.equals(entity.zoneId(), queryRequest.zoneId())) {
			return false;
		}

		return true;
	}

	private InventoryStockListItemResponse toListItemResponse(InventoryStockEntity entity) {
		return new InventoryStockListItemResponse(
				entity.id(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.productSpecification(),
				entity.unitId(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				InventoryStockQueryRepository.toDouble(entity.stockQuantity()),
				InventoryStockQueryRepository.toDouble(entity.reservedQuantity()),
				InventoryStockQueryRepository.toDouble(entity.lockedQuantity()),
				InventoryStockQueryRepository.toDouble(entity.availableQuantity()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
