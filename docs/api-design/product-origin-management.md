# 产地信息管理接口清单

## 页面目标

产地信息管理页用于维护农产品的标准产地信息，为产品档案、质量追溯、统计分析等模块提供统一产地口径。

## 数据模型

主表：`product_origin`

关键字段：

- `id`：产地主键
- `origin_code`：产地编号，全局唯一
- `origin_name`：产地名称，全局唯一
- `country_name`：国家名称
- `province_name`：省份名称
- `city_name`：城市名称
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询产地列表

`GET /api/product-origin/list`

查询参数：

- `originCode`：可选，产地编号模糊查询
- `originName`：可选，产地名称模糊查询
- `provinceName`：可选，省份名称筛选
- `status`：可选，状态筛选

### 2. 查询产地选项

`GET /api/product-origin/options`

### 3. 查询产地详情

`GET /api/product-origin/{id}`

### 4. 新增产地

`POST /api/product-origin`

### 5. 编辑产地

`PUT /api/product-origin/{id}`

### 6. 启用/停用产地

`PATCH /api/product-origin/{id}/status`

### 7. 删除产地

`DELETE /api/product-origin/{id}`

说明：

- 当前版本如果产地已被产品档案引用，不允许删除

## 常见业务错误

- `PRODUCT_ORIGIN_NOT_FOUND`：产地不存在
- `PRODUCT_ORIGIN_CODE_DUPLICATED`：产地编号已存在
- `PRODUCT_ORIGIN_NAME_DUPLICATED`：产地名称已存在
- `PRODUCT_ORIGIN_IN_USE`：产地已被产品档案引用，不能删除

## 前端联调建议

- 页面初始化调用 `/api/product-origin/list`
- 其他模块的产地选择器调用 `/api/product-origin/options`
- 新增、编辑、停用、删除成功后重新查询列表
- 表单字段错误优先回显 `errors`
