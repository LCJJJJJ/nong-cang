# 出库单管理接口清单

## 页面目标

出库单管理页用于维护待出库业务单据，关联客户、仓库和产品明细，为后续拣货出库任务生成提供业务入口。

## 数据模型

主表：`outbound_order`

关键字段：

- `id`：出库单主键
- `order_code`：出库单编号，全局唯一
- `customer_id`：客户ID
- `warehouse_id`：仓库ID
- `expected_delivery_at`：预计发货时间
- `actual_outbound_at`：实际出库时间
- `total_item_count`：商品条数
- `total_quantity`：总数量
- `status`：`1` 待分配，`2` 待拣货，`3` 待出库，`4` 已完成，`5` 已取消
- `remarks`：备注

明细表：`outbound_order_item`

关键字段：

- `outbound_order_id`：所属出库单ID
- `product_id`：产品档案ID
- `quantity`：出库数量
- `sort_order`：排序值
- `remarks`：备注

## 接口清单

### 1. 查询出库单列表

`GET /api/outbound-order/list`

查询参数：

- `orderCode`：可选，出库单编号模糊查询
- `customerId`：可选，客户筛选
- `warehouseId`：可选，仓库筛选
- `status`：可选，状态筛选

### 2. 查询出库单详情

`GET /api/outbound-order/{id}`

### 3. 新增出库单

`POST /api/outbound-order`

### 4. 编辑出库单

`PUT /api/outbound-order/{id}`

说明：

- 仅 `待分配` 状态允许编辑

### 5. 取消出库单

`PATCH /api/outbound-order/{id}/cancel`

说明：

- 仅 `待分配` 状态允许取消

## 常见业务错误

- `OUTBOUND_ORDER_NOT_FOUND`：出库单不存在
- `OUTBOUND_ORDER_ITEMS_EMPTY`：出库单明细不能为空
- `OUTBOUND_ORDER_STATUS_INVALID`：出库单当前状态不允许执行该操作
- `OUTBOUND_ORDER_ITEM_QUANTITY_INVALID`：出库数量必须大于 `0`

## 前端联调建议

- 页面初始化同时加载：
  - `/api/outbound-order/list`
  - `/api/customer/options`
  - `/api/warehouse/options`
  - `/api/product-archive/options`
- 出库单表单使用动态明细行
- 第二阶段拣货任务接入前，当前页面先负责待分配单据的维护与取消
