# 产品单位管理接口清单

## 页面目标

产品单位管理页用于维护农产品在产品档案、入库、出库、库存中使用的统一计量单位，为后续商品主数据和业务单据提供标准单位口径。

## 数据模型

主表：`product_unit`

关键字段：

- `id`：单位主键
- `unit_code`：单位编号，全局唯一
- `unit_name`：单位名称，全局唯一
- `unit_symbol`：单位符号
- `unit_type`：单位类型，例如重量、包装、数量
- `precision_digits`：精度位数
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询单位列表

`GET /api/product-unit/list`

查询参数：

- `unitCode`：可选，单位编号模糊查询
- `unitName`：可选，单位名称模糊查询
- `unitType`：可选，单位类型筛选
- `status`：可选，状态筛选

### 2. 查询单位选项

`GET /api/product-unit/options`

用于产品档案等模块下拉选择。

### 3. 查询单位详情

`GET /api/product-unit/{id}`

### 4. 新增单位

`POST /api/product-unit`

请求体：

```json
{
  "unitName": "托盘",
  "unitSymbol": "托",
  "unitType": "包装",
  "precisionDigits": 0,
  "status": 1,
  "sortOrder": 40,
  "remarks": "适用于托盘装载"
}
```

说明：

- 单位编号由系统自动生成
- 单位名称必须唯一

### 5. 编辑单位

`PUT /api/product-unit/{id}`

请求体与新增接口一致，但不包含 `unitCode`。

### 6. 启用/停用单位

`PATCH /api/product-unit/{id}/status`

### 7. 删除单位

`DELETE /api/product-unit/{id}`

说明：

- 当前版本如果单位已被产品档案引用，不允许删除

## 常见业务错误

- `PRODUCT_UNIT_NOT_FOUND`：单位不存在
- `PRODUCT_UNIT_CODE_DUPLICATED`：单位编号已存在
- `PRODUCT_UNIT_NAME_DUPLICATED`：单位名称已存在
- `PRODUCT_UNIT_IN_USE`：单位已被产品档案引用，不能删除

## 前端联调建议

- 页面初始化调用 `/api/product-unit/list`
- 新增、编辑、停用、删除成功后重新查询列表
- 其他模块的单位选择器调用 `/api/product-unit/options`
- 表单字段错误优先回显 `errors`
