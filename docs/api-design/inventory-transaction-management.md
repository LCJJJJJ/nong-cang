# 库存流水接口清单

## 页面目标

库存流水页用于统一查询库存变动记录，覆盖入库、出库、库存调整和库存盘点等库存变化来源。

## 数据来源

主表：`inventory_transaction`

关键字段：

- `transaction_code`：流水编号
- `transaction_type`：流水类型
- `product_id`：产品ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `quantity`：数量变化
- `source_type`：来源类型
- `source_id`：来源ID
- `occurred_at`：发生时间
- `remarks`：备注

## 接口清单

### 1. 查询库存流水列表

`GET /api/inventory-transaction/list`

查询参数：

- `transactionCode`：可选，流水编号模糊查询
- `warehouseId`：可选，仓库筛选
- `productId`：可选，产品筛选
- `transactionType`：可选，流水类型筛选

## 前端联调建议

- 页面初始化同时加载：
  - `/api/inventory-transaction/list`
  - `/api/product-archive/options`
  - `/api/warehouse/options`
- 数量变化建议正负着色区分
