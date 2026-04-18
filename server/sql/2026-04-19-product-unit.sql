USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `product_unit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '单位ID',
  `unit_code` VARCHAR(64) NOT NULL COMMENT '单位编号',
  `unit_name` VARCHAR(64) NOT NULL COMMENT '单位名称',
  `unit_symbol` VARCHAR(32) NOT NULL COMMENT '单位符号',
  `unit_type` VARCHAR(32) NOT NULL COMMENT '单位类型',
  `precision_digits` INT NOT NULL DEFAULT 0 COMMENT '精度位数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_unit_code` (`unit_code`),
  UNIQUE KEY `uk_product_unit_name` (`unit_name`),
  KEY `idx_product_unit_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品单位表';

INSERT INTO `product_unit` (
  `unit_code`,
  `unit_name`,
  `unit_symbol`,
  `unit_type`,
  `precision_digits`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'UNIT-202604190001',
  '千克',
  'kg',
  '重量',
  3,
  10,
  1,
  '适用于农产品重量计量'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_unit` WHERE `unit_code` = 'UNIT-202604190001'
);

INSERT INTO `product_unit` (
  `unit_code`,
  `unit_name`,
  `unit_symbol`,
  `unit_type`,
  `precision_digits`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'UNIT-202604190002',
  '箱',
  '箱',
  '包装',
  0,
  20,
  1,
  '适用于整箱计数'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_unit` WHERE `unit_code` = 'UNIT-202604190002'
);

INSERT INTO `product_unit` (
  `unit_code`,
  `unit_name`,
  `unit_symbol`,
  `unit_type`,
  `precision_digits`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'UNIT-202604190003',
  '件',
  '件',
  '数量',
  0,
  30,
  1,
  '适用于单件计数'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `product_unit` WHERE `unit_code` = 'UNIT-202604190003'
);
