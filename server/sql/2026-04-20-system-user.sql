USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_code` VARCHAR(64) NOT NULL COMMENT '用户编号',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `display_name` VARCHAR(64) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(32) NOT NULL COMMENT '手机号',
  `role_code` VARCHAR(32) NOT NULL COMMENT '角色编码',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '负责仓库ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_code` (`user_code`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_role_status` (`role_code`, `status`),
  CONSTRAINT `fk_sys_user_warehouse`
    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

INSERT INTO `sys_user` (
  `user_code`,
  `username`,
  `password_hash`,
  `display_name`,
  `phone`,
  `role_code`,
  `warehouse_id`,
  `status`,
  `remarks`
)
SELECT
  'U-202604200001',
  'admin',
  '$2a$10$BdOdW0ddWawa3Pt4mPM2Mu7WzzYOhhYy/WlPHN63W7qecZitVbTMW',
  '系统管理员',
  '13800000000',
  'ADMIN',
  NULL,
  1,
  '系统初始化管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');

INSERT INTO `sys_user` (
  `user_code`,
  `username`,
  `password_hash`,
  `display_name`,
  `phone`,
  `role_code`,
  `warehouse_id`,
  `status`,
  `remarks`
)
SELECT
  'U-202604200002',
  'warehouse_admin',
  '$2a$10$LhYntb84KnGaqdpcmCybXOWEktXu7JFoS3lWCEP9mEwaFH/eiEz.a',
  '仓库管理员',
  '13800000001',
  'WAREHOUSE_ADMIN',
  1,
  1,
  '系统初始化仓库管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'warehouse_admin');

INSERT INTO `sys_user` (
  `user_code`,
  `username`,
  `password_hash`,
  `display_name`,
  `phone`,
  `role_code`,
  `warehouse_id`,
  `status`,
  `remarks`
)
SELECT
  'U-202604200003',
  'inventory_admin',
  '$2a$10$.YiNXqjF/zCr9.JhsepyqeYlpPWAfzPJzubUWAL6VXTFxL81H3UmC',
  '库存管理员',
  '13800000002',
  'INVENTORY_ADMIN',
  1,
  1,
  '系统初始化库存管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'inventory_admin');

INSERT INTO `sys_user` (
  `user_code`,
  `username`,
  `password_hash`,
  `display_name`,
  `phone`,
  `role_code`,
  `warehouse_id`,
  `status`,
  `remarks`
)
SELECT
  'U-202604200004',
  'quality_admin',
  '$2a$10$rtKm5vCwZ4MDyyOUpkAC1.tO8xk9lCmTPsGLXMGFYL2wFrlBgpzem',
  '质检管理员',
  '13800000003',
  'QUALITY_ADMIN',
  1,
  1,
  '系统初始化质检管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'quality_admin');
