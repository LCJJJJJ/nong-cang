# 预警中心接口清单

## 页面目标

预警中心用于展示当前活跃、已忽略、已恢复的预警记录，并支持手动刷新和忽略预警。

## 数据模型

主表：`alert_record`

关键字段：

- `alert_code`：预警编号
- `rule_code`：规则编号
- `alert_type`：预警类型
- `severity`：严重级别
- `source_type`：来源类型
- `source_id`：来源ID
- `source_code`：来源编号
- `title`：标题
- `content`：内容
- `status`：`1` 活跃，`2` 已忽略，`3` 已恢复
- `occurred_at`：触发时间
- `resolved_at`：恢复时间

## 接口清单

### 1. 查询预警列表

`GET /api/alert-record/list`

### 2. 手动刷新预警

`POST /api/alert-record/refresh`

### 3. 忽略预警

`PATCH /api/alert-record/{id}/ignore`
