USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `assistant_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_code` VARCHAR(64) NOT NULL COMMENT '会话编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '会话标题',
  `route_path` VARCHAR(128) DEFAULT NULL COMMENT '最近一次页面路由',
  `route_title` VARCHAR(128) DEFAULT NULL COMMENT '最近一次页面标题',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0已归档',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assistant_session_code` (`session_code`),
  KEY `idx_assistant_session_user` (`user_id`, `status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能助手会话表';

CREATE TABLE IF NOT EXISTS `assistant_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `role` VARCHAR(16) NOT NULL COMMENT '消息角色：user assistant',
  `content` TEXT NOT NULL COMMENT '消息正文',
  `message_type` VARCHAR(32) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型',
  `metadata_json` LONGTEXT DEFAULT NULL COMMENT '结构化结果元数据',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_assistant_message_session` (`session_id`, `id`),
  CONSTRAINT `fk_assistant_message_session`
    FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能助手消息表';

CREATE TABLE IF NOT EXISTS `assistant_tool_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工具审计ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `message_id` BIGINT DEFAULT NULL COMMENT '触发该工具的用户消息ID',
  `tool_name` VARCHAR(64) NOT NULL COMMENT '工具名称',
  `tool_arguments_json` LONGTEXT DEFAULT NULL COMMENT '工具入参',
  `tool_result_json` LONGTEXT DEFAULT NULL COMMENT '工具结果摘要',
  `success` TINYINT NOT NULL DEFAULT 1 COMMENT '执行结果：1成功 0失败',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_assistant_tool_audit_session` (`session_id`, `created_at`),
  CONSTRAINT `fk_assistant_tool_audit_session`
    FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_assistant_tool_audit_message`
    FOREIGN KEY (`message_id`) REFERENCES `assistant_message` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能助手工具调用审计表';
