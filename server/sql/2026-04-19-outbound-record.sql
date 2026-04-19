USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `outbound_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库记录ID',
  `record_code` VARCHAR(64) NOT NULL COMMENT '记录编号',
  `outbound_order_id` BIGINT NOT NULL COMMENT '出库单ID',
  `outbound_task_id` BIGINT NOT NULL COMMENT '拣货出库任务ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '出库数量',
  `occurred_at` DATETIME NOT NULL COMMENT '出库时间',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_record_code` (`record_code`),
  KEY `idx_outbound_record_order` (`outbound_order_id`),
  KEY `idx_outbound_record_task` (`outbound_task_id`),
  KEY `idx_outbound_record_warehouse` (`warehouse_id`),
  KEY `idx_outbound_record_product` (`product_id`),
  CONSTRAINT `fk_outbound_record_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`),
  CONSTRAINT `fk_outbound_record_task` FOREIGN KEY (`outbound_task_id`) REFERENCES `outbound_task` (`id`),
  CONSTRAINT `fk_outbound_record_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_outbound_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_outbound_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_outbound_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_outbound_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库记录表';

INSERT INTO `outbound_order` (
  `order_code`,
  `customer_id`,
  `warehouse_id`,
  `expected_delivery_at`,
  `actual_outbound_at`,
  `total_item_count`,
  `total_quantity`,
  `status`,
  `remarks`
)
SELECT
  'OUT-202604190003',
  customer.`id`,
  warehouse.`id`,
  '2026-04-21 16:00:00',
  '2026-04-21 15:40:00',
  1,
  5.000,
  4,
  '已完成出库示例'
FROM `customer` customer
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-20260419093735433'
WHERE customer.`customer_code` = 'CUS-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_order` WHERE `order_code` = 'OUT-202604190003'
  );

INSERT INTO `outbound_order_item` (
  `outbound_order_id`,
  `product_id`,
  `quantity`,
  `sort_order`,
  `remarks`
)
SELECT
  outbound_order.`id`,
  product_archive.`id`,
  5.000,
  1,
  '已完成出库示例明细'
FROM `outbound_order` outbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-20260419083640261'
WHERE outbound_order.`order_code` = 'OUT-202604190003'
  AND NOT EXISTS (
    SELECT 1
    FROM `outbound_order_item`
    WHERE `outbound_order_id` = outbound_order.`id`
      AND `product_id` = product_archive.`id`
  );

INSERT INTO `outbound_task` (
  `task_code`,
  `outbound_order_id`,
  `outbound_order_item_id`,
  `customer_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `product_id`,
  `quantity`,
  `status`,
  `remarks`,
  `picked_at`,
  `completed_at`
)
SELECT
  'OT-202604190002',
  outbound_order.`id`,
  outbound_order_item.`id`,
  outbound_order.`customer_id`,
  outbound_order.`warehouse_id`,
  zone.`id`,
  location.`id`,
  outbound_order_item.`product_id`,
  outbound_order_item.`quantity`,
  4,
  '已完成出库任务示例',
  '2026-04-21 15:20:00',
  '2026-04-21 15:40:00'
FROM `outbound_order` outbound_order
JOIN `outbound_order_item` outbound_order_item ON outbound_order_item.`outbound_order_id` = outbound_order.`id`
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-20260419094454480'
JOIN `warehouse_location` location ON location.`location_code` = 'LOC-20260419100042806'
WHERE outbound_order.`order_code` = 'OUT-202604190003'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_task` WHERE `task_code` = 'OT-202604190002'
  );

UPDATE `inventory_stock` stock
JOIN `product_archive` product_archive ON product_archive.`id` = stock.`product_id`
JOIN `warehouse_location` location ON location.`id` = stock.`location_id`
SET stock.`quantity` = stock.`quantity` - 5.000
WHERE product_archive.`product_code` = 'PROD-20260419083640261'
  AND location.`location_code` = 'LOC-20260419100042806'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_record` WHERE `record_code` = 'OR-202604190001'
  );

INSERT INTO `inventory_transaction` (
  `transaction_code`,
  `transaction_type`,
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `quantity`,
  `source_type`,
  `source_id`,
  `occurred_at`,
  `remarks`
)
SELECT
  'INVTX-202604190003',
  'OUTBOUND',
  outbound_task.`product_id`,
  outbound_task.`warehouse_id`,
  outbound_task.`zone_id`,
  outbound_task.`location_id`,
  -5.000,
  'OUTBOUND_TASK',
  outbound_task.`id`,
  outbound_task.`completed_at`,
  '已完成出库流水示例'
FROM `outbound_task` outbound_task
WHERE outbound_task.`task_code` = 'OT-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190003'
  );

INSERT INTO `outbound_record` (
  `record_code`,
  `outbound_order_id`,
  `outbound_task_id`,
  `customer_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `product_id`,
  `quantity`,
  `occurred_at`,
  `remarks`
)
SELECT
  'OR-202604190001',
  outbound_task.`outbound_order_id`,
  outbound_task.`id`,
  outbound_task.`customer_id`,
  outbound_task.`warehouse_id`,
  outbound_task.`zone_id`,
  outbound_task.`location_id`,
  outbound_task.`product_id`,
  outbound_task.`quantity`,
  outbound_task.`completed_at`,
  '已完成出库记录示例'
FROM `outbound_task` outbound_task
WHERE outbound_task.`task_code` = 'OT-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_record` WHERE `record_code` = 'OR-202604190001'
  );
