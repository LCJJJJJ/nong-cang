CREATE DATABASE IF NOT EXISTS `nong-cang`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` VARCHAR(64) NOT NULL COMMENT '分类编号',
  `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT NULL COMMENT '上级分类ID',
  `category_level` INT NOT NULL COMMENT '层级，从1开始',
  `ancestor_path` VARCHAR(512) NOT NULL DEFAULT '/' COMMENT '祖先路径，形如 /1/2/',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `default_storage_condition_id` BIGINT DEFAULT NULL COMMENT '默认储存条件ID',
  `shelf_life_days` INT DEFAULT NULL COMMENT '保质期基准天数',
  `warning_days` INT DEFAULT NULL COMMENT '预警提前天数',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_category_code` (`category_code`),
  KEY `idx_product_category_parent` (`parent_id`),
  KEY `idx_product_category_status_sort` (`status`, `sort_order`),
  KEY `idx_product_category_storage_condition` (`default_storage_condition_id`),
  CONSTRAINT `fk_product_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `product_category` (`id`),
  CONSTRAINT `fk_product_category_storage_condition` FOREIGN KEY (`default_storage_condition_id`) REFERENCES `storage_condition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农产品分类表';

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-A00',
  '新鲜蔬菜',
  NULL,
  1,
  '/',
  10,
  1,
  NULL,
  NULL,
  2,
  '蔬菜大类'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-A00'
);

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-A01',
  '叶菜类',
  parent.`id`,
  2,
  CONCAT(parent.`ancestor_path`, parent.`id`, '/'),
  1,
  1,
  (
    SELECT sc.`id`
    FROM `storage_condition` sc
    WHERE sc.`condition_name` = '叶菜冷藏标准'
    LIMIT 1
  ),
  5,
  1,
  '叶菜默认规则'
FROM `product_category` parent
WHERE parent.`category_code` = 'CAT-A00'
  AND NOT EXISTS (
    SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-A01'
  );

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-A01-01',
  '菠菜',
  parent.`id`,
  3,
  CONCAT(parent.`ancestor_path`, parent.`id`, '/'),
  1,
  1,
  (
    SELECT sc.`id`
    FROM `storage_condition` sc
    WHERE sc.`condition_name` = '叶菜冷藏标准'
    LIMIT 1
  ),
  4,
  1,
  '菠菜细分类'
FROM `product_category` parent
WHERE parent.`category_code` = 'CAT-A01'
  AND NOT EXISTS (
    SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-A01-01'
  );

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-A01-02',
  '生菜',
  parent.`id`,
  3,
  CONCAT(parent.`ancestor_path`, parent.`id`, '/'),
  2,
  1,
  (
    SELECT sc.`id`
    FROM `storage_condition` sc
    WHERE sc.`condition_name` = '叶菜冷藏标准'
    LIMIT 1
  ),
  3,
  1,
  '生菜细分类'
FROM `product_category` parent
WHERE parent.`category_code` = 'CAT-A01'
  AND NOT EXISTS (
    SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-A01-02'
  );

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-A02',
  '根茎类',
  parent.`id`,
  2,
  CONCAT(parent.`ancestor_path`, parent.`id`, '/'),
  2,
  0,
  (
    SELECT sc.`id`
    FROM `storage_condition` sc
    WHERE sc.`condition_name` = '根茎阴凉干燥标准'
    LIMIT 1
  ),
  15,
  3,
  '根茎类默认规则'
FROM `product_category` parent
WHERE parent.`category_code` = 'CAT-A00'
  AND NOT EXISTS (
    SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-A02'
  );

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-B00',
  '时令水果',
  NULL,
  1,
  '/',
  20,
  1,
  NULL,
  NULL,
  2,
  '水果大类'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-B00'
);

INSERT INTO `product_category` (
  `category_code`,
  `category_name`,
  `parent_id`,
  `category_level`,
  `ancestor_path`,
  `sort_order`,
  `status`,
  `default_storage_condition_id`,
  `shelf_life_days`,
  `warning_days`,
  `remarks`
)
SELECT
  'CAT-B01',
  '柑橘类',
  parent.`id`,
  2,
  CONCAT(parent.`ancestor_path`, parent.`id`, '/'),
  1,
  1,
  (
    SELECT sc.`id`
    FROM `storage_condition` sc
    WHERE sc.`condition_name` = '水果冷藏标准'
    LIMIT 1
  ),
  12,
  2,
  '柑橘类默认规则'
FROM `product_category` parent
WHERE parent.`category_code` = 'CAT-B00'
  AND NOT EXISTS (
    SELECT 1 FROM `product_category` WHERE `category_code` = 'CAT-B01'
  );
