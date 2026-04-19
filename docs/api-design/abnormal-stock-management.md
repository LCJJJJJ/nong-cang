# 异常库存管理接口清单

## 页面目标

异常库存管理页用于查看由质检单锁定的不合格库存，并为后续释放回可用库存或转损耗处理提供来源数据。

## 数据模型

主表：`abnormal_stock`

关键字段：

- `abnormal_code`：异常库存编号
- `quality_inspection_id`：来源质检单ID
- `inspection_code`：来源质检单编号
- `product_id`：产品ID
- `warehouse_id`：仓库ID
- `zone_id`：库区ID
- `location_id`：库位ID
- `locked_quantity`：锁定数量
- `status`：`1` 锁定中，`2` 已释放，`3` 已转损耗
- `reason`：异常原因
- `remarks`：备注

## 接口清单

### 1. 查询异常库存列表

`GET /api/abnormal-stock/list`

### 2. 查询异常库存详情

`GET /api/abnormal-stock/{id}`

### 3. 查询异常库存选项

`GET /api/abnormal-stock/options`

说明：

- 仅返回 `锁定中` 的异常库存
- 后续损耗登记会直接引用该接口

### 4. 释放异常库存

`PATCH /api/abnormal-stock/{id}/release`

说明：

- 仅 `锁定中` 状态允许释放
- 释放后恢复为可用库存，不改动库存快照数量

### 5. 转损耗处理

`POST /api/abnormal-stock/{id}/dispose-loss`

说明：

- 仅 `锁定中` 状态允许转损耗
- 转损耗后会减少库存快照并生成 `LOSS` 类型库存流水
- 同时新增损耗记录
