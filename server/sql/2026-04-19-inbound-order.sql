USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `inbound_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `order_code` VARCHAR(64) NOT NULL COMMENT '入库单编号',
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `expected_arrival_at` DATETIME NOT NULL COMMENT '预计到货时间',
  `actual_arrival_at` DATETIME DEFAULT NULL COMMENT '实际到货时间',
  `total_item_count` INT NOT NULL DEFAULT 0 COMMENT '商品条数',
  `total_quantity` DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT '总数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待到货，2待上架，3已完成，4已取消',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_order_code` (`order_code`),
  KEY `idx_inbound_order_supplier` (`supplier_id`),
  KEY `idx_inbound_order_warehouse` (`warehouse_id`),
  KEY `idx_inbound_order_status` (`status`),
  CONSTRAINT `fk_inbound_order_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`),
  CONSTRAINT `fk_inbound_order_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单表';

CREATE TABLE IF NOT EXISTS `inbound_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单明细ID',
  `inbound_order_id` BIGINT NOT NULL COMMENT '入库单ID',
  `product_id` BIGINT NOT NULL COMMENT '产品档案ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '入库数量',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_inbound_order_item_order` (`inbound_order_id`),
  KEY `idx_inbound_order_item_product` (`product_id`),
  CONSTRAINT `fk_inbound_order_item_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`),
  CONSTRAINT `fk_inbound_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单明细表';

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
  'IN-202604190001',
  supplier.`id`,
  warehouse.`id`,
  '2026-04-20 09:00:00',
  NULL,
  1,
  100.000,
  1,
  '叶菜类到货计划'
FROM `supplier` supplier
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-202604190001'
WHERE supplier.`supplier_code` = 'SUP-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_order` WHERE `order_code` = 'IN-202604190001'
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
  100.000,
  1,
  '首批到货'
FROM `inbound_order` inbound_order
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190001'
WHERE inbound_order.`order_code` = 'IN-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inbound_order_item`
    WHERE `inbound_order_id` = inbound_order.`id`
      AND `product_id` = product_archive.`id`
  );
