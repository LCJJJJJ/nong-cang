# 库存盘点管理接口清单

## 页面目标

库存盘点管理页用于创建盘点单、录入实盘数量、确认盘点差异并回写库存，是库存治理闭环的最终环节。

## 数据模型

主表：`inventory_stocktaking_order`

关键字段：

- `stocktaking_code`：盘点单编号
- `warehouse_id`：仓库ID
- `zone_id`：库区ID，可为空
- `status`：`1` 待盘点，`2` 待确认，`3` 已完成，`4` 已取消
- `remarks`：备注

明细表：`inventory_stocktaking_item`

关键字段：

- `product_id`：产品ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `system_quantity`：系统数量
- `counted_quantity`：实盘数量
- `difference_quantity`：差异数量
- `remarks`：备注

## 接口清单

### 1. 查询盘点单列表

`GET /api/inventory-stocktaking/list`

查询参数：

- `stocktakingCode`：可选，盘点单编号模糊查询
- `warehouseId`：可选，仓库筛选
- `status`：可选，状态筛选

### 2. 查询盘点单详情

`GET /api/inventory-stocktaking/{id}`

### 3. 新增盘点单

`POST /api/inventory-stocktaking`

### 4. 保存盘点结果

`PUT /api/inventory-stocktaking/{id}/items`

### 5. 确认盘点

`PATCH /api/inventory-stocktaking/{id}/confirm`

说明：

- 确认后根据差异回写库存
- 同时生成 `STOCKTAKING` 类型库存流水

### 6. 取消盘点单

`PATCH /api/inventory-stocktaking/{id}/cancel`

## 常见业务错误

- `INVENTORY_STOCKTAKING_NOT_FOUND`：盘点单不存在
- `INVENTORY_STOCKTAKING_STATUS_INVALID`：当前盘点单状态不允许执行该操作
- `INVENTORY_STOCKTAKING_SCOPE_EMPTY`：所选范围内没有可盘点库存
- `INVENTORY_STOCKTAKING_COUNT_REQUIRED`：存在未录入实盘数量的明细
- `INVENTORY_STOCKTAKING_COUNT_INVALID`：实盘数量不能小于 `0`
- `INVENTORY_STOCKTAKING_RESERVED_CONFLICT`：实盘数量不能小于已预留数量

## 前端联调建议

- 页面初始化同时加载：
  - `/api/inventory-stocktaking/list`
  - `/api/warehouse/options`
  - `/api/warehouse-zone/options`
- 录入盘点结果时按详情接口加载明细
- 保存盘点结果后允许继续修改，确认盘点才最终生效
