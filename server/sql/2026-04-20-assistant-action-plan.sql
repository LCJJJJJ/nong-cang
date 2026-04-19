USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `assistant_action_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '动作计划ID',
  `action_code` VARCHAR(64) NOT NULL COMMENT '动作计划编号',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型',
  `action_type` VARCHAR(16) NOT NULL COMMENT '动作类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标资源ID',
  `target_label` VARCHAR(128) DEFAULT NULL COMMENT '目标资源名称',
  `fields_json` LONGTEXT DEFAULT NULL COMMENT '已解析字段JSON',
  `missing_fields_json` LONGTEXT DEFAULT NULL COMMENT '缺失字段JSON',
  `risk_level` VARCHAR(16) NOT NULL DEFAULT 'LOW' COMMENT '风险级别',
  `confirmation_mode` VARCHAR(32) NOT NULL DEFAULT 'CONFIRM_CARD' COMMENT '确认模式',
  `status` VARCHAR(16) NOT NULL COMMENT '状态：DRAFT READY CONFIRMED EXECUTED FAILED CANCELLED EXPIRED',
  `summary` VARCHAR(255) NOT NULL COMMENT '摘要说明',
  `error_code` VARCHAR(64) DEFAULT NULL COMMENT '失败错误码',
  `error_message` VARCHAR(255) DEFAULT NULL COMMENT '失败错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `executed_at` DATETIME DEFAULT NULL COMMENT '执行时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assistant_action_plan_code` (`action_code`),
  KEY `idx_assistant_action_plan_session` (`session_id`, `status`, `updated_at`),
  CONSTRAINT `fk_assistant_action_plan_session`
    FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能助手写操作动作计划表';
