USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `shelf_life_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '保质期规则ID',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '规则编号',
  `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
  `category_id` BIGINT DEFAULT NULL COMMENT '适用分类ID',
  `storage_condition_id` BIGINT DEFAULT NULL COMMENT '适用储存条件ID',
  `shelf_life_days` INT NOT NULL COMMENT '保质期天数',
  `warning_days` INT NOT NULL DEFAULT 0 COMMENT '预警提前天数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shelf_life_rule_code` (`rule_code`),
  UNIQUE KEY `uk_shelf_life_rule_name` (`rule_name`),
  KEY `idx_shelf_life_rule_status_sort` (`status`, `sort_order`),
  KEY `idx_shelf_life_rule_category` (`category_id`),
  KEY `idx_shelf_life_rule_storage_condition` (`storage_condition_id`),
  CONSTRAINT `fk_shelf_life_rule_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`),
  CONSTRAINT `fk_shelf_life_rule_storage_condition` FOREIGN KEY (`storage_condition_id`) REFERENCES `storage_condition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保质期规则表';

INSERT INTO `shelf_life_rule` (
  `rule_code`,
  `rule_name`,
  `category_id`,
  `storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'RULE-202604190001',
  '叶菜冷藏保质期规则',
  category.`id`,
  storage_condition.`id`,
  5,
  1,
  10,
  1,
  '适用于叶菜类冷藏存放'
FROM `product_category` category
JOIN `storage_condition` storage_condition
  ON storage_condition.`condition_code` = 'SC-202604190001'
WHERE category.`category_code` = 'CAT-A01'
  AND NOT EXISTS (
    SELECT 1 FROM `shelf_life_rule` WHERE `rule_code` = 'RULE-202604190001'
  );

INSERT INTO `shelf_life_rule` (
  `rule_code`,
  `rule_name`,
  `category_id`,
  `storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'RULE-202604190002',
  '根茎阴凉干燥保质期规则',
  category.`id`,
  storage_condition.`id`,
  15,
  3,
  20,
  1,
  '适用于根茎类常规存放'
FROM `product_category` category
JOIN `storage_condition` storage_condition
  ON storage_condition.`condition_code` = 'SC-202604190002'
WHERE category.`category_code` = 'CAT-A02'
  AND NOT EXISTS (
    SELECT 1 FROM `shelf_life_rule` WHERE `rule_code` = 'RULE-202604190002'
  );

INSERT INTO `shelf_life_rule` (
  `rule_code`,
  `rule_name`,
  `category_id`,
  `storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'RULE-202604190003',
  '柑橘冷藏保质期规则',
  category.`id`,
  storage_condition.`id`,
  12,
  2,
  30,
  1,
  '适用于柑橘类冷藏存放'
FROM `product_category` category
JOIN `storage_condition` storage_condition
  ON storage_condition.`condition_code` = 'SC-202604190003'
WHERE category.`category_code` = 'CAT-B01'
  AND NOT EXISTS (
    SELECT 1 FROM `shelf_life_rule` WHERE `rule_code` = 'RULE-202604190003'
  );
