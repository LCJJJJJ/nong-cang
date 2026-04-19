# 消息中心接口清单

## 页面目标

消息中心用于查看由预警实例生成的系统消息，并支持单条已读和全部已读。

## 数据模型

主表：`message_notice`

关键字段：

- `notice_code`：消息编号
- `alert_record_id`：关联预警记录ID
- `notice_type`：消息类型
- `severity`：严重级别
- `title`：标题
- `content`：内容
- `source_type`：来源类型
- `source_id`：来源ID
- `source_code`：来源编号
- `status`：`1` 未读，`2` 已读
- `created_at`：创建时间
- `read_at`：阅读时间

## 接口清单

### 1. 查询消息列表

`GET /api/message-notice/list`

### 2. 单条已读

`PATCH /api/message-notice/{id}/read`

### 3. 全部已读

`PATCH /api/message-notice/read-all`
