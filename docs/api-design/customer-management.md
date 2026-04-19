# 客户管理接口清单

## 页面目标

客户管理页用于维护出库业务使用的客户主数据，为后续出库单、配送协同和客户去向分析提供统一对象口径。

## 数据模型

主表：`customer`

关键字段：

- `id`：客户主键
- `customer_code`：客户编号，全局唯一
- `customer_name`：客户名称，全局唯一
- `customer_type`：客户类型
- `contact_name`：联系人
- `contact_phone`：联系电话
- `region_name`：所在地区
- `address`：详细地址
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询客户列表

`GET /api/customer/list`

查询参数：

- `customerCode`：可选，客户编号模糊查询
- `customerName`：可选，客户名称模糊查询
- `contactName`：可选，联系人筛选
- `status`：可选，状态筛选

### 2. 查询客户选项

`GET /api/customer/options`

### 3. 查询客户详情

`GET /api/customer/{id}`

### 4. 新增客户

`POST /api/customer`

### 5. 编辑客户

`PUT /api/customer/{id}`

### 6. 启用/停用客户

`PATCH /api/customer/{id}/status`

### 7. 删除客户

`DELETE /api/customer/{id}`

## 常见业务错误

- `CUSTOMER_NOT_FOUND`：客户不存在
- `CUSTOMER_CODE_DUPLICATED`：客户编号已存在
- `CUSTOMER_NAME_DUPLICATED`：客户名称已存在

## 前端联调建议

- 页面初始化调用 `/api/customer/list`
- 后续出库单客户选择器调用 `/api/customer/options`
- 新增、编辑、停用、删除成功后重新查询列表
