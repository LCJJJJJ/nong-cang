# Frontend

基于 `Vite + React + TypeScript + Axios` 的前端项目。

## 启动命令

- `pnpm dev`：启动开发环境
- `pnpm build`：执行 TypeScript 编译并构建生产包
- `pnpm lint`：执行 ESLint 检查
- `pnpm preview`：预览生产构建结果

## 目录组织原则

前端采用一套兼顾效率和可维护性的混合分层方案：

- `app`：应用入口、全局样式、后续路由与 Provider 装配
- `api`：统一响应契约、Axios 实例、错误归一化
- `pages`：页面编排层
- `features`：业务模块层，每个模块自带自己的类型和 API
- `components`：跨页面共享的 UI 组件
- `hooks / utils / types`：通用能力

## 当前目录结构

```text
frontend/
├─ public/
├─ src/
│  ├─ api/
│  │  ├─ contracts.ts            # 统一响应、分页、字段错误类型
│  │  ├─ errors.ts               # AppError 与错误归一化
│  │  └─ http.ts                 # Axios 实例与 request<T>()
│  ├─ app/
│  │  ├─ App.tsx
│  │  └─ styles/
│  │     └─ index.css
│  ├─ assets/
│  │  └─ images/
│  ├─ components/
│  ├─ features/
│  │  ├─ auth/
│  │  │  ├─ api.ts               # 登录、当前用户接口
│  │  │  ├─ storage.ts           # token 和用户信息本地存储
│  │  │  ├─ GuestOnlyRoute.tsx
│  │  │  ├─ RequireAuth.tsx
│  │  │  └─ types.ts
│  │  └─ system/
│  │     ├─ api.ts               # system 示例接口
│  │     └─ types.ts             # system 示例类型
│  ├─ hooks/
│  ├─ layouts/
│  │  ├─ auth/
│  │  │  ├─ AuthLayout.tsx
│  │  │  └─ AuthLayout.css
│  │  └─ main/
│  │     ├─ MainLayout.tsx
│  │     └─ MainLayout.css
│  ├─ pages/
│  │  ├─ auth/
│  │  │  └─ login/
│  │  │     ├─ LoginPage.css
│  │  │     └─ LoginPage.tsx
│  │  └─ home/
│  │     ├─ HomePage.css
│  │     └─ HomePage.tsx
│  ├─ types/
│  ├─ utils/
│  └─ main.tsx
├─ package.json
├─ tsconfig.app.json
├─ tsconfig.json
├─ tsconfig.node.json
└─ vite.config.ts
```

## 与后端统一的接口契约

默认所有普通 JSON 接口都符合下面的后端响应结构：

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

失败时：

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

前端统一约定：

- 逻辑判断只依赖 `code`
- 用户展示优先使用 `message`
- 字段级校验失败从 `errors` 中取值
- 排障时展示 `traceId`

## 请求层规范

统一请求入口在 `src/api/http.ts`：

- `request<T>()` 负责发请求并自动解包 `ApiResponse<T>`
- 成功时直接返回 `data`
- 失败时统一抛出 `AppError`
- 同时兼容 `204 No Content` 这类空响应成功场景

不要在页面里直接写 `axios.get(...)`。页面和组件只调用 `features/<feature>/api.ts` 里的函数。

## 类型组织规范

- 全局接口契约类型放 `src/api/contracts.ts`
- 模块类型放 `src/features/<feature>/types.ts`
- 页面不要自己临时声明接口返回类型

统一约束：

- 后端主键如果是 `Long`，前端一律按字符串处理
- 当前示例接口里的 `demoId` 已按字符串返回，可直接作为联调参考
- 时间字段统一按字符串处理
- 列表没数据时按空数组处理

## 错误处理规范

统一错误收口在 `src/api/errors.ts`：

- 网络异常：提示“网络连接失败，请检查后端服务是否可用”
- `401`：提示“登录已失效，请重新登录”
- `403`：提示“当前账号无权执行该操作”
- `5xx`：提示“系统繁忙，请稍后再试”
- 业务错误：优先展示后端返回的 `message`

页面层反馈规则：

- 表单提交失败并且有字段错误：显示到具体表单项
- 普通操作失败：显示错误卡片或 toast
- 页面首次加载失败：显示错误态和重试操作
- 所有异常都可以展示 `traceId` 便于排查

## 本地联调约定

- Axios 默认请求前缀是 `/api`
- `vite.config.ts` 已将 `/api` 代理到 `http://localhost:8080`
- 如需覆盖后端地址，可配置 `VITE_API_BASE_URL`

## 登录态与刷新策略

前端当前采用本地存储 + Axios 自动续期方案：

- 登录成功后，把 `accessToken`、`refreshToken` 和当前用户信息写入 `localStorage`
- 应用启动时，如果本地已有会话，会先请求 `GET /api/auth/me` 校验登录态有效性
- `Main Layout` 通过 `RequireAuth` 保护，没有 token 时自动回到 `/login`
- `Auth Layout` 通过 `GuestOnlyRoute` 限制，已经有登录态时不会再停留在登录页
- Axios 请求会自动带上 `Authorization: Bearer <accessToken>`
- 如果普通业务请求返回 `401`，且本地仍有 `refreshToken`，前端会自动调用 `/api/auth/refresh`
- 刷新成功后自动重放原请求
- 刷新失败后清空本地登录态并跳回 `/login`

当前演示账号：

- 账号：`admin`
- 手机号：`13800000000`
- 密码：`Admin@123456`

## 当前骨架示例

首页已经接入一套完整示例：

- 登录页调用 `POST /api/auth/login`
- 登录后访问受保护的 Main Layout
- 页面加载时调用 `GET /api/system/ping`
- 表单提交时调用 `POST /api/system/echo`
- 点击演示按钮时调用 `GET /api/system/business-error`

这些示例分别覆盖：

- 登录成功与本地 token 持久化
- AccessToken 自动携带与刷新
- 成功响应
- 参数校验失败
- 业务异常失败

后续新增真实业务时，直接按 `features/<feature>/api.ts + types.ts` 的模式继续扩展即可。
