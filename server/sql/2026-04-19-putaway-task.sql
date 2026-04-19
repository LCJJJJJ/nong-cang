USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `putaway_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '上架任务ID',
  `task_code` VARCHAR(64) NOT NULL COMMENT '任务编号',
  `inbound_order_id` BIGINT NOT NULL COMMENT '来源入库单ID',
  `inbound_order_item_id` BIGINT NOT NULL COMMENT '来源入库单明细ID',
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT DEFAULT NULL COMMENT '库区ID',
  `location_id` BIGINT DEFAULT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '待上架数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待分配，2待上架，3已完成，4已取消',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_putaway_task_code` (`task_code`),
  KEY `idx_putaway_task_status` (`status`),
  KEY `idx_putaway_task_order` (`inbound_order_id`),
  KEY `idx_putaway_task_item` (`inbound_order_item_id`),
  KEY `idx_putaway_task_location` (`location_id`),
  CONSTRAINT `fk_putaway_task_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`),
  CONSTRAINT `fk_putaway_task_item` FOREIGN KEY (`inbound_order_item_id`) REFERENCES `inbound_order_item` (`id`),
  CONSTRAINT `fk_putaway_task_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`),
  CONSTRAINT `fk_putaway_task_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_putaway_task_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_putaway_task_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_putaway_task_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上架任务表';

CREATE TABLE IF NOT EXISTS `inventory_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存快照ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `quantity` DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '当前库存数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_stock_location_product` (`product_id`, `location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存快照表';

CREATE TABLE IF NOT EXISTS `inventory_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  `transaction_code` VARCHAR(64) NOT NULL COMMENT '流水编号',
  `transaction_type` VARCHAR(32) NOT NULL COMMENT '流水类型',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '数量变化',
  `source_type` VARCHAR(32) NOT NULL COMMENT '来源类型',
  `source_id` BIGINT NOT NULL COMMENT '来源ID',
  `occurred_at` DATETIME NOT NULL COMMENT '发生时间',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_transaction_code` (`transaction_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水表';

INSERT INTO `inbound_order` (
  `order_code`,
  `supplier_id`,
  `warehouse_id`,
  `expected_arrival_at`,
  `actual_arrival_at`,
  `total_item_count`,
  `total_quantity`,
  `status`,
  `remarks`
)
SELECT
  'IN-202604190002',
  supplier.`id`,
  warehouse.`id`,
  '2026-04-20 10:00:00',
  '2026-04-20 09:30:00',
  1,
  48.000,
  2,
  '待上架示例单'
FROM `supplier` supplier
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-202604190002'
WHERE supplier.`supplier_code` = 'SUP-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_order` WHERE `order_code` = 'IN-202604190002'
  );

INSERT INTO `inbound_order_item` (
  `inbound_order_id`,
  `product_id`,
  `quantity`,
  `sort_order`,
  `remarks`
)
SELECT
  inbound_order.`id`,
  product_archive.`id`,
  48.000,
  1,
  '待上架示例明细'
FROM `inbound_order` inbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190003'
WHERE inbound_order.`order_code` = 'IN-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_order_item`
    WHERE `inbound_order_id` = inbound_order.`id`
      AND `product_id` = product_archive.`id`
  );

INSERT INTO `putaway_task` (
  `task_code`,
  `inbound_order_id`,
  `inbound_order_item_id`,
  `supplier_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `product_id`,
  `quantity`,
  `status`,
  `remarks`,
  `completed_at`
)
SELECT
  'PT-202604190001',
  inbound_order.`id`,
  inbound_order_item.`id`,
  inbound_order.`supplier_id`,
  inbound_order.`warehouse_id`,
  NULL,
  NULL,
  inbound_order_item.`product_id`,
  inbound_order_item.`quantity`,
  1,
  '待分配库位',
  NULL
FROM `inbound_order` inbound_order
JOIN `inbound_order_item` inbound_order_item ON inbound_order_item.`inbound_order_id` = inbound_order.`id`
WHERE inbound_order.`order_code` = 'IN-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `putaway_task` WHERE `task_code` = 'PT-202604190001'
  );
