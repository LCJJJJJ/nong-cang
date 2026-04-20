USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `alert_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警规则ID',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '规则编号',
  `rule_name` VARCHAR(64) NOT NULL COMMENT '规则名称',
  `alert_type` VARCHAR(64) NOT NULL COMMENT '预警类型',
  `severity` VARCHAR(16) NOT NULL COMMENT '严重级别',
  `threshold_value` DECIMAL(14,3) NOT NULL COMMENT '阈值',
  `threshold_unit` VARCHAR(16) NOT NULL COMMENT '阈值单位',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '规则说明',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警规则表';

CREATE TABLE IF NOT EXISTS `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
  `alert_code` VARCHAR(64) NOT NULL COMMENT '预警编号',
  `rule_id` BIGINT NOT NULL COMMENT '规则ID',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '规则编号',
  `alert_type` VARCHAR(64) NOT NULL COMMENT '预警类型',
  `severity` VARCHAR(16) NOT NULL COMMENT '严重级别',
  `source_type` VARCHAR(64) NOT NULL COMMENT '来源类型',
  `source_id` BIGINT NOT NULL COMMENT '来源ID',
  `source_code` VARCHAR(64) NOT NULL COMMENT '来源编号',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` VARCHAR(255) NOT NULL COMMENT '内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1活跃 2忽略 3已恢复',
  `occurred_at` DATETIME NOT NULL COMMENT '触发时间',
  `resolved_at` DATETIME DEFAULT NULL COMMENT '恢复时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_record_code` (`alert_code`),
  KEY `idx_alert_record_rule_status` (`rule_code`, `status`),
  KEY `idx_alert_record_source` (`source_type`, `source_id`),
  CONSTRAINT `fk_alert_record_rule` FOREIGN KEY (`rule_id`) REFERENCES `alert_rule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录表';

CREATE TABLE IF NOT EXISTS `message_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `notice_code` VARCHAR(64) NOT NULL COMMENT '消息编号',
  `alert_record_id` BIGINT DEFAULT NULL COMMENT '关联预警记录ID',
  `notice_type` VARCHAR(32) NOT NULL COMMENT '消息类型',
  `severity` VARCHAR(16) NOT NULL COMMENT '严重级别',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` VARCHAR(255) NOT NULL COMMENT '内容',
  `source_type` VARCHAR(64) NOT NULL COMMENT '来源类型',
  `source_id` BIGINT NOT NULL COMMENT '来源ID',
  `source_code` VARCHAR(64) NOT NULL COMMENT '来源编号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1未读 2已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_notice_code` (`notice_code`),
  UNIQUE KEY `uk_message_notice_alert` (`alert_record_id`),
  CONSTRAINT `fk_message_notice_alert` FOREIGN KEY (`alert_record_id`) REFERENCES `alert_record` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知表';

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-LOW-STOCK', '低库存预警', 'LOW_STOCK', 'MEDIUM', 50.000, 'QUANTITY', 1, '当可用库存小于等于阈值时触发', 10
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-LOW-STOCK');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-PUTAWAY-TIMEOUT', '待上架超时预警', 'PUTAWAY_TIMEOUT', 'MEDIUM', 4.000, 'HOUR', 1, '上架任务长时间未处理时触发', 20
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-PUTAWAY-TIMEOUT');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-OUTBOUND-PICK-TIMEOUT', '待拣货超时预警', 'OUTBOUND_PICK_TIMEOUT', 'MEDIUM', 4.000, 'HOUR', 1, '拣货任务长时间未拣货时触发', 30
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-OUTBOUND-PICK-TIMEOUT');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-OUTBOUND-SHIP-TIMEOUT', '待出库超时预警', 'OUTBOUND_SHIP_TIMEOUT', 'HIGH', 4.000, 'HOUR', 1, '拣货完成后长时间未出库时触发', 40
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-OUTBOUND-SHIP-TIMEOUT');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-ABNORMAL-STOCK-STAGNANT', '异常库存滞留预警', 'ABNORMAL_STOCK_STAGNANT', 'HIGH', 24.000, 'HOUR', 1, '异常库存长时间未处理时触发', 50
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-ABNORMAL-STOCK-STAGNANT');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-STOCKTAKING-CONFIRM-TIMEOUT', '待盘点确认超时预警', 'STOCKTAKING_CONFIRM_TIMEOUT', 'MEDIUM', 8.000, 'HOUR', 1, '盘点结果保存后长时间未确认时触发', 60
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-STOCKTAKING-CONFIRM-TIMEOUT');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-INBOUND-PENDING-INSPECTION', '待质检超时预警', 'INBOUND_PENDING_INSPECTION', 'MEDIUM', 8.000, 'HOUR', 1, '入库记录长时间未质检时触发', 70
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-INBOUND-PENDING-INSPECTION');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-NEAR-EXPIRY', '临期预警', 'NEAR_EXPIRY', 'MEDIUM', 0.000, 'DAY', 1, '按产品预警天数触发临期预警', 80
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-NEAR-EXPIRY');

INSERT INTO `alert_rule` (
  `rule_code`,
  `rule_name`,
  `alert_type`,
  `severity`,
  `threshold_value`,
  `threshold_unit`,
  `enabled`,
  `description`,
  `sort_order`
)
SELECT 'AR-EXPIRED', '过期预警', 'EXPIRED', 'HIGH', 0.000, 'DAY', 1, '批次到期后立即触发过期预警', 90
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'AR-EXPIRED');
