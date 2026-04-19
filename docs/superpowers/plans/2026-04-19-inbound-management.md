# 入库管理 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成入库单管理、上架任务管理、入库记录查询三类页面的数据库、接口、前端联调与测试，实现入库管理菜单完整闭环。

**Architecture:** 采用 `inboundorder -> putawaytask -> inboundrecord` 的依赖顺序实现，并在上架完成时同步写入入库记录和库存支持表。后端按模块分层，前端按 `features/<feature>` 与 `pages/<feature>` 组织，所有接口遵循统一响应和错误处理规范。

**Tech Stack:** Spring Boot 3、JDK 17、NamedParameterJdbcTemplate、MySQL、React、TypeScript、Vite、Axios。

---

### Task 1: 入库单管理

**Files:**
- Create: `server/sql/2026-04-19-inbound-order.sql`
- Create: `docs/api-design/inbound-order-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/controller/InboundOrderController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/service/InboundOrderService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/repository/InboundOrderRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/dto/InboundOrderCreateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/dto/InboundOrderItemRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/dto/InboundOrderListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/dto/InboundOrderUpdateRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/entity/InboundOrderEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/entity/InboundOrderItemEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/vo/InboundOrderDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/vo/InboundOrderItemResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundorder/domain/vo/InboundOrderListItemResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/inboundorder/controller/InboundOrderControllerTests.java`
- Modify: `server/src/main/java/com/nongcang/server/modules/productarchive/**` 增加产品选项接口
- Create: `frontend/src/features/inboundorder/api.ts`
- Create: `frontend/src/features/inboundorder/types.ts`
- Create: `frontend/src/pages/inbound-order/InboundOrderPage.tsx`
- Create: `frontend/src/pages/inbound-order/InboundOrderPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写入库单模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、新增、编辑、到货确认、取消
- [ ] 增加产品档案选项接口
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw -Dtest=InboundOrderControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 2: 上架任务管理

**Files:**
- Create: `server/sql/2026-04-19-putaway-task.sql`
- Create: `docs/api-design/putaway-task-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/controller/PutawayTaskController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/service/PutawayTaskService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/repository/PutawayTaskRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/domain/dto/PutawayAssignRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/domain/dto/PutawayTaskListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/domain/entity/PutawayTaskEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/domain/vo/PutawayTaskDetailResponse.java`
- Create: `server/src/main/java/com/nongcang/server/modules/putawaytask/domain/vo/PutawayTaskListItemResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/putawaytask/controller/PutawayTaskControllerTests.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inventorysupport/repository/InventoryStockRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inventorysupport/repository/InventoryTransactionRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inventorysupport/service/InventoryStockService.java`
- Create: `frontend/src/features/putawaytask/api.ts`
- Create: `frontend/src/features/putawaytask/types.ts`
- Create: `frontend/src/pages/putaway-task/PutawayTaskPage.tsx`
- Create: `frontend/src/pages/putaway-task/PutawayTaskPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写上架任务模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表、详情、分配库位、完成上架、取消任务
- [ ] 实现库存支持表写入逻辑
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw -Dtest=PutawayTaskControllerTests test`、`pnpm lint`、`pnpm build`
- [ ] 提交

### Task 3: 入库记录查询

**Files:**
- Create: `server/sql/2026-04-19-inbound-record.sql`
- Create: `docs/api-design/inbound-record-management.md`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/controller/InboundRecordController.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/service/InboundRecordService.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/repository/InboundRecordRepository.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/domain/dto/InboundRecordListQueryRequest.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/domain/entity/InboundRecordEntity.java`
- Create: `server/src/main/java/com/nongcang/server/modules/inboundrecord/domain/vo/InboundRecordListItemResponse.java`
- Create: `server/src/test/java/com/nongcang/server/modules/inboundrecord/controller/InboundRecordControllerTests.java`
- Modify: `server/src/main/java/com/nongcang/server/modules/putawaytask/service/PutawayTaskService.java`
- Create: `frontend/src/features/inboundrecord/api.ts`
- Create: `frontend/src/features/inboundrecord/types.ts`
- Create: `frontend/src/pages/inbound-record/InboundRecordPage.tsx`
- Create: `frontend/src/pages/inbound-record/InboundRecordPage.css`
- Modify: `frontend/src/app/App.tsx`
- Modify: `frontend/src/layouts/main/MainLayout.tsx`

- [ ] 写入库记录模块测试
- [ ] 落库并写入示例数据
- [ ] 实现后端列表查询，并在上架完成时写入入库记录
- [ ] 实现前端页面、路由、菜单与联调
- [ ] 运行 `./mvnw test`、`pnpm lint`、`pnpm build`
- [ ] 提交
