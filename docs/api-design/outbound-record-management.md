# 出库记录查询接口清单

## 页面目标

出库记录查询页用于查看最终完成的出库结果，统一追踪客户、仓库、库区、库位、产品、数量和出库时间，为后续库存追溯和报表分析提供基础数据。

## 数据模型

主表：`outbound_record`

关键字段：

- `id`：出库记录主键
- `record_code`：记录编号，全局唯一
- `outbound_order_id`：来源出库单ID
- `outbound_task_id`：来源拣货出库任务ID
- `customer_id`：客户ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `product_id`：产品ID
- `quantity`：出库数量
- `occurred_at`：出库时间
- `remarks`：备注

## 接口清单

### 1. 查询出库记录列表

`GET /api/outbound-record/list`

查询参数：

- `recordCode`：可选，记录编号模糊查询
- `orderCode`：可选，出库单编号模糊查询
- `warehouseId`：可选，仓库筛选
- `productId`：可选，产品筛选

## 常见业务错误

- `OUTBOUND_RECORD_NOT_FOUND`：出库记录不存在

## 前端联调建议

- 页面初始化同时加载：
  - `/api/outbound-record/list`
  - `/api/warehouse/options`
  - `/api/product-archive/options`
- 当前页面只负责查询，不承担编辑动作
