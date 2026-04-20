# Product Shelf Life Ownership Design

**Date:** 2026-04-20

**Goal:** Make shelf life a strict product attribute managed only in product archive maintenance, remove it entirely from category management, and ensure product shelf life edits recalculate only the shelf-life state of inventory that is still in stock.

**Context**

- The current system stores shelf-life fields in both `product_category` and `product_archive`.
- The current warehouse model only has aggregated stock in `inventory_stock` and does not preserve batch-level shelf-life state.
- Inbound, outbound, quality inspection, abnormal stock, and loss flows currently operate on product and location quantities, not on inventory batches.
- The user confirmed four non-negotiable business rules:
  - Shelf life belongs only to the product.
  - Category shelf-life fields must be deleted completely.
  - Product shelf life can be edited after product creation.
  - A product shelf-life edit must affect inventory that is still in stock, but must not rewrite historical completed records.

**Chosen Approach**

- Keep `product_archive.shelf_life_days` and `product_archive.warning_days` as the only maintained shelf-life source.
- Remove `shelf_life_days` and `warning_days` from `product_category` across schema, backend, frontend, docs, tests, and assistant write actions.
- Introduce a batch-level inventory layer to carry shelf-life state for current stock:
  - `inventory_batch`
  - `outbound_task_batch_allocation`
  - `abnormal_stock_batch_lock`
- Preserve historical facts:
  - `inbound_record` keeps historical inbound facts and optional inbound-time shelf-life snapshot for display.
  - `outbound_record`, `loss_record`, and completed quality actions remain immutable.
- Recalculate only active inventory batches when a product shelf life changes.

**Why This Approach**

- It satisfies the ownership requirement without leaving hidden shelf-life defaults in categories.
- It gives the system a correct place to store "current in-stock shelf-life state" without mutating historical records.
- It fixes the current modeling gap where aggregated stock cannot answer which still-in-stock quantities should respond to a shelf-life change.
- It creates the foundation for near-expiry and expired alerts without forcing a larger product-versioning refactor.

**Business Rules**

- `shelf_life_days` and `warning_days` are maintained only through product archive create and update operations.
- Category create and update operations must not accept shelf-life fields.
- Product shelf-life edits recalculate only inventory batches where `remaining_quantity > 0`.
- Historical inbound, outbound, loss, and completed inspection records are not rewritten by later product shelf-life changes.
- Until the system has explicit production-date or harvest-date support, inbound completion time is the effectivity base for batch shelf-life calculation.
- Aggregated stock and batch stock must stay consistent:
  - For a given product and location, the sum of active batch `remaining_quantity` must equal aggregated `inventory_stock.quantity`.

**Data Model**

1. Product source of truth

- Keep on `product_archive`:
  - `shelf_life_days`
  - `warning_days`

2. Category cleanup

- Delete from `product_category`:
  - `shelf_life_days`
  - `warning_days`

3. New current-stock batch table

- Add `inventory_batch` with fields:
  - `id`
  - `batch_code`
  - `source_type`
  - `source_id`
  - `product_id`
  - `warehouse_id`
  - `zone_id`
  - `location_id`
  - `base_occurred_at`
  - `shelf_life_days_snapshot`
  - `warning_days_snapshot`
  - `warning_at`
  - `expected_expire_at`
  - `initial_quantity`
  - `remaining_quantity`
  - `status`
  - `created_at`
  - `updated_at`

Notes:

- `source_type` should allow at least `INBOUND_RECORD` and `MIGRATION_OPENING`.
- `base_occurred_at` is the timestamp used to derive current expiry dates.
- Snapshot fields exist to carry the effective rule applied to the batch at the current moment.

4. New outbound batch allocation table

- Add `outbound_task_batch_allocation` with fields:
  - `id`
  - `outbound_task_id`
  - `inventory_batch_id`
  - `allocated_quantity`
  - `created_at`

5. New abnormal stock batch lock table

- Add `abnormal_stock_batch_lock` with fields:
  - `id`
  - `abnormal_stock_id`
  - `inventory_batch_id`
  - `locked_quantity`
  - `created_at`

6. Optional inbound display snapshot

- Extend `inbound_record` with:
  - `shelf_life_days_snapshot`
  - `warning_days_snapshot`
  - `expected_expire_at`

These fields are for inbound-time display only. They are not rewritten when product shelf life later changes.

**Write Path Changes**

1. Product archive create

- Continue requiring `shelf_life_days` and `warning_days`.
- No category-derived shelf-life defaults remain.

2. Product archive update

- If shelf-life fields do not change, update behavior stays the same.
- If `shelf_life_days` or `warning_days` changes:
  - Update `product_archive`.
  - Load active `inventory_batch` rows for that product.
  - Recompute:
    - `shelf_life_days_snapshot`
    - `warning_days_snapshot`
    - `warning_at`
    - `expected_expire_at`
  - Do not rewrite historical records.

3. Inbound completion

- Keep writing aggregated stock into `inventory_stock`.
- Keep writing `inbound_record`.
- Also create one `inventory_batch` row for the inbound quantity.
- Derive expiry data from current product shelf-life values and inbound completion time.

4. Outbound assignment and completion

- UI can continue selecting a location instead of forcing manual batch choice.
- On the backend, allocate from eligible active batches in the chosen location using FEFO, earliest expiry first.
- Persist the allocation rows in `outbound_task_batch_allocation`.
- On completion:
  - decrease aggregated stock
  - decrease each allocated batch `remaining_quantity`
  - mark depleted batches as non-active

5. Quality inspection and abnormal stock

- For inbound-record inspections, use the source inbound batch directly.
- For stock inspections, resolve the inspected quantity against active batches in FEFO order within the selected location.
- Persist locked quantities in `abnormal_stock_batch_lock`.

6. Loss handling

- For abnormal-stock disposal, decrease the exact locked batches.
- For direct loss, consume active batches in FEFO order in the specified location.

**Read Path Changes**

1. Category management

- Tree, detail, create, and update payloads no longer contain shelf-life fields.
- UI removes shelf-life and warning-day inputs and columns.

2. Product archive management

- Remains the only shelf-life maintenance page.
- Page copy and API docs must explicitly state that shelf life is a product attribute.

3. Inventory queries

- Extend stock queries to expose current shelf-life state derived from active batches, such as:
  - nearest expiry time
  - minimum remaining days
  - near-expiry flag

4. Alerting

- Existing alert rules remain valid.
- Add near-expiry and expired alerts as a follow-up once active batch data is available.

**Affected Modules**

- Must change:
  - category backend module
  - category frontend page and types
  - product archive update flow
  - inbound record flow
  - inventory support flow
  - outbound task flow
  - abnormal stock flow
  - loss record flow
  - quality inspection flow
  - assistant basic-info write actions
  - API docs
  - SQL initialization and migrations
  - controller and service tests

- Mostly unaffected in business semantics:
  - product unit
  - product origin
  - quality grade
  - user and permission modules
  - warehouse master data

**Migration Strategy**

The current schema cannot reconstruct exact historical batch lineage for every still-in-stock quantity because only aggregated stock is persisted. Migration must therefore be explicit about this limitation.

1. Schema migration

- Add the new batch-related tables.
- Extend `inbound_record` with inbound-time shelf-life snapshot fields.
- Drop `product_category.shelf_life_days` and `product_category.warning_days`.

2. Current-stock backfill

- For each `inventory_stock` row where `quantity > 0`, create one `inventory_batch` row with:
  - `source_type = MIGRATION_OPENING`
  - `remaining_quantity = inventory_stock.quantity`
  - `initial_quantity = inventory_stock.quantity`
  - `base_occurred_at` chosen by:
    - most relevant inbound time for the same product and location if resolvable
    - otherwise `inventory_stock.created_at` or `inventory_stock.updated_at`
  - shelf-life snapshot values from current `product_archive`

3. Historical inbound snapshot backfill

- Populate new `inbound_record` snapshot fields from current `product_archive` values only for display continuity.
- Treat those values as historical inbound display data, not as active recalculation targets.

4. Rollout order

- Phase 1: add schema and backfill scripts
- Phase 2: write batches during inbound
- Phase 3: consume batches during outbound, abnormal stock, and loss
- Phase 4: recalculate active batches on product shelf-life edits
- Phase 5: expose batch-derived shelf-life state in inventory queries
- Phase 6: remove category shelf-life fields across all interfaces
- Phase 7: add near-expiry and expired alerts

**Consistency and Concurrency**

- Product shelf-life update and active-batch recalculation must run in one transaction.
- Outbound completion, abnormal locking, and loss deduction must update aggregated stock and batch stock in the same transaction.
- Batch rows involved in a stock mutation should be locked during mutation to avoid race conditions with concurrent stock movements or shelf-life recalculation.
- Add a consistency check job or SQL for:
  - aggregated stock quantity versus summed active batch remaining quantity

**Risks**

1. Historical precision gap

- Risk: current stock cannot be perfectly traced back to true historical batches.
- Mitigation: represent migrated stock as `MIGRATION_OPENING` batches and document that precision starts from migration cutover.

2. Batch and aggregate stock drift

- Risk: stock mutations update one layer but not the other.
- Mitigation: centralize all stock mutation paths in service-layer transactional logic and add verification tests plus reconciliation checks.

3. Partial rollout inconsistency

- Risk: inbound writes batches but outbound and loss flows still consume only aggregate stock.
- Mitigation: do not enable batch-based recalculation in production until outbound, abnormal-stock, and loss paths all consume batches.

4. Shelf-life recalculation cost

- Risk: a product update touches many active batches.
- Mitigation: update by `product_id` only, index active batch lookups, and keep alert refresh decoupled from batch fact updates.

**Testing**

- Category controller tests must verify shelf-life fields are absent from create, update, detail, and tree responses.
- Product archive tests must verify shelf-life changes trigger active-batch recalculation.
- Inbound tests must verify inbound completion creates both aggregate stock and batch rows.
- Outbound tests must verify FEFO allocation and batch quantity deduction.
- Loss and abnormal-stock tests must verify the correct batch deductions and locks.
- Inventory query tests must verify batch-derived current shelf-life indicators.
- Migration tests must verify `MIGRATION_OPENING` backfill and aggregate-versus-batch consistency.

**Implementation Notes**

- Use existing product archive maintenance as the only user-facing shelf-life entry point.
- Do not add manual batch maintenance UI in this iteration.
- Do not introduce product shelf-life version tables in this iteration.
- Production-date or harvest-date support can later replace `base_occurred_at` as the batch calculation anchor without changing the ownership model.

**Verification**

- Confirm category schema, API responses, frontend forms, assistant mappings, and tests no longer reference category shelf-life fields.
- Confirm product archive remains the only place where shelf life can be created or edited.
- Confirm active-batch recalculation changes current in-stock shelf-life state without mutating historical records.
- Confirm aggregate stock and active batch totals remain consistent through inbound, outbound, inspection, abnormal-stock, and loss flows.
