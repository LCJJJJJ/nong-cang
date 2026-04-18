# 保质期规则管理接口清单

## 页面目标

保质期规则管理页用于维护不同产品分类和储存条件下的保质期与预警天数规则，为产品档案、库存预警和质检提醒提供统一依据。

## 数据模型

主表：`shelf_life_rule`

关键字段：

- `id`：保质期规则主键
- `rule_code`：规则编号，全局唯一
- `rule_name`：规则名称，全局唯一
- `category_id`：适用分类ID
- `storage_condition_id`：适用储存条件ID
- `shelf_life_days`：保质期天数
- `warning_days`：预警提前天数
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询规则列表

`GET /api/shelf-life-rule/list`

查询参数：

- `ruleCode`：可选，规则编号模糊查询
- `ruleName`：可选，规则名称模糊查询
- `categoryId`：可选，适用分类筛选
- `status`：可选，状态筛选

### 2. 查询规则选项

`GET /api/shelf-life-rule/options`

### 3. 查询规则详情

`GET /api/shelf-life-rule/{id}`

### 4. 新增规则

`POST /api/shelf-life-rule`

### 5. 编辑规则

`PUT /api/shelf-life-rule/{id}`

### 6. 启用/停用规则

`PATCH /api/shelf-life-rule/{id}/status`

### 7. 删除规则

`DELETE /api/shelf-life-rule/{id}`

说明：

- 当前版本如果规则已被产品档案引用，不允许删除

## 常见业务错误

- `SHELF_LIFE_RULE_NOT_FOUND`：保质期规则不存在
- `SHELF_LIFE_RULE_CODE_DUPLICATED`：保质期规则编号已存在
- `SHELF_LIFE_RULE_NAME_DUPLICATED`：保质期规则名称已存在
- `SHELF_LIFE_RULE_IN_USE`：保质期规则已被产品档案引用，不能删除

## 前端联调建议

- 页面初始化调用 `/api/shelf-life-rule/list`
- 同时加载 `/api/category/options` 和 `/api/storage-condition/options` 用于筛选和表单
- 其他模块的保质期规则选择器调用 `/api/shelf-life-rule/options`
- 新增、编辑、停用、删除成功后重新查询列表
