# 品质等级管理接口清单

## 页面目标

品质等级管理页用于维护农产品统一品质等级口径，为产品档案、质检和损耗分析模块提供标准等级规则。

## 数据模型

主表：`quality_grade`

关键字段：

- `id`：品质等级主键
- `grade_code`：品质等级编号，全局唯一
- `grade_name`：品质等级名称，全局唯一
- `score_min` / `score_max`：分值范围
- `sort_order`：排序值
- `status`：`1` 启用，`0` 停用
- `remarks`：备注
- `created_at` / `updated_at`

## 接口清单

### 1. 查询品质等级列表

`GET /api/quality-grade/list`

查询参数：

- `gradeCode`：可选，等级编号模糊查询
- `gradeName`：可选，等级名称模糊查询
- `status`：可选，状态筛选

### 2. 查询品质等级选项

`GET /api/quality-grade/options`

### 3. 查询品质等级详情

`GET /api/quality-grade/{id}`

### 4. 新增品质等级

`POST /api/quality-grade`

### 5. 编辑品质等级

`PUT /api/quality-grade/{id}`

### 6. 启用/停用品质等级

`PATCH /api/quality-grade/{id}/status`

### 7. 删除品质等级

`DELETE /api/quality-grade/{id}`

说明：

- 当前版本如果品质等级已被产品档案引用，不允许删除

## 常见业务错误

- `QUALITY_GRADE_NOT_FOUND`：品质等级不存在
- `QUALITY_GRADE_CODE_DUPLICATED`：品质等级编号已存在
- `QUALITY_GRADE_NAME_DUPLICATED`：品质等级名称已存在
- `QUALITY_GRADE_SCORE_RANGE_INVALID`：品质等级分值范围不正确
- `QUALITY_GRADE_IN_USE`：品质等级已被产品档案引用，不能删除

## 前端联调建议

- 页面初始化调用 `/api/quality-grade/list`
- 其他模块的品质等级选择器调用 `/api/quality-grade/options`
- 新增、编辑、停用、删除成功后重新查询列表
- 表单字段错误优先回显 `errors`
