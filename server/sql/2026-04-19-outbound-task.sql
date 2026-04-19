USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `outbound_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '拣货出库任务ID',
  `task_code` VARCHAR(64) NOT NULL COMMENT '任务编号',
  `outbound_order_id` BIGINT NOT NULL COMMENT '来源出库单ID',
  `outbound_order_item_id` BIGINT NOT NULL COMMENT '来源出库单明细ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT DEFAULT NULL COMMENT '库区ID',
  `location_id` BIGINT DEFAULT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '出库数量',
  `status` TINYINT NOT NULL COMMENT '状态：1待分配 2待拣货 3待出库 4已完成 5已取消',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `picked_at` DATETIME DEFAULT NULL COMMENT '拣货完成时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '出库完成时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_task_code` (`task_code`),
  KEY `idx_outbound_task_order` (`outbound_order_id`),
  KEY `idx_outbound_task_order_item` (`outbound_order_item_id`),
  KEY `idx_outbound_task_warehouse` (`warehouse_id`),
  KEY `idx_outbound_task_location` (`location_id`),
  KEY `idx_outbound_task_product` (`product_id`),
  CONSTRAINT `fk_outbound_task_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`),
  CONSTRAINT `fk_outbound_task_order_item` FOREIGN KEY (`outbound_order_item_id`) REFERENCES `outbound_order_item` (`id`),
  CONSTRAINT `fk_outbound_task_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_outbound_task_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_outbound_task_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_outbound_task_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_outbound_task_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拣货出库任务表';

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
  'OUT-202604190002',
  customer.`id`,
  warehouse.`id`,
  '2026-04-21 14:00:00',
  NULL,
  1,
  12.000,
  2,
  '待拣货示例'
FROM `customer` customer
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-202604190002'
WHERE customer.`customer_code` = 'CUS-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_order` WHERE `order_code` = 'OUT-202604190002'
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
  12.000,
  1,
  '待拣货示例明细'
FROM `outbound_order` outbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190003'
WHERE outbound_order.`order_code` = 'OUT-202604190002'
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
  'OT-202604190001',
  outbound_order.`id`,
  outbound_order_item.`id`,
  outbound_order.`customer_id`,
  outbound_order.`warehouse_id`,
  NULL,
  NULL,
  outbound_order_item.`product_id`,
  outbound_order_item.`quantity`,
  1,
  '待分配拣货任务示例',
  NULL,
  NULL
FROM `outbound_order` outbound_order
JOIN `outbound_order_item` outbound_order_item ON outbound_order_item.`outbound_order_id` = outbound_order.`id`
WHERE outbound_order.`order_code` = 'OUT-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_task` WHERE `task_code` = 'OT-202604190001'
  );
