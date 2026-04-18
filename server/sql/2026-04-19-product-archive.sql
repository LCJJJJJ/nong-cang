USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `product_archive` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品档案ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '产品编号',
  `product_name` VARCHAR(128) NOT NULL COMMENT '产品名称',
  `product_specification` VARCHAR(128) DEFAULT NULL COMMENT '产品规格',
  `category_id` BIGINT NOT NULL COMMENT '产品分类ID',
  `unit_id` BIGINT NOT NULL COMMENT '产品单位ID',
  `origin_id` BIGINT NOT NULL COMMENT '产地信息ID',
  `storage_condition_id` BIGINT NOT NULL COMMENT '储存条件ID',
  `shelf_life_rule_id` BIGINT NOT NULL COMMENT '保质期规则ID',
  `quality_grade_id` BIGINT NOT NULL COMMENT '品质等级ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_archive_code` (`product_code`),
  UNIQUE KEY `uk_product_archive_name` (`product_name`),
  KEY `idx_product_archive_status_sort` (`status`, `sort_order`),
  KEY `idx_product_archive_category` (`category_id`),
  KEY `idx_product_archive_unit` (`unit_id`),
  KEY `idx_product_archive_origin` (`origin_id`),
  KEY `idx_product_archive_storage_condition` (`storage_condition_id`),
  KEY `idx_product_archive_shelf_life_rule` (`shelf_life_rule_id`),
  KEY `idx_product_archive_quality_grade` (`quality_grade_id`),
  CONSTRAINT `fk_product_archive_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`),
  CONSTRAINT `fk_product_archive_unit` FOREIGN KEY (`unit_id`) REFERENCES `product_unit` (`id`),
  CONSTRAINT `fk_product_archive_origin` FOREIGN KEY (`origin_id`) REFERENCES `product_origin` (`id`),
  CONSTRAINT `fk_product_archive_storage_condition` FOREIGN KEY (`storage_condition_id`) REFERENCES `storage_condition` (`id`),
  CONSTRAINT `fk_product_archive_shelf_life_rule` FOREIGN KEY (`shelf_life_rule_id`) REFERENCES `shelf_life_rule` (`id`),
  CONSTRAINT `fk_product_archive_quality_grade` FOREIGN KEY (`quality_grade_id`) REFERENCES `quality_grade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品档案表';

INSERT INTO `product_archive` (
  `product_code`,
  `product_name`,
  `product_specification`,
  `category_id`,
  `unit_id`,
  `origin_id`,
  `storage_condition_id`,
  `shelf_life_rule_id`,
  `quality_grade_id`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'PROD-202604190001',
  '菠菜鲜菜',
  '500g/袋',
  category.`id`,
  unit.`id`,
  origin.`id`,
  storage_condition.`id`,
  shelf_life_rule.`id`,
  quality_grade.`id`,
  10,
  1,
  '叶菜类标准产品'
FROM `product_category` category
JOIN `product_unit` unit ON unit.`unit_code` = 'UNIT-202604190001'
JOIN `product_origin` origin ON origin.`origin_code` = 'ORI-202604190001'
JOIN `storage_condition` storage_condition ON storage_condition.`condition_code` = 'SC-202604190001'
JOIN `shelf_life_rule` shelf_life_rule ON shelf_life_rule.`rule_code` = 'RULE-202604190001'
JOIN `quality_grade` quality_grade ON quality_grade.`grade_code` = 'GRADE-202604190001'
WHERE category.`category_code` = 'CAT-A01-01'
  AND NOT EXISTS (
    SELECT 1 FROM `product_archive` WHERE `product_code` = 'PROD-202604190001'
  );

INSERT INTO `product_archive` (
  `product_code`,
  `product_name`,
  `product_specification`,
  `category_id`,
  `unit_id`,
  `origin_id`,
  `storage_condition_id`,
  `shelf_life_rule_id`,
  `quality_grade_id`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'PROD-202604190002',
  '根茎胡萝卜',
  '1kg/袋',
  category.`id`,
  unit.`id`,
  origin.`id`,
  storage_condition.`id`,
  shelf_life_rule.`id`,
  quality_grade.`id`,
  20,
  1,
  '根茎类标准产品'
FROM `product_category` category
JOIN `product_unit` unit ON unit.`unit_code` = 'UNIT-202604190001'
JOIN `product_origin` origin ON origin.`origin_code` = 'ORI-202604190001'
JOIN `storage_condition` storage_condition ON storage_condition.`condition_code` = 'SC-202604190002'
JOIN `shelf_life_rule` shelf_life_rule ON shelf_life_rule.`rule_code` = 'RULE-202604190002'
JOIN `quality_grade` quality_grade ON quality_grade.`grade_code` = 'GRADE-202604190002'
WHERE category.`category_code` = 'CAT-A02'
  AND NOT EXISTS (
    SELECT 1 FROM `product_archive` WHERE `product_code` = 'PROD-202604190002'
  );

INSERT INTO `product_archive` (
  `product_code`,
  `product_name`,
  `product_specification`,
  `category_id`,
  `unit_id`,
  `origin_id`,
  `storage_condition_id`,
  `shelf_life_rule_id`,
  `quality_grade_id`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'PROD-202604190003',
  '武鸣柑橘',
  '2kg/箱',
  category.`id`,
  unit.`id`,
  origin.`id`,
  storage_condition.`id`,
  shelf_life_rule.`id`,
  quality_grade.`id`,
  30,
  1,
  '水果类标准产品'
FROM `product_category` category
JOIN `product_unit` unit ON unit.`unit_code` = 'UNIT-202604190002'
JOIN `product_origin` origin ON origin.`origin_code` = 'ORI-202604190003'
JOIN `storage_condition` storage_condition ON storage_condition.`condition_code` = 'SC-202604190003'
JOIN `shelf_life_rule` shelf_life_rule ON shelf_life_rule.`rule_code` = 'RULE-202604190003'
JOIN `quality_grade` quality_grade ON quality_grade.`grade_code` = 'GRADE-202604190001'
WHERE category.`category_code` = 'CAT-B01'
  AND NOT EXISTS (
    SELECT 1 FROM `product_archive` WHERE `product_code` = 'PROD-202604190003'
  );
