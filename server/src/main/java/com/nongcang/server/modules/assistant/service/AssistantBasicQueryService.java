package com.nongcang.server.modules.assistant.service;

import java.util.ArrayList;
import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.category.domain.dto.CategoryTreeQueryRequest;
import com.nongcang.server.modules.category.domain.vo.CategoryTreeItemResponse;
import com.nongcang.server.modules.category.service.CategoryService;
import com.nongcang.server.modules.customer.domain.dto.CustomerListQueryRequest;
import com.nongcang.server.modules.customer.service.CustomerService;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveListQueryRequest;
import com.nongcang.server.modules.productarchive.service.ProductArchiveService;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginListQueryRequest;
import com.nongcang.server.modules.productorigin.service.ProductOriginService;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitListQueryRequest;
import com.nongcang.server.modules.productunit.service.ProductUnitService;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeListQueryRequest;
import com.nongcang.server.modules.qualitygrade.service.QualityGradeService;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionListQueryRequest;
import com.nongcang.server.modules.storagecondition.service.StorageConditionService;
import com.nongcang.server.modules.supplier.domain.dto.SupplierListQueryRequest;
import com.nongcang.server.modules.supplier.service.SupplierService;
import org.springframework.stereotype.Service;

@Service
public class AssistantBasicQueryService {

	private final CategoryService categoryService;
	private final ProductArchiveService productArchiveService;
	private final ProductUnitService productUnitService;
	private final ProductOriginService productOriginService;
	private final StorageConditionService storageConditionService;
	private final QualityGradeService qualityGradeService;
	private final SupplierService supplierService;
	private final CustomerService customerService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantBasicQueryService(
			CategoryService categoryService,
			ProductArchiveService productArchiveService,
			ProductUnitService productUnitService,
			ProductOriginService productOriginService,
			StorageConditionService storageConditionService,
			QualityGradeService qualityGradeService,
			SupplierService supplierService,
			CustomerService customerService,
			AssistantQuerySupport assistantQuerySupport) {
		this.categoryService = categoryService;
		this.productArchiveService = productArchiveService;
		this.productUnitService = productUnitService;
		this.productOriginService = productOriginService;
		this.storageConditionService = storageConditionService;
		this.qualityGradeService = qualityGradeService;
		this.supplierService = supplierService;
		this.customerService = customerService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);

		return switch (normalizeEntityType(arguments.entityType())) {
			case "category" -> createResult(
					"query_basic_master_data",
					"产品分类查询",
					"/",
					"产品分类管理",
					assistantQuerySupport.filterItems(
							flattenCategoryTree(categoryService.getCategoryTree(
									new CategoryTreeQueryRequest(null, null, null, null, null))),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("categoryCode", "categoryName", "categoryLevel", "defaultStorageCondition", "statusLabel",
							"updatedAt"),
					limit);
			case "product" -> createResult(
					"query_basic_master_data",
					"产品档案查询",
					"/product-archives",
					"产品档案管理",
					assistantQuerySupport.filterItems(
							productArchiveService.getProductArchiveList(new ProductArchiveListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("productCode", "productName", "productSpecification", "categoryName", "unitName",
							"originName", "storageConditionName", "qualityGradeName", "statusLabel"),
					limit);
			case "unit" -> createResult(
					"query_basic_master_data",
					"产品单位查询",
					"/product-units",
					"产品单位管理",
					assistantQuerySupport.filterItems(
							productUnitService.getProductUnitList(new ProductUnitListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("unitCode", "unitName", "unitSymbol", "unitType", "statusLabel", "updatedAt"),
					limit);
			case "origin" -> createResult(
					"query_basic_master_data",
					"产地信息查询",
					"/product-origins",
					"产地信息管理",
					assistantQuerySupport.filterItems(
							productOriginService.getProductOriginList(new ProductOriginListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("originCode", "originName", "provinceName", "statusLabel", "updatedAt"),
					limit);
			case "storage_condition" -> createResult(
					"query_basic_master_data",
					"储存条件查询",
					"/storage-conditions",
					"储存条件管理",
					assistantQuerySupport.filterItems(
							storageConditionService.getStorageConditionList(
									new StorageConditionListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("conditionCode", "conditionName", "storageType", "lightRequirement", "ventilationRequirement",
							"statusLabel"),
					limit);
			case "quality_grade" -> createResult(
					"query_basic_master_data",
					"品质等级查询",
					"/quality-grades",
					"品质等级管理",
					assistantQuerySupport.filterItems(
							qualityGradeService.getQualityGradeList(new QualityGradeListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("gradeCode", "gradeName", "statusLabel", "updatedAt"),
					limit);
			case "supplier" -> createResult(
					"query_basic_master_data",
					"供应商查询",
					"/suppliers",
					"供应商管理",
					assistantQuerySupport.filterItems(
							supplierService.getSupplierList(new SupplierListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("supplierCode", "supplierName", "contactName", "contactPhone", "statusLabel", "updatedAt"),
					limit);
			case "customer" -> createResult(
					"query_basic_master_data",
					"客户查询",
					"/customers",
					"客户管理",
					assistantQuerySupport.filterItems(
							customerService.getCustomerList(new CustomerListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("customerCode", "customerName", "contactName", "contactPhone", "statusLabel", "updatedAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "基础主数据查询实体不正确");
		};
	}

	private AssistantToolExecutionResult createResult(
			String toolName,
			String title,
			String routePath,
			String routeLabel,
			List<?> items,
			List<String> preferredKeys,
			int limit) {
		AssistantResultBlock block = assistantQuerySupport.toTableBlock(
				title,
				routePath,
				routeLabel,
				items,
				preferredKeys,
				limit,
				title + "：");
		return new AssistantToolExecutionResult(toolName, block.summary(), List.of(block));
	}

	private List<CategoryTreeItemResponse> flattenCategoryTree(List<CategoryTreeItemResponse> items) {
		List<CategoryTreeItemResponse> flattenedItems = new ArrayList<>();
		for (CategoryTreeItemResponse item : items) {
			flattenedItems.add(item);
			if (item.children() != null && !item.children().isEmpty()) {
				flattenedItems.addAll(flattenCategoryTree(item.children()));
			}
		}
		return flattenedItems;
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
