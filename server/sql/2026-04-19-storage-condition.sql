USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `storage_condition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '储存条件ID',
  `condition_code` VARCHAR(64) NOT NULL COMMENT '条件编号',
  `condition_name` VARCHAR(128) NOT NULL COMMENT '条件名称',
  `storage_type` VARCHAR(64) NOT NULL COMMENT '储存类型',
  `temperature_min` DECIMAL(5,2) DEFAULT NULL COMMENT '最低温度',
  `temperature_max` DECIMAL(5,2) DEFAULT NULL COMMENT '最高温度',
  `humidity_min` DECIMAL(5,2) DEFAULT NULL COMMENT '最低湿度',
  `humidity_max` DECIMAL(5,2) DEFAULT NULL COMMENT '最高湿度',
  `light_requirement` VARCHAR(32) DEFAULT NULL COMMENT '避光要求',
  `ventilation_requirement` VARCHAR(32) DEFAULT NULL COMMENT '通风要求',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_storage_condition_code` (`condition_code`),
  UNIQUE KEY `uk_storage_condition_name` (`condition_name`),
  KEY `idx_storage_condition_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='储存条件表';

INSERT INTO `storage_condition` (
  `condition_code`,
  `condition_name`,
  `storage_type`,
  `temperature_min`,
  `temperature_max`,
  `humidity_min`,
  `humidity_max`,
  `light_requirement`,
  `ventilation_requirement`,
  `status`,
  `sort_order`,
  `remarks`
)
SELECT
  'SC-202604190001',
  '叶菜冷藏标准',
  '冷藏',
  2.00,
  8.00,
  75.00,
  90.00,
  '需避强光',
  '普通通风',
  1,
  10,
  '适用于叶菜、菠菜、生菜等鲜蔬'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `storage_condition` WHERE `condition_code` = 'SC-202604190001'
);

INSERT INTO `storage_condition` (
  `condition_code`,
  `condition_name`,
  `storage_type`,
  `temperature_min`,
  `temperature_max`,
  `humidity_min`,
  `humidity_max`,
  `light_requirement`,
  `ventilation_requirement`,
  `status`,
  `sort_order`,
  `remarks`
)
SELECT
  'SC-202604190002',
  '根茎阴凉干燥标准',
  '阴凉干燥',
  10.00,
  15.00,
  50.00,
  70.00,
  '无特殊要求',
  '普通通风',
  1,
  20,
  '适用于根茎类、薯类、洋葱等'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `storage_condition` WHERE `condition_code` = 'SC-202604190002'
);

INSERT INTO `storage_condition` (
  `condition_code`,
  `condition_name`,
  `storage_type`,
  `temperature_min`,
  `temperature_max`,
  `humidity_min`,
  `humidity_max`,
  `light_requirement`,
  `ventilation_requirement`,
  `status`,
  `sort_order`,
  `remarks`
)
SELECT
  'SC-202604190003',
  '水果冷藏标准',
  '冷藏',
  4.00,
  8.00,
  65.00,
  85.00,
  '避免直射阳光',
  '普通通风',
  1,
  30,
  '适用于柑橘类、时令水果'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `storage_condition` WHERE `condition_code` = 'SC-202604190003'
);
