# 损耗登记管理接口清单

## 页面目标

损耗登记管理页用于统一查看损耗记录，并支持两类损耗来源：

- 从异常库存转损耗
- 人工直接登记损耗

## 数据模型

主表：`loss_record`

关键字段：

- `loss_code`：损耗记录编号
- `source_type`：来源类型，`ABNORMAL_STOCK` / `DIRECT`
- `source_id`：来源ID，可为空
- `product_id`：产品ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `quantity`：损耗数量
- `loss_reason`：损耗原因
- `remarks`：备注

## 接口清单

### 1. 查询损耗记录列表

`GET /api/loss-record/list`

### 2. 人工直接登记损耗

`POST /api/loss-record/direct`

### 3. 异常库存转损耗

`POST /api/abnormal-stock/{id}/dispose-loss`
