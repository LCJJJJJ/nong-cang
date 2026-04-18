# 储存条件管理接口清单

## 页面目标

储存条件管理页用于定义农产品在仓内应遵循的环境与存放规则，为产品分类、产品档案、仓库管理、入库质检与预警提醒提供统一规则依据。

## 数据模型

主表：`storage_condition`

关键字段：

- `id`：储存条件主键
- `condition_code`：条件编号，全局唯一
- `condition_name`：条件名称，全局唯一
- `storage_type`：储存类型
- `temperature_min` / `temperature_max`：温度范围
- `humidity_min` / `humidity_max`：湿度范围
- `light_requirement`：避光要求，枚举选择
- `ventilation_requirement`：通风要求，枚举选择
- `status`：`1` 启用，`0` 停用
- `sort_order`：排序值
- `remarks`：备注
- `created_at` / `updated_at`

## 核心业务规则

- 条件编号全局唯一
- 条件名称全局唯一
- 当前版本条件编号由系统自动生成
- 避光要求必须从系统预设选项中选择
- 通风要求必须从系统预设选项中选择
- 温度范围配置时，最低温不能大于最高温
- 湿度范围配置时，最低湿度不能大于最高湿度
- 停用后的储存条件不能再作为新业务的默认规则
- 当前版本允许删除储存条件，后续接入产品档案和分类绑定后应增加引用校验

## 接口清单

### 1. 查询储存条件列表

`GET /api/storage-condition/list`

查询参数：

- `conditionCode`：可选，条件编号模糊查询
- `conditionName`：可选，条件名称模糊查询
- `storageType`：可选，按储存类型筛选
- `status`：可选，按状态筛选，`1` 启用，`0` 停用

响应数据：

```json
[
  {
    "id": "1",
    "conditionCode": "SC-202604190001",
    "conditionName": "叶菜冷藏标准",
    "storageType": "冷藏",
    "temperatureMin": 2.0,
    "temperatureMax": 8.0,
    "humidityMin": 75.0,
    "humidityMax": 90.0,
    "lightRequirement": "需避强光",
    "ventilationRequirement": "普通通风",
    "status": 1,
    "statusLabel": "启用",
    "sortOrder": 10,
    "remarks": "适用于叶菜、菠菜、生菜等鲜蔬",
    "createdAt": "2026-04-19T10:00:00+08:00",
    "updatedAt": "2026-04-19T10:00:00+08:00"
  }
]
```

### 2. 查询储存条件选项

`GET /api/storage-condition/options`

用于产品分类、产品档案等模块选择默认储存条件。

响应数据：

```json
[
  {
    "id": "1",
    "label": "叶菜冷藏标准",
    "storageType": "冷藏",
    "status": 1
  }
]
```

### 3. 查询储存条件详情

`GET /api/storage-condition/{id}`

### 4. 新增储存条件

`POST /api/storage-condition`

请求体：

```json
{
  "conditionName": "鲜花恒温标准",
  "storageType": "恒温",
  "temperatureMin": 8.0,
  "temperatureMax": 12.0,
  "humidityMin": 65.0,
  "humidityMax": 75.0,
  "lightRequirement": "需避强光",
  "ventilationRequirement": "普通通风",
  "status": 1,
  "sortOrder": 40,
  "remarks": "示例新增"
}
```

说明：

- 条件编号由系统自动生成
- 前端新增表单不允许手动填写编号

### 5. 编辑储存条件

`PUT /api/storage-condition/{id}`

请求体与新增接口一致，但不包含 `conditionCode`。

说明：

- 条件编号创建后不可修改
- 编辑时始终保留原编号

### 6. 启用/停用储存条件

`PATCH /api/storage-condition/{id}/status`

请求体：

```json
{
  "status": 0
}
```

### 7. 删除储存条件

`DELETE /api/storage-condition/{id}`

## 常见业务错误

- `STORAGE_CONDITION_NOT_FOUND`：储存条件不存在
- `STORAGE_CONDITION_CODE_DUPLICATED`：储存条件编号已存在
- `STORAGE_CONDITION_NAME_DUPLICATED`：储存条件名称已存在
- `STORAGE_CONDITION_LIGHT_REQUIREMENT_INVALID`：避光要求不在允许范围内
- `STORAGE_CONDITION_VENTILATION_REQUIREMENT_INVALID`：通风要求不在允许范围内
- `STORAGE_CONDITION_TEMPERATURE_RANGE_INVALID`：温度范围不正确
- `STORAGE_CONDITION_HUMIDITY_RANGE_INVALID`：湿度范围不正确

## 前端联调建议

- 页面初始化调用 `/api/storage-condition/list`
- 表单新增/编辑时通过详情接口回填
- 新增、编辑、停用、删除成功后重新查询列表
- 字段级校验错误优先回显 `errors`
