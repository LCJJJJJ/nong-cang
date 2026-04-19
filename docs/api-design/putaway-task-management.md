# 上架任务管理接口清单

## 页面目标

上架任务管理页用于承接已到货的入库单明细，分配库区库位并完成上架，作为库存形成的直接入口。

## 数据模型

主表：`putaway_task`

关键字段：

- `id`：上架任务主键
- `task_code`：任务编号，全局唯一
- `inbound_order_id`：来源入库单ID
- `inbound_order_item_id`：来源入库单明细ID
- `supplier_id`：供应商ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `product_id`：产品ID
- `quantity`：待上架数量
- `status`：`1` 待分配，`2` 待上架，`3` 已完成，`4` 已取消
- `completed_at`：完成时间

## 接口清单

### 1. 查询任务列表

`GET /api/putaway-task/list`

查询参数：

- `taskCode`：可选，任务编号模糊查询
- `warehouseId`：可选，仓库筛选
- `status`：可选，状态筛选

### 2. 查询任务详情

`GET /api/putaway-task/{id}`

### 3. 分配库位

`PATCH /api/putaway-task/{id}/assign`

请求体：

```json
{
  "zoneId": 2,
  "locationId": 2
}
```

### 4. 完成上架

`PATCH /api/putaway-task/{id}/complete`

### 5. 取消任务

`PATCH /api/putaway-task/{id}/cancel`

## 常见业务错误

- `PUTAWAY_TASK_NOT_FOUND`：上架任务不存在
- `PUTAWAY_TASK_STATUS_INVALID`：当前任务状态不允许执行该操作
- `WAREHOUSE_LOCATION_ZONE_MISMATCH`：所选仓库与库区或库位不匹配

## 前端联调建议

- 页面初始化同时加载：
  - `/api/putaway-task/list`
  - `/api/warehouse/options`
  - `/api/warehouse-zone/options`
  - `/api/warehouse-location/options`
- 分配库位成功后刷新列表
- 完成上架成功后刷新列表
