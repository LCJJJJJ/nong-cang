# 库存调整管理接口清单

## 页面目标

库存调整管理页用于处理系统初始化修正、人工纠错等非业务单据导致的库存变更，并沉淀标准调整记录和库存流水。

## 数据模型

主表：`inventory_adjustment`

关键字段：

- `adjustment_code`：调整单编号
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `product_id`：产品ID
- `adjustment_type`：调整方向，`INCREASE` / `DECREASE`
- `quantity`：调整数量
- `reason`：调整原因
- `remarks`：备注

## 接口清单

### 1. 查询库存调整列表

`GET /api/inventory-adjustment/list`

查询参数：

- `adjustmentCode`：可选，调整单编号模糊查询
- `warehouseId`：可选，仓库筛选
- `productId`：可选，产品筛选
- `adjustmentType`：可选，调整方向筛选

### 2. 新增库存调整

`POST /api/inventory-adjustment`

说明：

- 创建成功后立即生效
- 同时更新库存快照并生成 `ADJUSTMENT` 类型库存流水

## 常见业务错误

- `INVENTORY_ADJUSTMENT_TYPE_INVALID`：调整方向不正确
- `INVENTORY_ADJUSTMENT_QUANTITY_INVALID`：调整数量必须大于 `0`
- `INVENTORY_ADJUSTMENT_STOCK_INSUFFICIENT`：减少库存时可用数量不足

## 前端联调建议

- 页面初始化同时加载：
  - `/api/inventory-adjustment/list`
  - `/api/product-archive/options`
  - `/api/warehouse/options`
  - `/api/warehouse-zone/options`
  - `/api/warehouse-location/options`
- 表单中仓库、库区、库位保持级联选择
