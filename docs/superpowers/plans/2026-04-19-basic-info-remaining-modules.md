# 农产品基础信息管理剩余模块 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成农产品基础信息管理剩余 5 个页面的数据库、接口、前端页面和联调，实现该菜单下完整业务闭环。

**Architecture:** 按数据依赖顺序实现 `产品单位管理 -> 产地信息管理 -> 品质等级管理 -> 保质期规则管理 -> 产品档案管理`。后端继续沿用 `modules/<feature>/controller|service|repository|domain` 模式，前端沿用 `features/<feature>` + `pages/<feature>` 模式，并保持统一响应、统一错误处理与现有 UI/UX 风格。

**Tech Stack:** Spring Boot 3、JDK 17、NamedParameterJdbcTemplate、MySQL、React、TypeScript、Vite、Axios。

---

### Task 1: 产品单位管理

**Files:**
- Create: `server/sql/2026-04-19-product-unit.sql`
- Create: `docs/api-design/product-unit-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/controller/ProductUnitController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/service/ProductUnitService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/repository/ProductUnitRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/dto/ProductUnitCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/dto/ProductUnitListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/dto/ProductUnitStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/dto/ProductUnitUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/entity/ProductUnitEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/vo/ProductUnitDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/vo/ProductUnitListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productunit/domain/vo/ProductUnitOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/productunit/controller/ProductUnitControllerTests.java`
- Create: `frontend/src/features/productunit/api.ts`
- Create: `frontend/src/features/productunit/types.ts`
- Create: `frontend/src/pages/product-unit/ProductUnitPage.tsx`
- Create: `frontend/src/pages/product-unit/ProductUnitPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] **Step 1: 写产品单位模块测试和接口文档**
- [ ] **Step 2: 落库并写入示例数据**
- [ ] **Step 3: 实现后端 CRUD、状态切换、选项接口**
- [ ] **Step 4: 实现前端页面、路由、菜单和联调**
- [ ] **Step 5: 运行 `./mvnw -Dtest=ProductUnitControllerTests test`、`pnpm lint`、`pnpm build`**
- [ ] **Step 6: 提交**

### Task 2: 产地信息管理

**Files:**
- Create: `server/sql/2026-04-19-product-origin.sql`
- Create: `docs/api-design/product-origin-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/controller/ProductOriginController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/service/ProductOriginService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/repository/ProductOriginRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/dto/ProductOriginCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/dto/ProductOriginListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/dto/ProductOriginStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/dto/ProductOriginUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/entity/ProductOriginEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/vo/ProductOriginDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/vo/ProductOriginListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productorigin/domain/vo/ProductOriginOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/productorigin/controller/ProductOriginControllerTests.java`
- Create: `frontend/src/features/productorigin/api.ts`
- Create: `frontend/src/features/productorigin/types.ts`
- Create: `frontend/src/pages/product-origin/ProductOriginPage.tsx`
- Create: `frontend/src/pages/product-origin/ProductOriginPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] **Step 1: 写产地模块测试和接口文档**
- [ ] **Step 2: 落库并写入示例数据**
- [ ] **Step 3: 实现后端 CRUD、状态切换、选项接口**
- [ ] **Step 4: 实现前端页面、路由、菜单和联调**
- [ ] **Step 5: 运行 `./mvnw -Dtest=ProductOriginControllerTests test`、`pnpm lint`、`pnpm build`**
- [ ] **Step 6: 提交**

### Task 3: 品质等级管理

**Files:**
- Create: `server/sql/2026-04-19-quality-grade.sql`
- Create: `docs/api-design/quality-grade-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/controller/QualityGradeController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/service/QualityGradeService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/repository/QualityGradeRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/dto/QualityGradeCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/dto/QualityGradeListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/dto/QualityGradeStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/dto/QualityGradeUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/entity/QualityGradeEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/vo/QualityGradeDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/vo/QualityGradeListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/qualitygrade/domain/vo/QualityGradeOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/qualitygrade/controller/QualityGradeControllerTests.java`
- Create: `frontend/src/features/qualitygrade/api.ts`
- Create: `frontend/src/features/qualitygrade/types.ts`
- Create: `frontend/src/pages/quality-grade/QualityGradePage.tsx`
- Create: `frontend/src/pages/quality-grade/QualityGradePage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] **Step 1: 写品质等级模块测试和接口文档**
- [ ] **Step 2: 落库并写入示例数据**
- [ ] **Step 3: 实现后端 CRUD、状态切换、选项接口**
- [ ] **Step 4: 实现前端页面、路由、菜单和联调**
- [ ] **Step 5: 运行 `./mvnw -Dtest=QualityGradeControllerTests test`、`pnpm lint`、`pnpm build`**
- [ ] **Step 6: 提交**

### Task 4: 保质期规则管理

**Files:**
- Create: `server/sql/2026-04-19-shelf-life-rule.sql`
- Create: `docs/api-design/shelf-life-rule-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/controller/ShelfLifeRuleController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/service/ShelfLifeRuleService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/repository/ShelfLifeRuleRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/dto/ShelfLifeRuleCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/dto/ShelfLifeRuleListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/dto/ShelfLifeRuleStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/dto/ShelfLifeRuleUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/entity/ShelfLifeRuleEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/vo/ShelfLifeRuleDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/vo/ShelfLifeRuleListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/shelfliferule/domain/vo/ShelfLifeRuleOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/shelfliferule/controller/ShelfLifeRuleControllerTests.java`
- Create: `frontend/src/features/shelfliferule/api.ts`
- Create: `frontend/src/features/shelfliferule/types.ts`
- Create: `frontend/src/pages/shelf-life-rule/ShelfLifeRulePage.tsx`
- Create: `frontend/src/pages/shelf-life-rule/ShelfLifeRulePage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] **Step 1: 写保质期规则模块测试和接口文档**
- [ ] **Step 2: 落库并写入示例数据**
- [ ] **Step 3: 实现后端 CRUD、状态切换、选项接口，并接分类/储存条件引用校验**
- [ ] **Step 4: 实现前端页面、路由、菜单和联调**
- [ ] **Step 5: 运行 `./mvnw -Dtest=ShelfLifeRuleControllerTests test`、`pnpm lint`、`pnpm build`**
- [ ] **Step 6: 提交**

### Task 5: 产品档案管理

**Files:**
- Create: `server/sql/2026-04-19-product-archive.sql`
- Create: `docs/api-design/product-archive-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/controller/ProductArchiveController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/service/ProductArchiveService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/repository/ProductArchiveRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/dto/ProductArchiveCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/dto/ProductArchiveListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/dto/ProductArchiveStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/dto/ProductArchiveUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/entity/ProductArchiveEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/vo/ProductArchiveDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/productarchive/domain/vo/ProductArchiveListItemResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/productarchive/controller/ProductArchiveControllerTests.java`
- Create: `frontend/src/features/productarchive/api.ts`
- Create: `frontend/src/features/productarchive/types.ts`
- Create: `frontend/src/pages/product-archive/ProductArchivePage.tsx`
- Create: `frontend/src/pages/product-archive/ProductArchivePage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`
- Modify: 已有主数据模块删除校验，增加被产品档案引用时的业务错误

- [ ] **Step 1: 写产品档案模块测试和接口文档**
- [ ] **Step 2: 落库并写入示例数据**
- [ ] **Step 3: 实现后端 CRUD、状态切换、完整引用回填和删除引用保护**
- [ ] **Step 4: 实现前端页面、路由、菜单和联调，整合分类、单位、产地、品质等级、储存条件、保质期规则选项**
- [ ] **Step 5: 运行 `./mvnw test`、`pnpm lint`、`pnpm build`**
- [ ] **Step 6: 提交**
