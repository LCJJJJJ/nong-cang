# 供应商与客户管理 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成供应商管理和客户管理两个页面的数据库、接口、前端联调与测试，实现供应商与客户管理菜单完整闭环。

**Architecture:** 采用 `supplier -> customer` 的顺序实现。两个模块沿用统一的后台管理页结构，后端按 `modules/<feature>` 分层，前端按 `features/<feature>` 与 `pages/<feature>` 组织，并提供选项接口供后续入库、出库模块引用。

**Tech Stack:** Spring Boot 3、JDK 17、NamedParameterJdbcTemplate、MySQL、React、TypeScript、Vite、Axios。

---

### Task 1: 供应商管理

**Files:**
- Create: `server/sql/2026-04-19-supplier.sql`
- Create: `docs/api-design/supplier-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/controller/SupplierController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/service/SupplierService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/repository/SupplierRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/dto/SupplierCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/dto/SupplierListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/dto/SupplierStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/dto/SupplierUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/entity/SupplierEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/vo/SupplierDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/vo/SupplierListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/supplier/domain/vo/SupplierOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/supplier/controller/SupplierControllerTests.java`
- Create: `frontend/src/features/supplier/api.ts`
- Create: `frontend/src/features/supplier/types.ts`
- Create: `frontend/src/pages/supplier/SupplierPage.tsx`
- Create: `frontend/src/pages/supplier/SupplierPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写供应商模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、状态切换、选项接口
- [ ] 实现前端页面、路由、菜单和联调
- [ ] 运行 `./mvnw -Dtest=SupplierControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 2: 客户管理

**Files:**
- Create: `server/sql/2026-04-19-customer.sql`
- Create: `docs/api-design/customer-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/controller/CustomerController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/service/CustomerService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/repository/CustomerRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/dto/CustomerCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/dto/CustomerListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/dto/CustomerStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/dto/CustomerUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/entity/CustomerEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/vo/CustomerDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/vo/CustomerListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/customer/domain/vo/CustomerOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/customer/controller/CustomerControllerTests.java`
- Create: `frontend/src/features/customer/api.ts`
- Create: `frontend/src/features/customer/types.ts`
- Create: `frontend/src/pages/customer/CustomerPage.tsx`
- Create: `frontend/src/pages/customer/CustomerPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写客户模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、状态切换、选项接口
- [ ] 实现前端页面、路由、菜单和联调
- [ ] 运行 `./mvnw -Dtest=CustomerControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 3: 供应商与客户管理总体验证

**Files:**
- Modify: `frontend/src/layouts/main/MainLayout.tsx`
- Verify: `server/src/main/java/com/nongcang/server/modules/supplier/**`
- Verify: `server/src/main/java/com/nongcang/server/modules/customer/**`
- Verify: `frontend/src/pages/supplier/**`
- Verify: `frontend/src/pages/customer/**`

- [ ] 运行 `./mvnw test`
- [ ] 运行 `pnpm lint`
- [ ] 运行 `pnpm build`
- [ ] 重启前后端服务到最新代码
- [ ] 提交
