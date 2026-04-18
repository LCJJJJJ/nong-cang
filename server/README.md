# Server

基于 `Spring Boot 3 + JDK 17 + Maven` 的后端项目。

## 启动命令

- `./mvnw spring-boot:run`：启动本地开发环境
- `./mvnw test`：执行测试
- `./mvnw package`：打包应用

## 目录组织原则

后端采用一套对 Spring Boot 项目最省心的混合组织方式：

- `common`：放全局通用能力，例如统一响应、异常、全局异常处理
- `config`：放 Spring 配置、拦截器、序列化、跨域等装配代码
- `modules`：按业务模块拆分，每个模块内部再按职责分层

这套结构比把所有代码都堆到全局 `controller / service / repository / entity` 里更适合中长期维护，原因很直接：

- 公共基础能力集中，不会和业务代码混在一起
- 业务代码按模块收敛，新增需求时更容易定位
- 模块内部保留常见 Spring 分层习惯，团队接手成本低

## 当前目录结构

```text
server/
├─ src/
│  ├─ main/
│  │  ├─ java/com/nongcang/server/
│  │  │  ├─ ServerApplication.java
│  │  │  ├─ common/                 # 全局公共能力
│  │  │  │  ├─ exception/           # 业务异常、系统异常定义
│  │  │  │  ├─ handler/             # 全局异常处理、统一返回处理
│  │  │  │  └─ response/            # 统一响应体
│  │  │  ├─ config/                 # Spring 配置
│  │  │  └─ modules/                # 业务模块根目录
│  │  └─ resources/
│  │     └─ application.properties
│  └─ test/
│     └─ java/com/nongcang/server/
│        └─ ServerApplicationTests.java
├─ pom.xml
├─ mvnw
└─ mvnw.cmd
```

## 新增业务模块时的推荐结构

新的业务功能统一放到 `src/main/java/com/nongcang/server/modules/<feature>/` 下，每个模块内部再按职责拆分：

```text
src/main/java/com/nongcang/server/modules/order/
├─ controller/                     # 对外 HTTP 接口
├─ service/                        # 业务编排
├─ repository/                     # 数据访问抽象
├─ domain/
│  ├─ dto/                         # 请求/输入模型
│  ├─ entity/                      # 领域实体或持久化对象
│  └─ vo/                          # 返回视图模型
```

## 推荐的放置约定

- 所有全局响应封装统一放到 `common/response`
- 所有业务异常和系统异常统一放到 `common/exception`
- 所有全局异常处理统一放到 `common/handler`
- 不要在项目根下堆大量全局 `controller / service / repository`
- 新业务优先新增 `modules/<feature>`，而不是继续扩散全局包
- 模块内部的 `dto / entity / vo` 只服务本模块，能不跨模块就不跨模块

## 后续演进建议

当项目接入数据库、消息队列或第三方服务后，优先保持这条原则不变：

- 公共基础设施放 `common` 或 `config`
- 业务能力放 `modules`
- 模块之间通过 service 或明确的接口协作，不直接横向依赖彼此内部实现

这样后续继续加统一响应、统一错误处理、登录鉴权和数据访问层时，不需要再推倒目录结构重来。
