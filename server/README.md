# Server

基于 `Spring Boot 3 + JDK 17 + Maven` 的后端项目。

## 启动命令

- `./mvnw spring-boot:run`：启动本地开发环境
- `./mvnw test`：执行测试
- `./mvnw package`：打包应用

## 目录组织原则

后端采用一套对 Spring Boot 项目最省心的混合组织方式：

- `common`：放全局通用能力，例如统一响应、异常、traceId、全局异常处理
- `config`：放 Spring 配置、拦截器、序列化、跨域等装配代码
- `modules`：按业务模块拆分，每个模块内部再按职责分层

## 当前目录结构

```text
server/
├─ src/
│  ├─ main/
│  │  ├─ java/com/nongcang/server/
│  │  │  ├─ ServerApplication.java
│  │  │  ├─ common/
│  │  │  │  ├─ exception/           # 错误码、业务异常
│  │  │  │  ├─ handler/             # 全局异常处理
│  │  │  │  ├─ response/            # 统一响应体
│  │  │  │  └─ trace/               # traceId 上下文与过滤器
│  │  │  ├─ config/                 # Jackson、MVC 等全局配置
│  │  │  └─ modules/
│  │  │     ├─ auth/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ domain/
│  │  │     │  │  ├─ dto/
│  │  │     │  │  └─ vo/
│  │  │     │  └─ service/
│  │  │     └─ system/
│  │  │        ├─ controller/
│  │  │        ├─ domain/
│  │  │        │  ├─ dto/
│  │  │        │  └─ vo/
│  │  │        └─ service/
│  │  └─ resources/
│  │     └─ application.properties
│  └─ test/
│     └─ java/com/nongcang/server/
│        ├─ ServerApplicationTests.java
│        └─ modules/system/controller/
│           └─ SystemDemoControllerTests.java
├─ pom.xml
├─ mvnw
└─ mvnw.cmd
```

## 接口响应规范

普通 JSON 接口统一返回以下结构：

```json
{
  "success": true,
  "code": "OK",
  "message": "操作成功",
  "data": {},
  "errors": null,
  "traceId": "4ab5d06d2d484ef89b7e3d57569a3f26"
}
```

失败时统一返回：

```json
{
  "success": false,
  "code": "VALIDATION_FAILED",
  "message": "请求参数校验失败",
  "data": null,
  "errors": [
    {
      "field": "content",
      "message": "内容不能为空"
    }
  ],
  "traceId": "4ab5d06d2d484ef89b7e3d57569a3f26"
}
```

统一字段约定：

- `success`：是否成功
- `code`：稳定错误码，前端只用它做逻辑判断
- `message`：给用户展示的友好提示
- `data`：成功响应数据
- `errors`：字段级错误信息
- `traceId`：日志排查标识，同时返回到响应头 `X-Trace-Id`

当前实现已经覆盖：

- 常规业务异常统一返回
- 参数校验异常统一返回
- 路由不存在或资源不存在统一返回 `RESOURCE_NOT_FOUND`
- Spring Security 认证失败统一返回 `401`
- 权限不足统一返回 `403`

## HTTP 状态码约定

- `200 / 201 / 204`：成功
- `400`：请求格式错误
- `401`：未登录或认证失效
- `403`：无权限
- `404`：资源不存在
- `409`：业务冲突
- `422`：参数语义校验失败
- `500`：系统异常
- `503`：下游服务不可用

不要把所有失败都包成 `200`。

## 入参与出参类型规范

模块内统一采用：

- `CreateXxxRequest` / `UpdateXxxRequest` / `QueryXxxRequest`：请求模型
- `XxxDetailResponse` / `XxxListItemResponse`：返回模型
- `PageResponse<T>`：分页返回

放置约定：

- 请求模型放 `modules/<feature>/domain/dto`
- 返回模型放 `modules/<feature>/domain/vo`
- 不直接把实体对象暴露给前端

统一约束：

- `Long` 类型主键统一按字符串返回，避免前端精度风险
- 当前已通过 Jackson 全局配置保证 `Long` 和 `long` 默认按字符串序列化
- 时间统一使用 ISO-8601 字符串
- 金额不要用浮点数
- 列表无数据时返回空数组，不返回 `null`

## 错误处理规范

系统内错误分两层：

- 业务错误：可预期，例如库存不足、状态不允许，统一抛 `BusinessException`
- 系统错误：不可预期，例如空指针、数据库异常，统一由 `GlobalExceptionHandler` 兜底

当前 `401/403` 的落地方式：

- 当前已接入 Spring Security，无 token、无效 token、刷新失败等认证问题会统一返回标准错误体
- 权限不足会统一返回标准 `403` 响应体

统一落点：

- `common/response/ApiResponse.java`：统一成功/失败包装
- `common/response/PageResponse.java`：分页结构
- `common/exception/CommonErrorCode.java`：公共错误码
- `common/exception/BusinessException.java`：业务异常
- `common/handler/GlobalExceptionHandler.java`：全局异常处理
- `common/trace/TraceIdFilter.java`：traceId 注入
- `config/JacksonConfiguration.java`：全局 `Long -> String` 序列化配置

Controller 编写规则：

- 成功时显式返回 `ApiResponse.success(...)`
- 失败时不要手写 `try/catch` 拼错误响应
- 参数校验交给 `@Valid` 和全局异常处理器

## 新增业务模块时的推荐结构

```text
src/main/java/com/nongcang/server/modules/order/
├─ controller/
├─ service/
├─ repository/
├─ domain/
│  ├─ dto/
│  ├─ entity/
│  └─ vo/
```

## 当前示例模块

当前已落了 `modules/auth` 和 `modules/system` 两个示例模块，用来演示：

- 登录：`POST /api/auth/login`
- 刷新：`POST /api/auth/refresh`
- 当前用户：`GET /api/auth/me`

- 成功响应：`GET /api/system/ping`
- 参数校验失败：`POST /api/system/echo`
- 业务错误：`GET /api/system/business-error`
- 路由不存在统一响应：任意不存在的 `/api/**` 路径

后续新增真实业务时，按这个模块结构继续扩展即可。

## 登录态方案

当前项目采用双 Token 方案：

- AccessToken：短期有效，放在 `Authorization: Bearer <token>` 请求头中
- RefreshToken：长期有效，用于 AccessToken 过期后的续期

后端约定：

- 登录成功同时签发 `accessToken` 和 `refreshToken`
- 刷新接口验证 `refreshToken` 后重新签发一对新 token
- 除 `POST /api/auth/login` 和 `POST /api/auth/refresh` 外，其他 `/api/**` 默认需要认证

当前演示账号：

- 账号：`admin`
- 手机号：`13800000000`
- 密码：`Admin@123456`

## 角色与功能矩阵

当前系统建议采用 4 类核心角色：

- `管理员`：负责系统全局配置与全部业务操作
- `仓库管理员`：负责仓库空间、入库、出库和仓储现场作业
- `库存管理员`：负责库存准确性、库存调整、盘点与库存治理
- `质检管理员`：负责质检、异常库存和损耗处理

功能权限矩阵建议如下：

| 功能模块 | 管理员 | 仓库管理员 | 库存管理员 | 质检管理员 |
| --- | --- | --- | --- | --- |
| 用户与权限管理 | 增删改查 | 无 | 无 | 无 |
| 农产品基础信息管理 | 增删改查 | 查 | 查 | 查 |
| 仓库管理 | 增删改查 | 增删改查 | 查 | 查 |
| 供应商与客户管理 | 增删改查 | 查 | 查 | 查 |
| 入库管理 | 增删改查 | 增删改查 | 查 | 查 |
| 出库管理 | 增删改查 | 增删改查 | 查 | 查 |
| 实时库存查询 | 增删改查 | 查 | 查 | 查 |
| 库存流水 | 增删改查 | 查 | 查 | 查 |
| 库存调整管理 | 增删改查 | 无 | 增删改查 | 查 |
| 库存盘点管理 | 增删改查 | 增删改查 | 增删改查 | 查 |
| 质量检测与损耗管理 | 增删改查 | 查 | 查 | 增删改查 |
| 预警与消息提醒 | 增删改查 | 查并处理仓储类 | 查并处理库存类 | 查并处理质检类 |
| 统计分析与报表 | 增删改查 | 查 | 查 | 查 |
| 智能助手高风险写操作 | 允许 | 仅仓储相关 | 仅库存相关 | 仅质检相关 |

权限设计原则：

- 角色控制功能权限：决定能否查看、创建、修改、删除、审核和导出
- 数据范围控制可见范围：后续可按仓库、库区、业务线继续细化
- 管理员拥有全局权限，其余角色按实际业务职责分配，不建议越权维护无关主数据
- 智能助手写操作应复用同一套角色判断，不绕过现有模块权限控制
