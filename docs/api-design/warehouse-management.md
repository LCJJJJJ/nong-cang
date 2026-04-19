# 仓库信息管理接口清单

## 页面目标

仓库信息管理页用于维护仓库主数据，为库区、库位、入库、出库、库存和预警模块提供统一仓库维度。

## 数据模型

主表：`warehouse`

关键字段：

- `id`：仓库主键
- `warehouse_code`：仓库编号，全局唯一
- `warehouse_name`：仓库名称，全局唯一
- `warehouse_type`：仓库类型
- `manager_name`：负责人
- `contact_phone`：联系电话
- `address`：仓库地址
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询仓库列表

`GET /api/warehouse/list`

查询参数：

- `warehouseCode`：可选，仓库编号模糊查询
- `warehouseName`：可选，仓库名称模糊查询
- `warehouseType`：可选，仓库类型筛选
- `status`：可选，状态筛选

### 2. 查询仓库选项

`GET /api/warehouse/options`

### 3. 查询仓库详情

`GET /api/warehouse/{id}`

### 4. 新增仓库

`POST /api/warehouse`

### 5. 编辑仓库

`PUT /api/warehouse/{id}`

### 6. 启用/停用仓库

`PATCH /api/warehouse/{id}/status`

### 7. 删除仓库

`DELETE /api/warehouse/{id}`

说明：

- 当前版本如果仓库下存在库区，不允许删除

## 常见业务错误

- `WAREHOUSE_NOT_FOUND`：仓库不存在
- `WAREHOUSE_CODE_DUPLICATED`：仓库编号已存在
- `WAREHOUSE_NAME_DUPLICATED`：仓库名称已存在
- `WAREHOUSE_HAS_ZONES`：仓库下存在库区，不能删除

## 前端联调建议

- 页面初始化调用 `/api/warehouse/list`
- 库区、库位等模块的仓库选择器调用 `/api/warehouse/options`
- 新增、编辑、停用、删除成功后重新查询列表
