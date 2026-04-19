USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `warehouse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_code` VARCHAR(64) NOT NULL COMMENT '仓库编号',
  `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
  `warehouse_type` VARCHAR(32) NOT NULL COMMENT '仓库类型',
  `manager_name` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '仓库地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  UNIQUE KEY `uk_warehouse_name` (`warehouse_name`),
  KEY `idx_warehouse_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库信息表';

INSERT INTO `warehouse` (
  `warehouse_code`,
  `warehouse_name`,
  `warehouse_type`,
  `manager_name`,
  `contact_phone`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'WH-202604190001',
  '一号综合仓',
  '综合仓',
  '王仓管',
  '13800000011',
  '山东省寿光市农产品园区1号',
  10,
  1,
  '主要承接蔬菜类商品'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `warehouse` WHERE `warehouse_code` = 'WH-202604190001'
);

INSERT INTO `warehouse` (
  `warehouse_code`,
  `warehouse_name`,
  `warehouse_type`,
  `manager_name`,
  `contact_phone`,
  `address`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'WH-202604190002',
  '冷链中心仓',
  '冷藏仓',
  '李冷链',
  '13800000012',
  '山东省寿光市冷链物流园2号',
  20,
  1,
  '主要承接冷藏冷链商品'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `warehouse` WHERE `warehouse_code` = 'WH-202604190002'
);
