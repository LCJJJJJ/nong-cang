# Frontend

基于 `Vite + React + TypeScript` 的前端项目。

## 启动命令

- `pnpm dev`：启动开发环境
- `pnpm build`：执行 TypeScript 编译并构建生产包
- `pnpm lint`：执行 ESLint 检查
- `pnpm preview`：预览生产构建结果

## 目录组织原则

当前采用 React 项目里最常见、也最容易长期维护的一种混合分层方案：

- `app`：应用级入口、全局样式、Provider、路由装配
- `pages`：路由页面，只负责页面编排，不堆业务细节
- `features`：按业务领域拆分的功能模块，后续优先往这里增长
- `components`：跨页面复用的共享组件
- `api`：Axios 实例、拦截器、接口模块
- `hooks`：跨业务复用的 React Hooks
- `types`：共享 TypeScript 类型
- `utils`：纯函数工具
- `assets`：图片、图标等静态资源

这套结构兼顾了两个目标：

- 小项目阶段不会被过度设计拖慢
- 业务变大后可以自然演进到按 `feature` 拆分，而不是把所有代码堆在 `components` 或 `utils` 里

## 当前目录结构

```text
frontend/
├─ public/                      # 不经过打包导入、按原路径输出的静态资源
├─ src/
│  ├─ api/                      # 请求层：Axios 实例、接口模块、拦截器
│  ├─ app/                      # 应用装配层
│  │  ├─ App.tsx
│  │  └─ styles/
│  │     └─ index.css
│  ├─ assets/                   # 由代码导入的静态资源
│  │  └─ images/
│  │     ├─ hero.png
│  │     ├─ react.svg
│  │     └─ vite.svg
│  ├─ components/               # 共享组件
│  ├─ features/                 # 业务模块
│  ├─ hooks/                    # 通用 Hooks
│  ├─ pages/                    # 路由页面
│  │  └─ home/
│  │     ├─ HomePage.css
│  │     └─ HomePage.tsx
│  ├─ types/                    # 共享类型
│  ├─ utils/                    # 工具函数
│  └─ main.tsx                  # 前端入口
├─ index.html
├─ package.json
├─ tsconfig.app.json
├─ tsconfig.json
├─ tsconfig.node.json
└─ vite.config.ts
```

## 推荐的放置约定

- 新页面放到 `src/pages/<page-name>/`
- 新业务功能优先放到 `src/features/<feature-name>/`
- 只有跨多个页面复用的 UI 组件才放到 `src/components/`
- 接口请求统一从 `src/api/` 发起，不在页面里直接写 Axios 细节
- 页面只负责编排，业务逻辑逐步下沉到 `features`

## 业务增长后的推荐形态

当某个业务模块开始变大时，推荐按下面的形式继续拆：

```text
src/features/auth/
├─ api/
├─ components/
├─ hooks/
├─ types.ts
└─ utils.ts
```

这样可以让页面层保持薄，业务边界更稳定，后续多人协作也更容易。
