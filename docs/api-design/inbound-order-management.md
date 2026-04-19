# 入库单管理接口清单

## 页面目标

入库单管理页用于维护到货前的入库业务单据，关联供应商、仓库和产品明细，为后续上架任务生成提供业务入口。

## 数据模型

主表：`inbound_order`

关键字段：

- `id`：入库单主键
- `order_code`：入库单编号，全局唯一
- `supplier_id`：供应商ID
- `warehouse_id`：仓库ID
- `expected_arrival_at`：预计到货时间
- `actual_arrival_at`：实际到货时间
- `total_item_count`：商品条数
- `total_quantity`：总数量
- `status`：`1` 待到货，`2` 待上架，`3` 已完成，`4` 已取消
- `remarks`：备注

明细表：`inbound_order_item`

关键字段：

- `inbound_order_id`：所属入库单ID
- `product_id`：产品档案ID
- `quantity`：入库数量
- `sort_order`：排序值
- `remarks`：备注

## 接口清单

### 1. 查询入库单列表

`GET /api/inbound-order/list`

查询参数：

- `orderCode`：可选，入库单编号模糊查询
- `supplierId`：可选，供应商筛选
- `warehouseId`：可选，仓库筛选
- `status`：可选，状态筛选

### 2. 查询入库单详情

`GET /api/inbound-order/{id}`

### 3. 新增入库单

`POST /api/inbound-order`

### 4. 编辑入库单

`PUT /api/inbound-order/{id}`

说明：

- 仅 `待到货` 状态允许编辑

### 5. 到货确认

`PATCH /api/inbound-order/{id}/arrive`

说明：

- 到货确认后状态流转为 `待上架`
- 后续上架任务模块会基于该状态的入库单生成上架任务

### 6. 取消入库单

`PATCH /api/inbound-order/{id}/cancel`

说明：

- 仅 `待到货` 状态允许取消

## 常见业务错误

- `INBOUND_ORDER_NOT_FOUND`：入库单不存在
- `INBOUND_ORDER_ITEMS_EMPTY`：入库单明细不能为空
- `INBOUND_ORDER_STATUS_INVALID`：入库单当前状态不允许执行该操作
- `INBOUND_ORDER_ITEM_QUANTITY_INVALID`：入库数量必须大于 `0`

## 前端联调建议

- 页面初始化同时加载：
  - `/api/inbound-order/list`
  - `/api/supplier/options`
  - `/api/warehouse/options`
  - `/api/product-archive/options`
- 入库单表单使用动态明细行
- 到货确认成功后刷新列表，等待后续上架任务页面消费
