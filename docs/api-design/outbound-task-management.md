# 拣货出库任务管理接口清单

## 页面目标

拣货出库任务管理页用于承接已分配完成的出库单明细，完成库存分配、拣货确认与最终出库扣减，是库存减少的直接入口。

## 数据模型

主表：`outbound_task`

关键字段：

- `id`：拣货出库任务主键
- `task_code`：任务编号，全局唯一
- `outbound_order_id`：来源出库单ID
- `outbound_order_item_id`：来源出库单明细ID
- `customer_id`：客户ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `product_id`：产品ID
- `quantity`：待出库数量
- `status`：`1` 待分配，`2` 待拣货，`3` 待出库，`4` 已完成，`5` 已取消
- `picked_at`：拣货完成时间
- `completed_at`：出库完成时间

## 接口清单

### 1. 查询任务列表

`GET /api/outbound-task/list`

查询参数：

- `taskCode`：可选，任务编号模糊查询
- `warehouseId`：可选，仓库筛选
- `status`：可选，状态筛选

### 2. 查询任务详情

`GET /api/outbound-task/{id}`

### 3. 查询可分配库存

`GET /api/outbound-task/{id}/stock-options`

说明：

- 返回当前任务可用的库存库位及可分配数量

### 4. 分配库存

`PATCH /api/outbound-task/{id}/assign`

请求体：

```json
{
  "zoneId": 2,
  "locationId": 2
}
```

### 5. 确认拣货

`PATCH /api/outbound-task/{id}/pick`

### 6. 确认出库

`PATCH /api/outbound-task/{id}/complete`

说明：

- 确认出库时会真正扣减库存并生成库存流水

### 7. 取消任务

`PATCH /api/outbound-task/{id}/cancel`

## 常见业务错误

- `OUTBOUND_TASK_NOT_FOUND`：拣货出库任务不存在
- `OUTBOUND_TASK_STATUS_INVALID`：当前任务状态不允许执行该操作
- `OUTBOUND_TASK_STOCK_INSUFFICIENT`：所选库位可用库存不足
- `WAREHOUSE_LOCATION_ZONE_MISMATCH`：所选仓库与库区或库位不匹配

## 前端联调建议

- 页面初始化同时加载：
  - `/api/outbound-task/list`
  - `/api/warehouse/options`
- 打开分配库存弹窗时再加载：
  - `/api/outbound-task/{id}`
  - `/api/outbound-task/{id}/stock-options`
- 确认拣货成功后刷新列表
- 确认出库成功后刷新列表
