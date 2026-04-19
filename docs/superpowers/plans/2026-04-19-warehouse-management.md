# 仓库管理 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成仓库管理菜单下的仓库信息管理、库区管理、库位管理三类页面，实现从仓库到库区再到库位的完整闭环。

**Architecture:** 采用 `warehouse -> warehousezone -> warehouselocation` 的依赖顺序实现，后端按模块分层，前端按 `features/<feature>` 与 `pages/<feature>` 组织，并为各层提供选项接口供下游引用。所有删除操作增加层级引用校验，避免直接落到数据库外键异常。

**Tech Stack:** Spring Boot 3、JDK 17、NamedParameterJdbcTemplate、MySQL、React、TypeScript、Vite、Axios。

---

### Task 1: 仓库信息管理

**Files:**
- Create: `server/sql/2026-04-19-warehouse.sql`
- Create: `docs/api-design/warehouse-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/controller/WarehouseController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/service/WarehouseService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/repository/WarehouseRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/dto/WarehouseCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/dto/WarehouseListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/dto/WarehouseStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/dto/WarehouseUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/entity/WarehouseEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/vo/WarehouseDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/vo/WarehouseListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouse/domain/vo/WarehouseOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/warehouse/controller/WarehouseControllerTests.java`
- Create: `frontend/src/features/warehouse/api.ts`
- Create: `frontend/src/features/warehouse/types.ts`
- Create: `frontend/src/pages/warehouse/WarehousePage.tsx`
- Create: `frontend/src/pages/warehouse/WarehousePage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写仓库模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、状态切换、选项接口
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw -Dtest=WarehouseControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 2: 库区管理

**Files:**
- Create: `server/sql/2026-04-19-warehouse-zone.sql`
- Create: `docs/api-design/warehouse-zone-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/controller/WarehouseZoneController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/service/WarehouseZoneService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/repository/WarehouseZoneRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/dto/WarehouseZoneCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/dto/WarehouseZoneListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/dto/WarehouseZoneStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/dto/WarehouseZoneUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/entity/WarehouseZoneEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/vo/WarehouseZoneDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/vo/WarehouseZoneListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehousezone/domain/vo/WarehouseZoneOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/warehousezone/controller/WarehouseZoneControllerTests.java`
- Create: `frontend/src/features/warehousezone/api.ts`
- Create: `frontend/src/features/warehousezone/types.ts`
- Create: `frontend/src/pages/warehouse-zone/WarehouseZonePage.tsx`
- Create: `frontend/src/pages/warehouse-zone/WarehouseZonePage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写库区模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、状态切换、选项接口
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw -Dtest=WarehouseZoneControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 3: 库位管理

**Files:**
- Create: `server/sql/2026-04-19-warehouse-location.sql`
- Create: `docs/api-design/warehouse-location-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/controller/WarehouseLocationController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/service/WarehouseLocationService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/repository/WarehouseLocationRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/dto/WarehouseLocationCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/dto/WarehouseLocationListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/dto/WarehouseLocationStatusUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/dto/WarehouseLocationUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/entity/WarehouseLocationEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/vo/WarehouseLocationDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/vo/WarehouseLocationListItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/warehouselocation/domain/vo/WarehouseLocationOptionResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/warehouselocation/controller/WarehouseLocationControllerTests.java`
- Create: `frontend/src/features/warehouselocation/api.ts`
- Create: `frontend/src/features/warehouselocation/types.ts`
- Create: `frontend/src/pages/warehouse-location/WarehouseLocationPage.tsx`
- Create: `frontend/src/pages/warehouse-location/WarehouseLocationPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写库位模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、状态切换、选项接口与仓库/库区归属校验
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw test`、`pnpm lint`、`pnpm build`
- [ ] 提交
