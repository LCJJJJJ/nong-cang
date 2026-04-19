USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` VARCHAR(64) NOT NULL COMMENT '供应商编号',
  `supplier_name` VARCHAR(128) NOT NULL COMMENT '供应商名称',
  `supplier_type` VARCHAR(32) NOT NULL COMMENT '供应商类型',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `region_name` VARCHAR(128) DEFAULT NULL COMMENT '所在地区',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  UNIQUE KEY `uk_supplier_name` (`supplier_name`),
  KEY `idx_supplier_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';

INSERT INTO `supplier` (
  `supplier_code`,
  `supplier_name`,
  `supplier_type`,
  `contact_name`,
  `contact_phone`,
  `region_name`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'SUP-202604190001',
  '寿光绿源农业合作社',
  '产地直供',
  '王菜农',
  '13800000201',
  '山东省寿光市',
  '蔬菜产业园1号',
  10,
  1,
  '叶菜类核心供应商'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `supplier` WHERE `supplier_code` = 'SUP-202604190001'
);

INSERT INTO `supplier` (
  `supplier_code`,
  `supplier_name`,
  `supplier_type`,
  `contact_name`,
  `contact_phone`,
  `region_name`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'SUP-202604190002',
  '云南丰鲜果业有限公司',
  '贸易商',
  '李果商',
  '13800000202',
  '云南省昆明市',
  '水果集散中心2号',
  20,
  1,
  '热带水果供应商'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `supplier` WHERE `supplier_code` = 'SUP-202604190002'
);
