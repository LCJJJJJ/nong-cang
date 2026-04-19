USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `warehouse_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库位ID',
  `location_code` VARCHAR(64) NOT NULL COMMENT '库位编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '所属库区ID',
  `location_name` VARCHAR(128) NOT NULL COMMENT '库位名称',
  `location_type` VARCHAR(32) NOT NULL COMMENT '库位类型',
  `capacity` INT DEFAULT NULL COMMENT '容量上限',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_location_code` (`location_code`),
  UNIQUE KEY `uk_warehouse_location_name` (`zone_id`, `location_name`),
  KEY `idx_warehouse_location_status_sort` (`status`, `sort_order`),
  KEY `idx_warehouse_location_warehouse` (`warehouse_id`),
  KEY `idx_warehouse_location_zone` (`zone_id`),
  CONSTRAINT `fk_warehouse_location_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_warehouse_location_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库库位表';

INSERT INTO `warehouse_location` (
  `location_code`,
  `warehouse_id`,
  `zone_id`,
  `location_name`,
  `location_type`,
  `capacity`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'LOC-202604190001',
  warehouse.`id`,
  zone.`id`,
  'A-01-01',
  '货架位',
  50,
  10,
  1,
  '蔬菜收货暂存位'
FROM `warehouse` warehouse
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-202604190001'
WHERE warehouse.`warehouse_code` = 'WH-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `warehouse_location` WHERE `location_code` = 'LOC-202604190001'
  );

INSERT INTO `warehouse_location` (
  `location_code`,
  `warehouse_id`,
  `zone_id`,
  `location_name`,
  `location_type`,
  `capacity`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'LOC-202604190002',
  warehouse.`id`,
  zone.`id`,
  'B-02-01',
  '托盘位',
  24,
  20,
  1,
  '冷藏商品托盘位'
FROM `warehouse` warehouse
JOIN `warehouse_zone` zone ON zone.`zone_code` = 'ZONE-202604190002'
WHERE warehouse.`warehouse_code` = 'WH-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `warehouse_location` WHERE `location_code` = 'LOC-202604190002'
  );
