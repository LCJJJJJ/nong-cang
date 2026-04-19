USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `inventory_stocktaking_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `stocktaking_code` VARCHAR(64) NOT NULL COMMENT '盘点单编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT DEFAULT NULL COMMENT '库区ID，为空表示盘点整个仓库',
  `status` TINYINT NOT NULL COMMENT '状态：1待盘点 2待确认 3已完成 4已取消',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_stocktaking_code` (`stocktaking_code`),
  KEY `idx_inventory_stocktaking_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inventory_stocktaking_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_inventory_stocktaking_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存盘点单表';

CREATE TABLE IF NOT EXISTS `inventory_stocktaking_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点单明细ID',
  `stocktaking_order_id` BIGINT NOT NULL COMMENT '所属盘点单ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `system_quantity` DECIMAL(14,3) NOT NULL COMMENT '系统数量',
  `counted_quantity` DECIMAL(14,3) DEFAULT NULL COMMENT '实盘数量',
  `difference_quantity` DECIMAL(14,3) DEFAULT NULL COMMENT '差异数量',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_inventory_stocktaking_item_order` (`stocktaking_order_id`),
  KEY `idx_inventory_stocktaking_item_product` (`product_id`),
  KEY `idx_inventory_stocktaking_item_location` (`location_id`),
  CONSTRAINT `fk_inventory_stocktaking_item_order` FOREIGN KEY (`stocktaking_order_id`) REFERENCES `inventory_stocktaking_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inventory_stocktaking_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`),
  CONSTRAINT `fk_inventory_stocktaking_item_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_inventory_stocktaking_item_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_inventory_stocktaking_item_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存盘点单明细表';

INSERT INTO `inventory_stocktaking_order` (
  `stocktaking_code`,
  `warehouse_id`,
  `zone_id`,
  `status`,
  `remarks`
)
SELECT
  'STK-202604190001',
  warehouse.`id`,
  zone.`id`,
  3,
  '已完成盘点示例'
FROM `warehouse` warehouse
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-20260419094454480'
WHERE warehouse.`warehouse_code` = 'WH-20260419093735433'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_stocktaking_order` WHERE `stocktaking_code` = 'STK-202604190001'
  );

INSERT INTO `inventory_stocktaking_item` (
  `stocktaking_order_id`,
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `system_quantity`,
  `counted_quantity`,
  `difference_quantity`,
  `remarks`
)
SELECT
  stocktaking_order.`id`,
  product_archive.`id`,
  warehouse.`id`,
  zone.`id`,
  location.`id`,
  48.997,
  47.997,
  -1.000,
  '已完成盘点示例明细'
FROM `inventory_stocktaking_order` stocktaking_order
JOIN `warehouse` warehouse ON warehouse.`warehouse_code` = 'WH-20260419093735433'
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-20260419094454480'
JOIN `warehouse_location` location ON location.`location_code` = 'LOC-20260419100042806'
JOIN `product_archive` product_archive ON product_archive.`product_code` = 'PROD-20260419083640261'
WHERE stocktaking_order.`stocktaking_code` = 'STK-202604190001'
  AND NOT EXISTS (
    SELECT 1
    FROM `inventory_stocktaking_item`
    WHERE `stocktaking_order_id` = stocktaking_order.`id`
      AND `product_id` = product_archive.`id`
      AND `location_id` = location.`id`
  );

UPDATE `inventory_stock` stock
JOIN `product_archive` product_archive ON product_archive.`id` = stock.`product_id`
JOIN `warehouse_location` location ON location.`id` = stock.`location_id`
SET stock.`quantity` = 47.997
WHERE product_archive.`product_code` = 'PROD-20260419083640261'
  AND location.`location_code` = 'LOC-20260419100042806'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190005'
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
  'INVTX-202604190005',
  'STOCKTAKING',
  stocktaking_item.`product_id`,
  stocktaking_item.`warehouse_id`,
  stocktaking_item.`zone_id`,
  stocktaking_item.`location_id`,
  stocktaking_item.`difference_quantity`,
  'INVENTORY_STOCKTAKING',
  stocktaking_item.`id`,
  stocktaking_order.`updated_at`,
  stocktaking_item.`remarks`
FROM `inventory_stocktaking_item` stocktaking_item
JOIN `inventory_stocktaking_order` stocktaking_order ON stocktaking_order.`id` = stocktaking_item.`stocktaking_order_id`
WHERE stocktaking_order.`stocktaking_code` = 'STK-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `inventory_transaction` WHERE `transaction_code` = 'INVTX-202604190005'
  );
