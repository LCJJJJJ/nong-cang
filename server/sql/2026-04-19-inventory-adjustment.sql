USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `inventory_adjustment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存调整ID',
  `adjustment_code` VARCHAR(64) NOT NULL COMMENT '调整单编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `adjustment_type` VARCHAR(32) NOT NULL COMMENT '调整方向：INCREASE/DECREASE',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '调整数量',
  `reason` VARCHAR(64) NOT NULL COMMENT '调整原因',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_adjustment_code` (`adjustment_code`),
  KEY `idx_inventory_adjustment_warehouse` (`warehouse_id`),
  KEY `idx_inventory_adjustment_location` (`location_id`),
  KEY `idx_inventory_adjustment_product` (`product_id`),
  CONSTRAINT `fk_inventory_adjustment_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_inventory_adjustment_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_inventory_adjustment_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`),
  CONSTRAINT `fk_inventory_adjustment_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存调整表';

INSERT INTO `inventory_adjustment` (
  `adjustment_code`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `product_id`,
  `adjustment_type`,
  `quantity`,
  `reason`,
  `remarks`
)
SELECT
  'ADJ-202604190001',
  warehouse.`id`,
  zone.`id`,
  location.`id`,
  product_archive.`id`,
  'INCREASE',
  20.000,
  '系统初始化修正',
  '库存调整示例'
FROM `warehouse` warehouse
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-202604190001'
JOIN `warehouse_location` location ON location.`location_code` = 'LOC-202604190001'
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190001'
WHERE warehouse.`warehouse_code` = 'WH-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_adjustment` WHERE `adjustment_code` = 'ADJ-202604190001'
  );

INSERT INTO `inventory_stock` (
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `quantity`
)
SELECT
  product_archive.`id`,
  warehouse.`id`,
  zone.`id`,
  location.`id`,
  20.000
FROM `warehouse` warehouse
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-202604190001'
JOIN `warehouse_location` location ON location.`location_code` = 'LOC-202604190001'
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-202604190001'
WHERE warehouse.`warehouse_code` = 'WH-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190004'
  )
ON DUPLICATE KEY UPDATE
  `quantity` = `quantity` + VALUES(`quantity`);

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
  'INVTX-202604190004',
  'ADJUSTMENT',
  adjustment.`product_id`,
  adjustment.`warehouse_id`,
  adjustment.`zone_id`,
  adjustment.`location_id`,
  adjustment.`quantity`,
  'INVENTORY_ADJUSTMENT',
  adjustment.`id`,
  adjustment.`created_at`,
  adjustment.`remarks`
FROM `inventory_adjustment` adjustment
WHERE adjustment.`adjustment_code` = 'ADJ-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190004'
  );
