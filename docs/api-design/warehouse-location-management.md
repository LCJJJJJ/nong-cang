# 库位管理接口清单

## 页面目标

库位管理页用于维护仓库内部可实际存放货物的库位信息，为入库上架、库存查询、拣货出库和预警分析提供最细粒度的空间维度。

## 数据模型

主表：`warehouse_location`

关键字段：

- `id`：库位主键
- `location_code`：库位编号，全局唯一
- `warehouse_id`：所属仓库ID
- `zone_id`：所属库区ID
- `location_name`：库位名称，同库区内唯一
- `location_type`：库位类型
- `capacity`：容量上限
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询库位列表

`GET /api/warehouse-location/list`

查询参数：

- `locationCode`：可选，库位编号模糊查询
- `locationName`：可选，库位名称模糊查询
- `warehouseId`：可选，所属仓库筛选
- `zoneId`：可选，所属库区筛选
- `status`：可选，状态筛选

### 2. 查询库位选项

`GET /api/warehouse-location/options`

### 3. 查询库位详情

`GET /api/warehouse-location/{id}`

### 4. 新增库位

`POST /api/warehouse-location`

### 5. 编辑库位

`PUT /api/warehouse-location/{id}`

### 6. 启用/停用库位

`PATCH /api/warehouse-location/{id}/status`

### 7. 删除库位

`DELETE /api/warehouse-location/{id}`

说明：

- 新增和编辑时，需要保证所选库位所属仓库与所属库区一致

## 常见业务错误

- `WAREHOUSE_LOCATION_NOT_FOUND`：库位不存在
- `WAREHOUSE_LOCATION_CODE_DUPLICATED`：库位编号已存在
- `WAREHOUSE_LOCATION_NAME_DUPLICATED`：同库区下库位名称已存在
- `WAREHOUSE_LOCATION_ZONE_MISMATCH`：所选仓库与库区不匹配

## 前端联调建议

- 页面初始化调用 `/api/warehouse-location/list`
- 页面同时加载 `/api/warehouse/options` 和 `/api/warehouse-zone/options`
- 其他业务模块的库位选择器调用 `/api/warehouse-location/options`
