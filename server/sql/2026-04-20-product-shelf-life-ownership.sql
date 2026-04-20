USE `nong-cang`;

ALTER TABLE `product_category`
  DROP COLUMN `shelf_life_days`;

ALTER TABLE `product_category`
  DROP COLUMN `warning_days`;

ALTER TABLE `inbound_record`
  ADD COLUMN `shelf_life_days_snapshot` INT NULL COMMENT '入库时保质期天数快照' AFTER `quantity`,
  ADD COLUMN `warning_days_snapshot` INT NULL COMMENT '入库时预警提前天数快照' AFTER `shelf_life_days_snapshot`,
  ADD COLUMN `expected_expire_at` DATETIME NULL COMMENT '预计到期时间' AFTER `warning_days_snapshot`;

UPDATE `inbound_record` ir
JOIN `product_archive` pa
  ON pa.`id` = ir.`product_id`
SET
  ir.`shelf_life_days_snapshot` = pa.`shelf_life_days`,
  ir.`warning_days_snapshot` = pa.`warning_days`,
  ir.`expected_expire_at` = DATE_ADD(ir.`occurred_at`, INTERVAL pa.`shelf_life_days` DAY)
WHERE ir.`shelf_life_days_snapshot` IS NULL
   OR ir.`warning_days_snapshot` IS NULL
   OR ir.`expected_expire_at` IS NULL;

CREATE TABLE IF NOT EXISTS `inventory_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存批次ID',
  `batch_code` VARCHAR(64) NOT NULL COMMENT '批次编号',
  `source_type` VARCHAR(32) NOT NULL COMMENT '来源类型',
  `source_id` BIGINT NOT NULL COMMENT '来源记录ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `base_occurred_at` DATETIME NOT NULL COMMENT '效期计算基准时间',
  `shelf_life_days_snapshot` INT NOT NULL COMMENT '保质期天数快照',
  `warning_days_snapshot` INT NOT NULL DEFAULT 0 COMMENT '预警提前天数快照',
  `warning_at` DATETIME NOT NULL COMMENT '预警触发时间',
  `expected_expire_at` DATETIME NOT NULL COMMENT '预计到期时间',
  `initial_quantity` DECIMAL(14,3) NOT NULL COMMENT '初始数量',
  `remaining_quantity` DECIMAL(14,3) NOT NULL COMMENT '剩余数量',
  `status` VARCHAR(16) NOT NULL COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_batch_code` (`batch_code`),
  UNIQUE KEY `uk_inventory_batch_source` (`source_type`, `source_id`),
  KEY `idx_inventory_batch_product_status_expire` (`product_id`, `status`, `expected_expire_at`),
  KEY `idx_inventory_batch_location_status` (`location_id`, `status`),
  CONSTRAINT `fk_inventory_batch_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`),
  CONSTRAINT `fk_inventory_batch_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_inventory_batch_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_inventory_batch_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存批次表';

CREATE TABLE IF NOT EXISTS `outbound_task_batch_allocation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '拣货批次分配ID',
  `outbound_task_id` BIGINT NOT NULL COMMENT '出库任务ID',
  `inventory_batch_id` BIGINT NOT NULL COMMENT '库存批次ID',
  `allocated_quantity` DECIMAL(14,3) NOT NULL COMMENT '分配数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_task_batch_allocation` (`outbound_task_id`, `inventory_batch_id`),
  CONSTRAINT `fk_outbound_task_batch_task` FOREIGN KEY (`outbound_task_id`) REFERENCES `outbound_task` (`id`),
  CONSTRAINT `fk_outbound_task_batch_inventory` FOREIGN KEY (`inventory_batch_id`) REFERENCES `inventory_batch` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库任务批次分配表';

CREATE TABLE IF NOT EXISTS `abnormal_stock_batch_lock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常库存批次锁定ID',
  `abnormal_stock_id` BIGINT NOT NULL COMMENT '异常库存ID',
  `inventory_batch_id` BIGINT NOT NULL COMMENT '库存批次ID',
  `locked_quantity` DECIMAL(14,3) NOT NULL COMMENT '锁定数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_abnormal_stock_batch_lock` (`abnormal_stock_id`, `inventory_batch_id`),
  CONSTRAINT `fk_abnormal_stock_batch_lock_abnormal` FOREIGN KEY (`abnormal_stock_id`) REFERENCES `abnormal_stock` (`id`),
  CONSTRAINT `fk_abnormal_stock_batch_lock_inventory` FOREIGN KEY (`inventory_batch_id`) REFERENCES `inventory_batch` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异常库存批次锁定表';

INSERT INTO `inventory_batch` (
  `batch_code`,
  `source_type`,
  `source_id`,
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `base_occurred_at`,
  `shelf_life_days_snapshot`,
  `warning_days_snapshot`,
  `warning_at`,
  `expected_expire_at`,
  `initial_quantity`,
  `remaining_quantity`,
  `status`
)
SELECT
  CONCAT('BATCH-MIG-', s.`id`),
  'MIGRATION_OPENING',
  s.`id`,
  s.`product_id`,
  s.`warehouse_id`,
  s.`zone_id`,
  s.`location_id`,
  COALESCE(s.`updated_at`, s.`created_at`),
  pa.`shelf_life_days`,
  pa.`warning_days`,
  DATE_ADD(
    COALESCE(s.`updated_at`, s.`created_at`),
    INTERVAL GREATEST(pa.`shelf_life_days` - pa.`warning_days`, 0) DAY
  ),
  DATE_ADD(COALESCE(s.`updated_at`, s.`created_at`), INTERVAL pa.`shelf_life_days` DAY),
  s.`quantity`,
  s.`quantity`,
  'ACTIVE'
FROM `inventory_stock` s
JOIN `product_archive` pa
  ON pa.`id` = s.`product_id`
WHERE s.`quantity` > 0
  AND NOT EXISTS (
    SELECT 1
    FROM `inventory_batch` ib
    WHERE ib.`source_type` = 'MIGRATION_OPENING'
      AND ib.`source_id` = s.`id`
  );
