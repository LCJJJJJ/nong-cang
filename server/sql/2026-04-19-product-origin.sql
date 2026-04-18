USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `product_origin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产地ID',
  `origin_code` VARCHAR(64) NOT NULL COMMENT '产地编号',
  `origin_name` VARCHAR(128) NOT NULL COMMENT '产地名称',
  `country_name` VARCHAR(64) NOT NULL COMMENT '国家名称',
  `province_name` VARCHAR(64) NOT NULL COMMENT '省份名称',
  `city_name` VARCHAR(64) DEFAULT NULL COMMENT '城市名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_origin_code` (`origin_code`),
  UNIQUE KEY `uk_product_origin_name` (`origin_name`),
  KEY `idx_product_origin_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品产地表';

INSERT INTO `product_origin` (
  `origin_code`,
  `origin_name`,
  `country_name`,
  `province_name`,
  `city_name`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'ORI-202604190001',
  '山东寿光',
  '中国',
  '山东省',
  '潍坊市',
  10,
  1,
  '设施蔬菜核心产区'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_origin` WHERE `origin_code` = 'ORI-202604190001'
);

INSERT INTO `product_origin` (
  `origin_code`,
  `origin_name`,
  `country_name`,
  `province_name`,
  `city_name`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'ORI-202604190002',
  '云南西双版纳',
  '中国',
  '云南省',
  '西双版纳州',
  20,
  1,
  '热带水果代表产区'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_origin` WHERE `origin_code` = 'ORI-202604190002'
);

INSERT INTO `product_origin` (
  `origin_code`,
  `origin_name`,
  `country_name`,
  `province_name`,
  `city_name`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'ORI-202604190003',
  '广西武鸣',
  '中国',
  '广西壮族自治区',
  '南宁市',
  30,
  1,
  '柑橘类特色产区'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_origin` WHERE `origin_code` = 'ORI-202604190003'
);
