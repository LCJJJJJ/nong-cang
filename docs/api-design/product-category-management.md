# 产品分类管理接口清单

## 页面目标

产品分类管理页用于维护农产品分类树、默认储存规则和基础治理口径，为产品档案、库存和预警模块提供统一分类主数据。

## 数据模型

主表：`product_category`

关键字段：

- `id`：分类主键
- `category_code`：分类编号，全局唯一
- `category_name`：分类名称
- `parent_id`：上级分类ID，顶级分类为空
- `category_level`：层级，从 `1` 开始
- `ancestor_path`：祖先路径，形如 `/1/3/`
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `default_storage_type`：默认储存类型
- `default_storage_condition`：默认储存条件
- `shelf_life_days`：保质期基准天数
- `warning_days`：预警提前天数
- `require_quality_check`：是否要求质检
- `remarks`：备注
- `created_at` / `updated_at`

## 核心业务规则

- 分类编号全局唯一
- 同一父分类下分类名称唯一
- 当前版本最大层级限制为 `3`
- 上级分类不能选择自己，也不能选择自己的下级分类
- 删除分类前必须校验“是否存在子分类”
- 当前版本不允许删除存在子分类的分类
- 停用分类后不影响历史数据，但不建议继续挂载新产品

## 统一响应约定

遵循 `server/README.md` 与 `frontend/README.md` 现有统一响应协议：

```json
{
  "success": true,
  "code": "OK",
  "message": "操作成功",
  "data": {},
  "traceId": "..."
}
```

## 接口清单

### 1. 查询分类树

`GET /api/category/tree`

查询参数：

- `categoryCode`：可选，分类编号模糊查询
- `categoryName`：可选，分类名称模糊查询
- `parentId`：可选，按上级分类筛选
- `level`：可选，按层级筛选
- `status`：可选，按状态筛选，`1` 启用，`0` 停用

响应数据：

```json
[
  {
    "id": "1",
    "categoryCode": "CAT-A00",
    "categoryName": "新鲜蔬菜",
    "parentId": null,
    "categoryLevel": 1,
    "ancestorPath": "/",
    "sortOrder": 10,
    "status": 1,
    "statusLabel": "启用",
    "defaultStorageType": null,
    "defaultStorageCondition": null,
    "shelfLifeDays": null,
    "warningDays": 2,
    "requireQualityCheck": false,
    "remarks": "蔬菜大类",
    "createdAt": "2026-04-19T10:00:00+08:00",
    "updatedAt": "2026-04-19T10:00:00+08:00",
    "children": []
  }
]
```

说明：

- 结果为树结构
- 如果子节点命中筛选条件，父节点也会保留，方便前端展示完整层级

### 2. 查询父分类选项树

`GET /api/category/options`

用于搜索下拉框、表单父分类选择器。

响应数据：

```json
[
  {
    "id": "1",
    "label": "新鲜蔬菜",
    "categoryLevel": 1,
    "ancestorPath": "/",
    "children": []
  }
]
```

### 3. 查询分类详情

`GET /api/category/{id}`

响应数据：

```json
{
  "id": "2",
  "categoryCode": "CAT-A01",
  "categoryName": "叶菜类",
  "parentId": "1",
  "parentName": "新鲜蔬菜",
  "categoryLevel": 2,
  "ancestorPath": "/1/",
  "sortOrder": 1,
  "status": 1,
  "statusLabel": "启用",
  "defaultStorageType": "冷藏",
  "defaultStorageCondition": "2-8°C",
  "shelfLifeDays": 5,
  "warningDays": 1,
  "requireQualityCheck": true,
  "remarks": "叶菜默认规则",
  "createdAt": "2026-04-19T10:00:00+08:00",
  "updatedAt": "2026-04-19T10:00:00+08:00"
}
```

### 4. 新增分类

`POST /api/category`

请求体：

```json
{
  "categoryName": "粮油干货",
  "parentId": null,
  "sortOrder": 30,
  "status": 1,
  "defaultStorageType": "阴凉干燥",
  "defaultStorageCondition": "常温避光",
  "shelfLifeDays": 180,
  "warningDays": 15,
  "requireQualityCheck": false,
  "remarks": "首版新分类"
}
```

说明：

- 分类编号由系统自动生成，前端新增表单不允许手动填写

响应数据：返回创建后的详情结构。

### 5. 编辑分类

`PUT /api/category/{id}`

请求体：

```json
{
  "categoryName": "叶菜类",
  "parentId": "1",
  "sortOrder": 1,
  "status": 1,
  "defaultStorageType": "冷藏",
  "defaultStorageCondition": "2-8°C",
  "shelfLifeDays": 5,
  "warningDays": 1,
  "requireQualityCheck": true,
  "remarks": "叶菜默认规则"
}
```

说明：

- 支持修改上级分类
- 修改上级分类时会同步刷新当前节点及其所有下级节点的层级与祖先路径
- 分类编号不可编辑，更新时保留原编号

### 6. 启用/停用分类

`PATCH /api/category/{id}/status`

请求体：

```json
{
  "status": 0
}
```

### 7. 删除分类

`DELETE /api/category/{id}`

删除前置校验：

- 如果存在子分类，返回业务错误
- 当前版本未接产品档案引用校验，后续接入产品主数据后应增加引用检查

## 常见业务错误

- `CATEGORY_NOT_FOUND`：分类不存在
- `CATEGORY_CODE_DUPLICATED`：分类编号已存在
- `CATEGORY_NAME_DUPLICATED`：同级分类名称重复
- `CATEGORY_PARENT_NOT_FOUND`：上级分类不存在
- `CATEGORY_PARENT_INVALID`：不能选择自己或自己的下级分类作为父分类
- `CATEGORY_LEVEL_EXCEEDED`：分类层级超过当前系统限制
- `CATEGORY_HAS_CHILDREN`：当前分类下存在子分类，不能删除

## 前端联调建议

- 页面初始化调用 `/api/category/tree` 与 `/api/category/options`
- 筛选后重新请求 `/api/category/tree`
- 树表格只处理展示与展开状态，不在前端重复造树
- 新增/编辑成功后重新请求树数据和选项数据
- 停用/启用与删除成功后重新请求树数据
- 表单字段错误优先回显 `errors`
