# 库区管理接口清单

## 页面目标

库区管理页用于维护仓库内部的功能分区，为库位管理、入库上架、库存预警和出库拣选提供统一的库区维度。

## 数据模型

主表：`warehouse_zone`

关键字段：

- `id`：库区主键
- `zone_code`：库区编号，全局唯一
- `warehouse_id`：所属仓库ID
- `zone_name`：库区名称，同仓库内唯一
- `zone_type`：库区类型
- `temperature_min` / `temperature_max`：温度范围
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询库区列表

`GET /api/warehouse-zone/list`

查询参数：

- `zoneCode`：可选，库区编号模糊查询
- `zoneName`：可选，库区名称模糊查询
- `warehouseId`：可选，所属仓库筛选
- `status`：可选，状态筛选

### 2. 查询库区选项

`GET /api/warehouse-zone/options`

### 3. 查询库区详情

`GET /api/warehouse-zone/{id}`

### 4. 新增库区

`POST /api/warehouse-zone`

### 5. 编辑库区

`PUT /api/warehouse-zone/{id}`

### 6. 启用/停用库区

`PATCH /api/warehouse-zone/{id}/status`

### 7. 删除库区

`DELETE /api/warehouse-zone/{id}`

说明：

- 当前版本如果库区下存在库位，不允许删除

## 常见业务错误

- `WAREHOUSE_ZONE_NOT_FOUND`：库区不存在
- `WAREHOUSE_ZONE_CODE_DUPLICATED`：库区编号已存在
- `WAREHOUSE_ZONE_NAME_DUPLICATED`：同仓库下库区名称已存在
- `WAREHOUSE_ZONE_HAS_LOCATIONS`：库区下存在库位，不能删除

## 前端联调建议

- 页面初始化调用 `/api/warehouse-zone/list`
- 页面同时加载 `/api/warehouse/options`
- 库位管理等模块的库区选择器调用 `/api/warehouse-zone/options`
