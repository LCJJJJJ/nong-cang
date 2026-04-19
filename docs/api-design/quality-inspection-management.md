# 质检单管理接口清单

## 页面目标

质检单管理页用于登记入库质检和在库抽检结果，并在发现不合格数量时自动生成异常锁定库存。

## 数据模型

主表：`quality_inspection`

关键字段：

- `inspection_code`：质检单编号
- `source_type`：来源类型，`INBOUND_RECORD` / `INVENTORY_STOCK`
- `source_id`：来源ID
- `source_code`：来源编号
- `source_label`：来源展示文本
- `product_id`：产品ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `inspect_quantity`：送检数量
- `qualified_quantity`：合格数量
- `unqualified_quantity`：不合格数量
- `result_status`：`1` 合格，`2` 部分不合格，`3` 不合格
- `remarks`：备注

## 接口清单

### 1. 查询质检单列表

`GET /api/quality-inspection/list`

### 2. 查询质检单详情

`GET /api/quality-inspection/{id}`

### 3. 新增质检单

`POST /api/quality-inspection`

说明：

- 送检数量不能超过来源可检数量
- 不合格数量不能超过送检数量
- 不合格数量大于 `0` 时，自动生成异常锁定库存

## 常见业务错误

- `QUALITY_INSPECTION_SOURCE_INVALID`
- `QUALITY_INSPECTION_QUANTITY_INVALID`
- `QUALITY_INSPECTION_RESULT_INVALID`
- `QUALITY_INSPECTION_SOURCE_INSUFFICIENT`
- `QUANTITY_PRECISION_INVALID`
