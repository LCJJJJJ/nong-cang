# 智能助手接口清单

## 页面目标

智能助手以右下角悬浮入口挂在主布局中，支持：

- 自然语言查询系统几乎全部业务数据
- 结合当前页面上下文理解用户问题
- 通过受控工具调用 DeepSeek 完成数据查询与业务解释
- 保留会话历史与工具调用审计

## 数据模型

主表：

- `assistant_session`
- `assistant_message`
- `assistant_tool_audit`

关键字段：

- `session_code`：会话编号
- `title`：会话标题
- `route_path` / `route_title`：最近一次对话所属页面上下文
- `role`：消息角色，`user` / `assistant`
- `message_type`：消息类型，文本或结构化结果
- `metadata_json`：结构化结果块，例如表格列定义、行数据、跳转页面
- `tool_name`：被调用的查询工具名称
- `tool_arguments_json` / `tool_result_json`：工具调用审计内容

## 接口清单

### 1. 查询最近会话列表

`GET /api/assistant/sessions`

返回当前登录用户最近的智能助手会话列表。

### 2. 查询指定会话消息

`GET /api/assistant/sessions/{id}/messages`

返回指定会话下的全部历史消息，按时间正序排列。

### 3. 发送对话消息

`POST /api/assistant/chat`

请求体：

- `sessionId`：可选，继续既有会话时传入
- `message`：用户输入的自然语言问题
- `routePath`：当前页面路由
- `routeTitle`：当前页面标题

返回：

- 会话摘要
- 刚保存的用户消息
- 生成的助手消息
- 助手结构化结果块

### 4. 刷新预警（通过助手工具执行）

不单独暴露页面接口，作为助手内部受控工具调用 `AlertRecordService.refreshAlertRecords()`。

## 工具清单

智能助手当前内置以下查询工具：

- `query_basic_master_data`
- `query_warehouse_data`
- `query_inbound_data`
- `query_outbound_data`
- `query_inventory_data`
- `query_quality_loss_data`
- `query_alert_message_data`
- `refresh_alerts`

所有工具都必须由后端白名单执行，不允许大模型直接访问数据库或拼接 SQL。
