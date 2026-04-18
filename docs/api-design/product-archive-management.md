# 产品档案管理接口清单

## 页面目标

产品档案管理页用于维护农产品主数据档案，关联产品分类、单位、产地、储存条件、保质期规则和品质等级，为后续入库、出库、库存和质检业务提供统一商品主数据。

## 数据模型

主表：`product_archive`

关键字段：

- `id`：产品档案主键
- `product_code`：产品编号，全局唯一
- `product_name`：产品名称，全局唯一
- `product_specification`：产品规格
- `category_id`：产品分类ID
- `unit_id`：产品单位ID
- `origin_id`：产地信息ID
- `storage_condition_id`：储存条件ID
- `shelf_life_rule_id`：保质期规则ID
- `quality_grade_id`：品质等级ID
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 核心业务规则

- 产品编号由系统自动生成
- 产品名称全局唯一
- 保质期规则如果绑定了适用分类，则必须与产品分类一致
- 保质期规则如果绑定了储存条件，则必须与产品储存条件一致

## 接口清单

### 1. 查询产品档案列表

`GET /api/product-archive/list`

查询参数：

- `productCode`：可选，产品编号模糊查询
- `productName`：可选，产品名称模糊查询
- `categoryId`：可选，产品分类筛选
- `status`：可选，状态筛选

### 2. 查询产品档案详情

`GET /api/product-archive/{id}`

### 3. 新增产品档案

`POST /api/product-archive`

### 4. 编辑产品档案

`PUT /api/product-archive/{id}`

### 5. 启用/停用产品档案

`PATCH /api/product-archive/{id}/status`

### 6. 删除产品档案

`DELETE /api/product-archive/{id}`

## 常见业务错误

- `PRODUCT_ARCHIVE_NOT_FOUND`：产品档案不存在
- `PRODUCT_ARCHIVE_CODE_DUPLICATED`：产品档案编号已存在
- `PRODUCT_ARCHIVE_NAME_DUPLICATED`：产品档案名称已存在
- `PRODUCT_ARCHIVE_RULE_SCOPE_INVALID`：保质期规则与当前产品分类或储存条件不匹配

## 前端联调建议

- 页面初始化同时加载：
  - `/api/product-archive/list`
  - `/api/category/options`
  - `/api/product-unit/options`
  - `/api/product-origin/options`
  - `/api/storage-condition/options`
  - `/api/shelf-life-rule/options`
  - `/api/quality-grade/options`
- 新增、编辑、停用、删除成功后重新查询列表
- 表单字段错误优先回显 `errors`
