# 入库记录查询接口清单

## 页面目标

入库记录查询页用于查询已经完成上架的入库结果，为追溯、报表和库存来源分析提供只读记录。

## 数据模型

主表：`inbound_record`

关键字段：

- `id`：入库记录主键
- `record_code`：记录编号，全局唯一
- `inbound_order_id`：来源入库单ID
- `putaway_task_id`：来源上架任务ID
- `supplier_id`：供应商ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `product_id`：产品ID
- `quantity`：入库数量
- `occurred_at`：入库时间
- `remarks`：备注

## 接口清单

### 1. 查询入库记录列表

`GET /api/inbound-record/list`

查询参数：

- `recordCode`：可选，记录编号模糊查询
- `orderCode`：可选，入库单编号模糊查询
- `warehouseId`：可选，仓库筛选
- `productId`：可选，产品筛选

## 前端联调建议

- 页面初始化调用 `/api/inbound-record/list`
- 页面同时加载 `/api/warehouse/options` 和 `/api/product-archive/options`
- 该页面只读，不承担编辑职责
