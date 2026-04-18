USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `quality_grade` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '品质等级ID',
  `grade_code` VARCHAR(64) NOT NULL COMMENT '品质等级编号',
  `grade_name` VARCHAR(64) NOT NULL COMMENT '品质等级名称',
  `score_min` DECIMAL(5,2) DEFAULT NULL COMMENT '最低分值',
  `score_max` DECIMAL(5,2) DEFAULT NULL COMMENT '最高分值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quality_grade_code` (`grade_code`),
  UNIQUE KEY `uk_quality_grade_name` (`grade_name`),
  KEY `idx_quality_grade_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品质等级表';

INSERT INTO `quality_grade` (
  `grade_code`,
  `grade_name`,
  `score_min`,
  `score_max`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'GRADE-202604190001',
  '特级',
  90.00,
  100.00,
  10,
  1,
  '适用于高品质精选农产品'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `quality_grade` WHERE `grade_code` = 'GRADE-202604190001'
);

INSERT INTO `quality_grade` (
  `grade_code`,
  `grade_name`,
  `score_min`,
  `score_max`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'GRADE-202604190002',
  '一级',
  80.00,
  89.90,
  20,
  1,
  '适用于优选农产品'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `quality_grade` WHERE `grade_code` = 'GRADE-202604190002'
);

INSERT INTO `quality_grade` (
  `grade_code`,
  `grade_name`,
  `score_min`,
  `score_max`,
  `sort_order`,
  `status`,
  `remarks`
)
SELECT
  'GRADE-202604190003',
  '二级',
  70.00,
  79.90,
  30,
  1,
  '适用于标准农产品'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `quality_grade` WHERE `grade_code` = 'GRADE-202604190003'
);
