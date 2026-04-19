# 实时库存查询接口清单

## 页面目标

实时库存查询页用于查看当前库存分布和可用数量，统一追踪产品在仓库、库区、库位维度上的现存、预留和可用情况。

## 数据来源

基础库存来自：`inventory_stock`

预留数量来自：`outbound_task`

预留规则：

- 统计 `status in (2, 3)` 的拣货出库任务数量
- `现存数量 - 预留数量 = 可用数量`

## 接口清单

### 1. 查询实时库存列表

`GET /api/inventory-stock/list`

查询参数：

- `productId`：可选，产品筛选
- `warehouseId`：可选，仓库筛选
- `zoneId`：可选，库区筛选

## 前端联调建议

- 页面初始化同时加载：
  - `/api/inventory-stock/list`
  - `/api/product-archive/options`
  - `/api/warehouse/options`
  - `/api/warehouse-zone/options`
- 表格展示建议固定：
  - 产品
  - 仓库
  - 库区
  - 库位
  - 现存数量
  - 预留数量
  - 可用数量
  - 更新时间
