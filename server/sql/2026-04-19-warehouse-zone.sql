USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `warehouse_zone` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库区ID',
  `zone_code` VARCHAR(64) NOT NULL COMMENT '库区编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID',
  `zone_name` VARCHAR(128) NOT NULL COMMENT '库区名称',
  `zone_type` VARCHAR(32) NOT NULL COMMENT '库区类型',
  `temperature_min` DECIMAL(6,2) DEFAULT NULL COMMENT '最低温度',
  `temperature_max` DECIMAL(6,2) DEFAULT NULL COMMENT '最高温度',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_zone_code` (`zone_code`),
  UNIQUE KEY `uk_warehouse_zone_name` (`warehouse_id`, `zone_name`),
  KEY `idx_warehouse_zone_status_sort` (`status`, `sort_order`),
  KEY `idx_warehouse_zone_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_warehouse_zone_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库库区表';

INSERT INTO `warehouse_zone` (
  `zone_code`,
  `warehouse_id`,
  `zone_name`,
  `zone_type`,
  `temperature_min`,
  `temperature_max`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'ZONE-202604190001',
  warehouse.`id`,
  '蔬菜收货区',
  '收货区',
  NULL,
  NULL,
  10,
  1,
  '用于蔬菜收货和临时复核'
FROM `warehouse` warehouse
WHERE warehouse.`warehouse_code` = 'WH-202604190001'
  AND NOT EXISTS (
    SELECT 1 FROM `warehouse_zone` WHERE `zone_code` = 'ZONE-202604190001'
  );

INSERT INTO `warehouse_zone` (
  `zone_code`,
  `warehouse_id`,
  `zone_name`,
  `zone_type`,
  `temperature_min`,
  `temperature_max`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'ZONE-202604190002',
  warehouse.`id`,
  '冷藏存储区',
  '冷藏区',
  2.00,
  8.00,
  20,
  1,
  '用于叶菜和冷链水果存放'
FROM `warehouse` warehouse
WHERE warehouse.`warehouse_code` = 'WH-202604190002'
  AND NOT EXISTS (
    SELECT 1 FROM `warehouse_zone` WHERE `zone_code` = 'ZONE-202604190002'
  );
