# 供应商管理接口清单

## 页面目标

供应商管理页用于维护入库业务使用的供应商主数据，为后续入库单、到货质检和供应来源分析提供统一对象口径。

## 数据模型

主表：`supplier`

关键字段：

- `id`：供应商主键
- `supplier_code`：供应商编号，全局唯一
- `supplier_name`：供应商名称，全局唯一
- `supplier_type`：供应商类型
- `contact_name`：联系人
- `contact_phone`：联系电话
- `region_name`：所在地区
- `address`：详细地址
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询供应商列表

`GET /api/supplier/list`

查询参数：

- `supplierCode`：可选，供应商编号模糊查询
- `supplierName`：可选，供应商名称模糊查询
- `contactName`：可选，联系人筛选
- `status`：可选，状态筛选

### 2. 查询供应商选项

`GET /api/supplier/options`

### 3. 查询供应商详情

`GET /api/supplier/{id}`

### 4. 新增供应商

`POST /api/supplier`

### 5. 编辑供应商

`PUT /api/supplier/{id}`

### 6. 启用/停用供应商

`PATCH /api/supplier/{id}/status`

### 7. 删除供应商

`DELETE /api/supplier/{id}`

## 常见业务错误

- `SUPPLIER_NOT_FOUND`：供应商不存在
- `SUPPLIER_CODE_DUPLICATED`：供应商编号已存在
- `SUPPLIER_NAME_DUPLICATED`：供应商名称已存在

## 前端联调建议

- 页面初始化调用 `/api/supplier/list`
- 后续入库单供应商选择器调用 `/api/supplier/options`
- 新增、编辑、停用、删除成功后重新查询列表
