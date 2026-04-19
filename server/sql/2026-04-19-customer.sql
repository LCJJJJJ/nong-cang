USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `customer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_code` VARCHAR(64) NOT NULL COMMENT '客户编号',
  `customer_name` VARCHAR(128) NOT NULL COMMENT '客户名称',
  `customer_type` VARCHAR(32) NOT NULL COMMENT '客户类型',
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
  UNIQUE KEY `uk_customer_code` (`customer_code`),
  UNIQUE KEY `uk_customer_name` (`customer_name`),
  KEY `idx_customer_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户表';

INSERT INTO `customer` (
  `customer_code`,
  `customer_name`,
  `customer_type`,
  `contact_name`,
  `contact_phone`,
  `region_name`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'CUS-202604190001',
  '华北社区采购中心',
  '批发客户',
  '赵采购',
  '13800000401',
  '北京市朝阳区',
  '社区团购园区1号',
  10,
  1,
  '社区团购主要客户'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `customer` WHERE `customer_code` = 'CUS-202604190001'
);

INSERT INTO `customer` (
  `customer_code`,
  `customer_name`,
  `customer_type`,
  `contact_name`,
  `contact_phone`,
  `region_name`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'CUS-202604190002',
  '上海鲜享商超',
  '商超客户',
  '周经理',
  '13800000402',
  '上海市浦东新区',
  '商超配送中心2号',
  20,
  1,
  '连锁商超客户'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `customer` WHERE `customer_code` = 'CUS-202604190002'
);
