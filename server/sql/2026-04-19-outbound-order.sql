USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `outbound_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `order_code` VARCHAR(64) NOT NULL COMMENT '出库单编号',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `expected_delivery_at` DATETIME NOT NULL COMMENT '预计发货时间',
  `actual_outbound_at` DATETIME DEFAULT NULL COMMENT '实际出库时间',
  `total_item_count` INT NOT NULL COMMENT '商品条数',
  `total_quantity` DECIMAL(14,3) NOT NULL COMMENT '总数量',
  `status` TINYINT NOT NULL COMMENT '状态：1待分配 2待拣货 3待出库 4已完成 5已取消',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_order_code` (`order_code`),
  KEY `idx_outbound_order_customer` (`customer_id`),
  KEY `idx_outbound_order_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_outbound_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_outbound_order_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单表';

CREATE TABLE IF NOT EXISTS `outbound_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单明细ID',
  `outbound_order_id` BIGINT NOT NULL COMMENT '所属出库单ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '出库数量',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_outbound_order_item_order` (`outbound_order_id`),
  KEY `idx_outbound_order_item_product` (`product_id`),
  CONSTRAINT `fk_outbound_order_item_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_outbound_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单明细表';

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
  'OUT-202604190001',
  customer.`id`,
  warehouse.`id`,
  '2026-04-21 09:30:00',
  NULL,
  1,
  18.000,
  1,
  '待分配出库示例'
FROM `customer` customer
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-202604190002'
WHERE customer.`customer_code` = 'CUS-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `outbound_order` WHERE `order_code` = 'OUT-202604190001'
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
  18.000,
  1,
  '待分配出库示例明细'
FROM `outbound_order` outbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190003'
WHERE outbound_order.`order_code` = 'OUT-202604190001'
  AND NOT EXISTS (
    SELECT 1
    FROM `outbound_order_item`
    WHERE `outbound_order_id` = outbound_order.`id`
      AND `product_id` = product_archive.`id`
  );
