USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `inbound_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库记录ID',
  `record_code` VARCHAR(64) NOT NULL COMMENT '记录编号',
  `inbound_order_id` BIGINT NOT NULL COMMENT '入库单ID',
  `putaway_task_id` BIGINT NOT NULL COMMENT '上架任务ID',
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '入库数量',
  `occurred_at` DATETIME NOT NULL COMMENT '入库时间',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_record_code` (`record_code`),
  KEY `idx_inbound_record_order` (`inbound_order_id`),
  KEY `idx_inbound_record_task` (`putaway_task_id`),
  KEY `idx_inbound_record_warehouse` (`warehouse_id`),
  KEY `idx_inbound_record_product` (`product_id`),
  CONSTRAINT `fk_inbound_record_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`),
  CONSTRAINT `fk_inbound_record_task` FOREIGN KEY (`putaway_task_id`) REFERENCES `putaway_task` (`id`),
  CONSTRAINT `fk_inbound_record_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`),
  CONSTRAINT `fk_inbound_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_inbound_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_inbound_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_inbound_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库记录表';

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
  'IN-202604190003',
  supplier.`id`,
  warehouse.`id`,
  '2026-04-20 11:00:00',
  '2026-04-20 10:30:00',
  1,
  36.000,
  3,
  '已完成入库示例'
FROM `supplier` supplier
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-202604190002'
WHERE supplier.`supplier_code` = 'SUP-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_order` WHERE `order_code` = 'IN-202604190003'
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
  36.000,
  1,
  '已完成入库示例明细'
FROM `inbound_order` inbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190003'
WHERE inbound_order.`order_code` = 'IN-202604190003'
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
  'PT-202604190002',
  inbound_order.`id`,
  inbound_order_item.`id`,
  inbound_order.`supplier_id`,
  inbound_order.`warehouse_id`,
  zone.`id`,
  location.`id`,
  inbound_order_item.`product_id`,
  inbound_order_item.`quantity`,
  3,
  '已完成上架示例',
  '2026-04-20 10:40:00'
FROM `inbound_order` inbound_order
JOIN `inbound_order_item` inbound_order_item ON inbound_order_item.`inbound_order_id` = inbound_order.`id`
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-202604190002'
JOIN `warehouse_location` location ON location.`location_code` = 'LOC-202604190002'
WHERE inbound_order.`order_code` = 'IN-202604190003'
  AND NOT EXISTS (
    SELECT 1 FROM `putaway_task` WHERE `task_code` = 'PT-202604190002'
  );

INSERT INTO `inbound_record` (
  `record_code`,
  `inbound_order_id`,
  `putaway_task_id`,
  `supplier_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `product_id`,
  `quantity`,
  `occurred_at`,
  `remarks`
)
SELECT
  'IR-202604190001',
  putaway_task.`inbound_order_id`,
  putaway_task.`id`,
  putaway_task.`supplier_id`,
  putaway_task.`warehouse_id`,
  putaway_task.`zone_id`,
  putaway_task.`location_id`,
  putaway_task.`product_id`,
  putaway_task.`quantity`,
  putaway_task.`completed_at`,
  '已完成入库记录示例'
FROM `putaway_task` putaway_task
WHERE putaway_task.`task_code` = 'PT-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_record` WHERE `record_code` = 'IR-202604190001'
  );

INSERT INTO `inventory_stock` (
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `quantity`
)
SELECT
  putaway_task.`product_id`,
  putaway_task.`warehouse_id`,
  putaway_task.`zone_id`,
  putaway_task.`location_id`,
  putaway_task.`quantity`
FROM `putaway_task` putaway_task
WHERE putaway_task.`task_code` = 'PT-202604190002'
  AND NOT EXISTS (
    SELECT 1
    FROM `inventory_stock`
    WHERE `product_id` = putaway_task.`product_id`
      AND `location_id` = putaway_task.`location_id`
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
  'INVTX-202604190001',
  'INBOUND',
  putaway_task.`product_id`,
  putaway_task.`warehouse_id`,
  putaway_task.`zone_id`,
  putaway_task.`location_id`,
  putaway_task.`quantity`,
  'PUTAWAY_TASK',
  putaway_task.`id`,
  putaway_task.`completed_at`,
  '已完成入库流水示例'
FROM `putaway_task` putaway_task
WHERE putaway_task.`task_code` = 'PT-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190001'
  );
