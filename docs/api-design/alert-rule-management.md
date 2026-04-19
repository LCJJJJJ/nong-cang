# 预警规则管理接口清单

## 页面目标

预警规则管理页用于维护系统内置预警规则的启用状态、严重级别、阈值和说明，并支持手动刷新预警实例。

## 数据模型

主表：`alert_rule`

关键字段：

- `rule_code`：规则编号
- `rule_name`：规则名称
- `alert_type`：预警类型
- `severity`：严重级别
- `threshold_value`：阈值
- `threshold_unit`：阈值单位
- `enabled`：启用状态
- `description`：规则说明

## 接口清单

### 1. 查询预警规则列表

`GET /api/alert-rule/list`

### 2. 查询预警规则详情

`GET /api/alert-rule/{id}`

### 3. 更新预警规则

`PUT /api/alert-rule/{id}`

### 4. 切换预警规则状态

`PATCH /api/alert-rule/{id}/status`

### 5. 手动刷新预警

`POST /api/alert-record/refresh`

## 常见业务错误

- `ALERT_RULE_NOT_FOUND`
- `ALERT_RULE_THRESHOLD_INVALID`
- `ALERT_RULE_SEVERITY_INVALID`
