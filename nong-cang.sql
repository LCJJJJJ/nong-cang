/*
 Navicat Premium Dump SQL

 Source Server         : tencent
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 101.34.73.192:2017
 Source Schema         : nong-cang

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 20/04/2026 19:53:48
*/
USE `nong-cang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for abnormal_stock
-- ----------------------------
DROP TABLE IF EXISTS `abnormal_stock`;
CREATE TABLE `abnormal_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异常库存ID',
  `abnormal_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常库存编号',
  `quality_inspection_id` bigint NOT NULL COMMENT '来源质检单ID',
  `inspection_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源质检单编号',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `locked_quantity` decimal(14, 3) NOT NULL COMMENT '锁定数量',
  `status` tinyint NOT NULL COMMENT '状态：1锁定中 2已释放 3已转损耗',
  `reason` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常原因',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `processed_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_abnormal_stock_code`(`abnormal_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_abnormal_stock_inspection`(`quality_inspection_id` ASC) USING BTREE,
  INDEX `idx_abnormal_stock_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_abnormal_stock_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_abnormal_stock_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_abnormal_stock_location`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_abnormal_stock_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_abnormal_stock_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_abnormal_stock_quality_inspection` FOREIGN KEY (`quality_inspection_id`) REFERENCES `quality_inspection` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_abnormal_stock_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_abnormal_stock_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常锁定库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of abnormal_stock
-- ----------------------------

-- ----------------------------
-- Table structure for abnormal_stock_batch_lock
-- ----------------------------
DROP TABLE IF EXISTS `abnormal_stock_batch_lock`;
CREATE TABLE `abnormal_stock_batch_lock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '异常库存批次锁定ID',
  `abnormal_stock_id` bigint NOT NULL COMMENT '异常库存ID',
  `inventory_batch_id` bigint NOT NULL COMMENT '库存批次ID',
  `locked_quantity` decimal(14, 3) NOT NULL COMMENT '锁定数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_abnormal_stock_batch_lock`(`abnormal_stock_id` ASC, `inventory_batch_id` ASC) USING BTREE,
  INDEX `fk_abnormal_stock_batch_lock_inventory`(`inventory_batch_id` ASC) USING BTREE,
  CONSTRAINT `fk_abnormal_stock_batch_lock_abnormal` FOREIGN KEY (`abnormal_stock_id`) REFERENCES `abnormal_stock` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_abnormal_stock_batch_lock_inventory` FOREIGN KEY (`inventory_batch_id`) REFERENCES `inventory_batch` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常库存批次锁定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of abnormal_stock_batch_lock
-- ----------------------------

-- ----------------------------
-- Table structure for alert_record
-- ----------------------------
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
  `alert_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预警编号',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则编号',
  `alert_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预警类型',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '严重级别',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源ID',
  `source_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源编号',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1活跃 2忽略 3已恢复',
  `occurred_at` datetime NOT NULL COMMENT '触发时间',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '恢复时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_alert_record_code`(`alert_code` ASC) USING BTREE,
  INDEX `idx_alert_record_rule_status`(`rule_code` ASC, `status` ASC) USING BTREE,
  INDEX `idx_alert_record_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `fk_alert_record_rule`(`rule_id` ASC) USING BTREE,
  CONSTRAINT `fk_alert_record_rule` FOREIGN KEY (`rule_id`) REFERENCES `alert_rule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 159 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of alert_record
-- ----------------------------
INSERT INTO `alert_record` VALUES (129, 'ALERT-20260420030614909', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 29, 'STOCK-29', '菠菜鲜菜 低库存预警', '菠菜鲜菜 在 一号综合仓/A-01-01 的可用库存为 18.000，低于阈值 50.000', 3, '2026-04-19 06:59:50', '2026-04-20 05:18:24', '2026-04-19 19:06:14', '2026-04-19 21:18:24');
INSERT INTO `alert_record` VALUES (130, 'ALERT-20260420030614912', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 9, 'STOCK-9', '内酯豆腐 低库存预警', '内酯豆腐 在 三里庵一号仓/冷藏库区1-1 的可用库存为 45.000，低于阈值 50.000', 3, '2026-04-19 06:59:50', '2026-04-20 05:18:24', '2026-04-19 19:06:14', '2026-04-19 21:18:24');
INSERT INTO `alert_record` VALUES (131, 'ALERT-20260420030614914', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 4, 'STOCK-4', '武鸣柑橘 低库存预警', '武鸣柑橘 在 冷链中心仓/B-02-01 的可用库存为 36.000，低于阈值 50.000', 3, '2026-04-19 03:56:42', '2026-04-20 05:18:24', '2026-04-19 19:06:14', '2026-04-19 21:18:24');
INSERT INTO `alert_record` VALUES (132, 'ALERT-20260420030614923', 2, 'AR-PUTAWAY-TIMEOUT', 'PUTAWAY_TIMEOUT', 'MEDIUM', 'PUTAWAY_TASK', 1, 'PT-202604190001', 'PT-202604190001 待上架超时', '上架任务 PT-202604190001 已超过 4.000 小时未完成上架', 2, '2026-04-19 03:26:09', NULL, '2026-04-19 19:06:14', '2026-04-20 06:39:04');
INSERT INTO `alert_record` VALUES (133, 'ALERT-20260420030614943', 7, 'AR-INBOUND-PENDING-INSPECTION', 'INBOUND_PENDING_INSPECTION', 'MEDIUM', 'INBOUND_RECORD', 6, 'IR-20260419121132300', 'IR-20260419121132300 待质检超时', '入库记录 IR-20260419121132300 已超过 8.000 小时未完成质检', 2, '2026-04-19 12:11:32', NULL, '2026-04-19 19:06:14', '2026-04-20 06:39:03');
INSERT INTO `alert_record` VALUES (145, 'ALERT-20260420143556385', 7, 'AR-INBOUND-PENDING-INSPECTION', 'INBOUND_PENDING_INSPECTION', 'MEDIUM', 'INBOUND_RECORD', 26, 'IR-20260420051715735', 'IR-20260420051715735 待质检超时', '入库记录 IR-20260420051715735 已超过 8.000 小时未完成质检', 2, '2026-04-20 05:17:16', NULL, '2026-04-20 06:35:56', '2026-04-20 06:39:02');
INSERT INTO `alert_record` VALUES (150, 'ALERT-20260420152740135', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 85, 'STOCK-85', '猪肋排条 低库存预警', '猪肋排条 在 三里庵一号仓/冷藏库区1-1 的可用库存为 10.000，低于阈值 50.000', 2, '2026-04-20 07:24:42', NULL, '2026-04-20 07:27:40', '2026-04-20 07:27:49');
INSERT INTO `alert_record` VALUES (152, 'ALERT-20260420152740140', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 29, 'STOCK-29', '菠菜鲜菜 低库存预警', '菠菜鲜菜 在 一号综合仓/A-01-01 的可用库存为 18.000，低于阈值 50.000', 2, '2026-04-19 06:59:50', NULL, '2026-04-20 07:27:40', '2026-04-20 07:27:56');
INSERT INTO `alert_record` VALUES (153, 'ALERT-20260420152740142', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 9, 'STOCK-9', '内酯豆腐 低库存预警', '内酯豆腐 在 三里庵一号仓/冷藏库区1-1 的可用库存为 45.000，低于阈值 50.000', 2, '2026-04-19 06:59:50', NULL, '2026-04-20 07:27:40', '2026-04-20 07:27:53');
INSERT INTO `alert_record` VALUES (154, 'ALERT-20260420152740145', 1, 'AR-LOW-STOCK', 'LOW_STOCK', 'MEDIUM', 'INVENTORY_STOCK', 4, 'STOCK-4', '武鸣柑橘 低库存预警', '武鸣柑橘 在 冷链中心仓/B-02-01 的可用库存为 36.000，低于阈值 50.000', 2, '2026-04-19 03:56:42', NULL, '2026-04-20 07:27:40', '2026-04-20 07:27:55');
INSERT INTO `alert_record` VALUES (157, 'ALERT-20260420155639040', 2, 'AR-PUTAWAY-TIMEOUT', 'PUTAWAY_TIMEOUT', 'MEDIUM', 'PUTAWAY_TASK', 73, 'PT-20260420155220823', 'PT-20260420155220823 待上架超时', '上架任务 PT-20260420155220823 已超过 1.000 分钟 未完成上架', 1, '2026-04-20 07:52:20', NULL, '2026-04-20 07:56:39', '2026-04-20 07:56:39');
INSERT INTO `alert_record` VALUES (158, 'ALERT-20260420155639048', 2, 'AR-PUTAWAY-TIMEOUT', 'PUTAWAY_TIMEOUT', 'MEDIUM', 'PUTAWAY_TASK', 73, 'PT-20260420155220823', 'PT-20260420155220823 待上架超时', '上架任务 PT-20260420155220823 已超过 1.000 分钟 未完成上架', 1, '2026-04-20 07:52:20', NULL, '2026-04-20 07:56:39', '2026-04-20 07:56:39');

-- ----------------------------
-- Table structure for alert_rule
-- ----------------------------
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预警规则ID',
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则编号',
  `rule_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `alert_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预警类型',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '严重级别',
  `threshold_value` decimal(14, 3) NOT NULL COMMENT '阈值',
  `threshold_unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '阈值单位',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则说明',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_alert_rule_code`(`rule_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预警规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of alert_rule
-- ----------------------------
INSERT INTO `alert_rule` VALUES (1, 'AR-LOW-STOCK', '低库存预警', 'LOW_STOCK', 'MEDIUM', 50.000, 'QUANTITY', 1, '当可用库存小于等于阈值时触发', 10, '2026-04-19 11:20:53', '2026-04-20 07:27:37');
INSERT INTO `alert_rule` VALUES (2, 'AR-PUTAWAY-TIMEOUT', '待上架超时预警', 'PUTAWAY_TIMEOUT', 'MEDIUM', 1.000, 'MINUTE', 1, '上架任务长时间未处理时触发', 20, '2026-04-19 11:20:53', '2026-04-20 07:51:24');
INSERT INTO `alert_rule` VALUES (3, 'AR-OUTBOUND-PICK-TIMEOUT', '待拣货超时预警', 'OUTBOUND_PICK_TIMEOUT', 'MEDIUM', 4.000, 'HOUR', 1, '拣货任务长时间未拣货时触发', 30, '2026-04-19 11:20:53', '2026-04-19 11:20:53');
INSERT INTO `alert_rule` VALUES (4, 'AR-OUTBOUND-SHIP-TIMEOUT', '待出库超时预警', 'OUTBOUND_SHIP_TIMEOUT', 'HIGH', 4.000, 'HOUR', 1, '拣货完成后长时间未出库时触发', 40, '2026-04-19 11:20:53', '2026-04-19 11:20:53');
INSERT INTO `alert_rule` VALUES (5, 'AR-ABNORMAL-STOCK-STAGNANT', '异常库存滞留预警', 'ABNORMAL_STOCK_STAGNANT', 'HIGH', 24.000, 'HOUR', 1, '异常库存长时间未处理时触发', 50, '2026-04-19 11:20:53', '2026-04-19 11:20:53');
INSERT INTO `alert_rule` VALUES (6, 'AR-STOCKTAKING-CONFIRM-TIMEOUT', '待盘点确认超时预警', 'STOCKTAKING_CONFIRM_TIMEOUT', 'MEDIUM', 8.000, 'HOUR', 1, '盘点结果保存后长时间未确认时触发', 60, '2026-04-19 11:20:53', '2026-04-19 11:20:53');
INSERT INTO `alert_rule` VALUES (7, 'AR-INBOUND-PENDING-INSPECTION', '待质检超时预警', 'INBOUND_PENDING_INSPECTION', 'MEDIUM', 8.000, 'HOUR', 1, '入库记录长时间未质检时触发', 70, '2026-04-19 11:20:53', '2026-04-19 11:20:53');
INSERT INTO `alert_rule` VALUES (8, 'AR-NEAR-EXPIRY', '临期预警', 'NEAR_EXPIRY', 'MEDIUM', 0.000, 'DAY', 1, '按产品预警天数触发临期预警', 80, '2026-04-20 06:37:44', '2026-04-20 06:37:44');
INSERT INTO `alert_rule` VALUES (9, 'AR-EXPIRED', '过期预警', 'EXPIRED', 'HIGH', 0.000, 'DAY', 1, '批次到期后立即触发过期预警', 90, '2026-04-20 06:37:44', '2026-04-20 06:37:44');

-- ----------------------------
-- Table structure for assistant_action_plan
-- ----------------------------
DROP TABLE IF EXISTS `assistant_action_plan`;
CREATE TABLE `assistant_action_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动作计划ID',
  `action_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作计划编号',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源类型',
  `action_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标资源ID',
  `target_label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标资源名称',
  `fields_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '已解析字段JSON',
  `missing_fields_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '缺失字段JSON',
  `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'LOW' COMMENT '风险级别',
  `confirmation_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CONFIRM_CARD' COMMENT '确认模式',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：DRAFT READY CONFIRMED EXECUTED FAILED CANCELLED EXPIRED',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '摘要说明',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败错误码',
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `executed_at` datetime NULL DEFAULT NULL COMMENT '执行时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_assistant_action_plan_code`(`action_code` ASC) USING BTREE,
  INDEX `idx_assistant_action_plan_session`(`session_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  CONSTRAINT `fk_assistant_action_plan_session` FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能助手写操作动作计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assistant_action_plan
-- ----------------------------
INSERT INTO `assistant_action_plan` VALUES (17, 'AWA-20260420021140316', 62, 1, 'product_unit', 'CREATE', NULL, '助手测试盒', '{\"unitName\":\"助手测试盒\",\"unitSymbol\":\"ZTH\",\"unitType\":\"包装单位\",\"precisionDigits\":0,\"status\":1,\"sortOrder\":91,\"remark\":\"用于助手新增测试\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品单位：助手测试盒', NULL, NULL, '2026-04-19 18:11:40', '2026-04-19 18:12:04', '2026-04-20 02:12:05');
INSERT INTO `assistant_action_plan` VALUES (18, 'AWA-20260420021408292', 63, 1, 'product_unit', 'CREATE', NULL, 'AI测试箱20260420', '{\"unitName\":\"AI测试箱20260420\",\"unitSymbol\":\"ATX20\",\"unitType\":\"包装单位\",\"precisionDigits\":0,\"status\":1,\"sortOrder\":92,\"remarks\":\"AI测试单位\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品单位：AI测试箱20260420', NULL, NULL, '2026-04-19 18:14:08', '2026-04-19 18:14:33', '2026-04-20 02:14:33');
INSERT INTO `assistant_action_plan` VALUES (19, 'AWA-20260420021520089', 64, 1, 'product_origin', 'CREATE', NULL, 'AI测试产地20260420', '{\"originName\":\"AI测试产地20260420\",\"countryName\":\"中国\",\"provinceName\":\"安徽\",\"cityName\":\"合肥\",\"status\":1,\"sort_order\":93,\"remarks\":\"AI测试产地\"}', '[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}]', 'MEDIUM', 'CONFIRM_CARD', 'DRAFT', '还缺少以下字段：排序值', NULL, NULL, '2026-04-19 18:15:20', '2026-04-19 18:15:20', NULL);
INSERT INTO `assistant_action_plan` VALUES (20, 'AWA-20260420021526034', 64, 1, 'product_origin', 'CREATE', NULL, 'AI测试产地20260420', '{\"originName\":\"AI测试产地20260420\",\"countryName\":\"中国\",\"provinceName\":\"安徽\",\"cityName\":\"合肥\",\"status\":\"1\",\"sortOrder\":93,\"remarks\":\"AI测试产地\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产地信息：AI测试产地20260420', NULL, NULL, '2026-04-19 18:15:26', '2026-04-19 18:15:29', '2026-04-20 02:15:30');
INSERT INTO `assistant_action_plan` VALUES (21, 'AWA-20260420021534637', 65, 1, 'storage_condition', 'CREATE', NULL, 'AI测试储存条件20260420', '{\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"status\":1,\"sortOrder\":94,\"remarks\":\"AI测试储存条件\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增储存条件：AI测试储存条件20260420', NULL, NULL, '2026-04-19 18:15:34', '2026-04-19 18:15:37', '2026-04-20 02:15:38');
INSERT INTO `assistant_action_plan` VALUES (22, 'AWA-20260420021542676', 66, 1, 'quality_grade', 'CREATE', NULL, 'AI测试等级20260420', '{\"gradeName\":\"AI测试等级20260420\",\"minScore\":90,\"maxScore\":95,\"status\":1,\"sortOrder\":95,\"remarks\":\"AI测试等级\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增品质等级：AI测试等级20260420', NULL, NULL, '2026-04-19 18:15:42', '2026-04-19 18:15:45', '2026-04-20 02:15:46');
INSERT INTO `assistant_action_plan` VALUES (23, 'AWA-20260420021600207', 67, 1, 'category', 'CREATE', NULL, 'AI测试分类20260420', '{\"categoryName\":\"AI测试分类20260420\",\"parentCategoryCode\":\"CAT-20260419051005341\",\"defaultStorageConditionCode\":\"SC-20260420021537855\",\"shelfLifeDays\":15,\"warningDays\":3,\"status\":1,\"sortOrder\":96,\"remarks\":\"AI测试分类\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品分类：AI测试分类20260420', NULL, NULL, '2026-04-19 18:16:00', '2026-04-19 18:16:04', '2026-04-20 02:16:05');
INSERT INTO `assistant_action_plan` VALUES (24, 'AWA-20260420021905965', 70, 1, 'product_archive', 'CREATE', NULL, 'AI测试产品20260420', '{\"productName\":\"AI测试产品20260420\",\"产品规格\":\"100g/盒\",\"产品分类\":\"AI测试分类20260420\",\"产品单位\":\"AI测试箱20260420\",\"产地信息\":\"AI测试产地20260420\",\"storageConditionId\":\"43\",\"shelfLifeDays\":15,\"预警提前天数\":3,\"qualityGradeId\":\"34\",\"status\":1,\"排序值\":97,\"remarks\":\"AI测试产品档案\"}', '[{\"field\":\"categoryId\",\"label\":\"产品分类\",\"hint\":\"请提供名称或编号\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"hint\":\"请提供名称或编号\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"hint\":\"请提供名称或编号\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"hint\":null},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}]', 'MEDIUM', 'CONFIRM_CARD', 'DRAFT', '还缺少以下字段：产品分类、产品单位、产地信息、预警提前天数、排序值', NULL, NULL, '2026-04-19 18:19:05', '2026-04-19 18:19:05', NULL);
INSERT INTO `assistant_action_plan` VALUES (25, 'AWA-20260420022110873', 71, 1, 'product_archive', 'CREATE', NULL, 'AI测试产品20260420', '{\"productName\":\"AI测试产品20260420\",\"productSpecification\":\"100g/盒\",\"categoryId\":\"37\",\"unitId\":\"40\",\"originId\":\"35\",\"storageConditionId\":\"43\",\"shelfLifeDays\":15,\"warningDays\":3,\"qualityGradeId\":\"34\",\"status\":1,\"sortOrder\":97,\"remarks\":\"AI测试产品档案\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品档案：AI测试产品20260420', NULL, NULL, '2026-04-19 18:21:10', '2026-04-19 18:21:36', '2026-04-20 02:21:36');
INSERT INTO `assistant_action_plan` VALUES (26, 'AWA-20260420022219020', 72, 1, 'product_unit', 'UPDATE', 40, 'AI测试箱20260420', '{\"名称\":\"AI测试箱20260420改\",\"unitSymbol\":\"ATX20B\",\"排序值\":101,\"remarks\":\"AI测试单位更新\",\"unitName\":\"AI测试箱20260420\",\"unitType\":\"包装单位\",\"precisionDigits\":0,\"status\":1,\"sortOrder\":92}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产品单位：AI测试箱20260420', NULL, NULL, '2026-04-19 18:22:19', '2026-04-19 18:22:22', '2026-04-20 02:22:23');
INSERT INTO `assistant_action_plan` VALUES (27, 'AWA-20260420022228373', 73, 1, 'product_origin', 'UPDATE', 35, 'AI测试产地20260420', '{\"名称\":\"AI测试产地20260420改\",\"cityName\":\"芜湖\",\"排序值\":102,\"remarks\":\"AI测试产地更新\",\"originName\":\"AI测试产地20260420\",\"countryName\":\"中国\",\"provinceName\":\"安徽\",\"status\":1,\"sortOrder\":93}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产地信息：AI测试产地20260420', NULL, NULL, '2026-04-19 18:22:28', '2026-04-19 18:22:32', '2026-04-20 02:22:33');
INSERT INTO `assistant_action_plan` VALUES (28, 'AWA-20260420022238187', 74, 1, 'storage_condition', 'UPDATE', 43, 'AI测试储存条件20260420', '{\"名称\":\"AI测试储存条件20260420改\",\"ventilationRequirement\":\"密闭\",\"排序值\":103,\"remarks\":\"AI测试储存条件更新\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"temperatureMin\":null,\"temperatureMax\":null,\"humidityMin\":null,\"humidityMax\":null,\"lightRequirement\":\"无特殊要求\",\"status\":1,\"sortOrder\":94}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新储存条件：AI测试储存条件20260420', NULL, NULL, '2026-04-19 18:22:38', '2026-04-19 18:22:42', '2026-04-20 02:22:43');
INSERT INTO `assistant_action_plan` VALUES (29, 'AWA-20260420022247891', 75, 1, 'quality_grade', 'UPDATE', 34, 'AI测试等级20260420', '{\"新名称\":\"AI测试等级20260420改\",\"scoreMax\":96,\"排序值\":104,\"remarks\":\"AI测试品质等级更新\",\"gradeName\":\"AI测试等级20260420\",\"scoreMin\":null,\"status\":1,\"sortOrder\":95}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新品质等级：AI测试等级20260420', NULL, NULL, '2026-04-19 18:22:47', '2026-04-19 18:22:52', '2026-04-20 02:22:52');
INSERT INTO `assistant_action_plan` VALUES (30, 'AWA-20260420022256988', 76, 1, 'category', 'UPDATE', 37, 'AI测试分类20260420', '{\"categoryName\":\"AI测试分类20260420改\",\"warningDays\":4,\"排序值\":105,\"remarks\":\"AI测试分类更新\",\"parentId\":null,\"sortOrder\":96,\"status\":1,\"defaultStorageConditionId\":null,\"shelfLifeDays\":15}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产品分类：AI测试分类20260420', NULL, NULL, '2026-04-19 18:22:56', '2026-04-19 18:23:01', '2026-04-20 02:23:02');
INSERT INTO `assistant_action_plan` VALUES (31, 'AWA-20260420022306808', 77, 1, 'product_archive', 'UPDATE', 41, 'AI测试产品20260420', '{\"productName\":\"AI测试产品20260420改\",\"warningDays\":4,\"sortOrder\":106,\"remarks\":\"AI测试产品更新\",\"productSpecification\":\"100g/盒\",\"categoryId\":\"37\",\"unitId\":\"40\",\"originId\":\"35\",\"storageConditionId\":\"43\",\"shelfLifeDays\":15,\"qualityGradeId\":\"34\",\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产品档案：AI测试产品20260420', NULL, NULL, '2026-04-19 18:23:06', '2026-04-19 18:23:11', '2026-04-20 02:23:11');
INSERT INTO `assistant_action_plan` VALUES (32, 'AWA-20260420022551918', 78, 1, 'product_unit', 'UPDATE', 40, 'AI测试箱20260420', '{\"unitName\":\"AI测试箱20260420改\",\"unitSymbol\":\"ATX20B\",\"sortOrder\":101,\"remarks\":\"AI测试单位更新\",\"unitType\":\"包装单位\",\"precisionDigits\":0,\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产品单位：AI测试箱20260420', NULL, NULL, '2026-04-19 18:25:51', '2026-04-19 18:25:56', '2026-04-20 02:25:56');
INSERT INTO `assistant_action_plan` VALUES (33, 'AWA-20260420022602709', 79, 1, 'product_origin', 'UPDATE', 35, 'AI测试产地20260420', '{\"originName\":\"AI测试产地20260420改\",\"cityName\":\"芜湖\",\"sortOrder\":102,\"remarks\":\"AI测试产地更新\",\"countryName\":\"中国\",\"provinceName\":\"安徽\",\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新产地信息：AI测试产地20260420', NULL, NULL, '2026-04-19 18:26:02', '2026-04-19 18:26:07', '2026-04-20 02:26:07');
INSERT INTO `assistant_action_plan` VALUES (34, 'AWA-20260420022613331', 80, 1, 'storage_condition', 'UPDATE', 43, 'AI测试储存条件20260420', '{\"新名称\":\"AI测试储存条件20260420改\",\"ventilationRequirement\":\"密闭\",\"sortOrder\":103,\"remarks\":\"AI测试储存条件更新\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"temperatureMin\":null,\"temperatureMax\":null,\"humidityMin\":null,\"humidityMax\":null,\"lightRequirement\":\"无特殊要求\",\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新储存条件：AI测试储存条件20260420', NULL, NULL, '2026-04-19 18:26:13', '2026-04-19 18:26:18', '2026-04-20 02:26:18');
INSERT INTO `assistant_action_plan` VALUES (35, 'AWA-20260420022625532', 81, 1, 'quality_grade', 'UPDATE', 34, 'AI测试等级20260420', '{\"gradeName\":\"AI测试等级20260420改\",\"scoreMax\":96,\"sortOrder\":104,\"remarks\":\"AI测试品质等级更新\",\"scoreMin\":null,\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新品质等级：AI测试等级20260420', NULL, NULL, '2026-04-19 18:26:25', '2026-04-19 18:26:29', '2026-04-20 02:26:29');
INSERT INTO `assistant_action_plan` VALUES (36, 'AWA-20260420022834488', 82, 1, 'storage_condition', 'UPDATE', 43, 'AI测试储存条件20260420', '{\"conditionName\":\"AI测试储存条件20260420改\",\"ventilationRequirement\":\"密闭\",\"sortOrder\":103,\"remarks\":\"AI测试储存条件更新\",\"storageType\":\"常温\",\"temperatureMin\":null,\"temperatureMax\":null,\"humidityMin\":null,\"humidityMax\":null,\"lightRequirement\":\"无特殊要求\",\"status\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已更新储存条件：AI测试储存条件20260420', NULL, NULL, '2026-04-19 18:28:34', '2026-04-19 18:28:38', '2026-04-20 02:28:39');
INSERT INTO `assistant_action_plan` VALUES (37, 'AWA-20260420022919938', 83, 1, 'product_archive', 'DELETE', 41, 'AI测试产品20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除产品档案：AI测试产品20260420改', NULL, NULL, '2026-04-19 18:29:19', '2026-04-19 18:29:24', '2026-04-20 02:29:25');
INSERT INTO `assistant_action_plan` VALUES (38, 'AWA-20260420022933397', 84, 1, 'category', 'DELETE', 37, 'AI测试分类20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除产品分类：AI测试分类20260420改', NULL, NULL, '2026-04-19 18:29:33', '2026-04-19 18:29:38', '2026-04-20 02:29:39');
INSERT INTO `assistant_action_plan` VALUES (39, 'AWA-20260420022948861', 85, 1, 'quality_grade', 'DELETE', 34, 'AI测试等级20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除品质等级：AI测试等级20260420改', NULL, NULL, '2026-04-19 18:29:48', '2026-04-19 18:29:53', '2026-04-20 02:29:54');
INSERT INTO `assistant_action_plan` VALUES (40, 'AWA-20260420023002277', 86, 1, 'storage_condition', 'DELETE', 43, 'AI测试储存条件20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除储存条件：AI测试储存条件20260420改', NULL, NULL, '2026-04-19 18:30:02', '2026-04-19 18:30:07', '2026-04-20 02:30:07');
INSERT INTO `assistant_action_plan` VALUES (41, 'AWA-20260420023016796', 87, 1, 'product_origin', 'DELETE', 35, 'AI测试产地20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除产地信息：AI测试产地20260420改', NULL, NULL, '2026-04-19 18:30:16', '2026-04-19 18:30:21', '2026-04-20 02:30:21');
INSERT INTO `assistant_action_plan` VALUES (42, 'AWA-20260420023031197', 88, 1, 'product_unit', 'DELETE', 40, 'AI测试箱20260420改', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除产品单位：AI测试箱20260420改', NULL, NULL, '2026-04-19 18:30:31', '2026-04-19 18:30:36', '2026-04-20 02:30:36');
INSERT INTO `assistant_action_plan` VALUES (47, 'AWA-20260420024327093', 96, 1, 'category', 'CREATE', NULL, '海鲜', '{\"categoryName\":\"海鲜\"}', '[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null},{\"field\":\"status\",\"label\":\"状态\",\"hint\":null}]', 'MEDIUM', 'CONFIRM_CARD', 'DRAFT', '还缺少以下字段：排序值、状态', NULL, NULL, '2026-04-19 18:43:27', '2026-04-19 18:43:27', NULL);
INSERT INTO `assistant_action_plan` VALUES (48, 'AWA-20260420024626610', 98, 1, 'product_unit', 'CREATE', NULL, 'AI会话确认单位20260420', '{\"unitName\":\"AI会话确认单位20260420\",\"unitSymbol\":\"ACU20\",\"unitType\":\"包装单位\",\"precisionDigits\":0,\"status\":1,\"sortOrder\":108,\"remarks\":\"AI会话确认测试\"}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品单位：AI会话确认单位20260420', NULL, NULL, '2026-04-19 18:46:26', '2026-04-19 18:46:34', '2026-04-20 02:46:34');
INSERT INTO `assistant_action_plan` VALUES (49, 'AWA-20260420024710925', 99, 1, 'product_unit', 'DELETE', 44, 'AI会话确认单位20260420', '{}', '[]', 'HIGH', 'CONFIRM_DELETE_TEXT', 'EXECUTED', '已删除产品单位：AI会话确认单位20260420', NULL, NULL, '2026-04-19 18:47:10', '2026-04-19 18:47:19', '2026-04-20 02:47:19');
INSERT INTO `assistant_action_plan` VALUES (50, 'AWA-20260420025156201', 100, 1, 'category', 'CREATE', NULL, '海产品', '{\"categoryName\":\"海产品\",\"排序值\":1,\"status\":1,\"sortOrder\":1}', '[]', 'MEDIUM', 'CONFIRM_CARD', 'EXECUTED', '已新增产品分类：海产品', NULL, NULL, '2026-04-19 18:51:56', '2026-04-19 19:00:04', '2026-04-20 03:00:05');

-- ----------------------------
-- Table structure for assistant_message
-- ----------------------------
DROP TABLE IF EXISTS `assistant_message`;
CREATE TABLE `assistant_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息角色：user assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息正文',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TEXT' COMMENT '消息类型',
  `metadata_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '结构化结果元数据',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assistant_message_session`(`session_id` ASC, `id` ASC) USING BTREE,
  CONSTRAINT `fk_assistant_message_session` FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 256 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能助手消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assistant_message
-- ----------------------------
INSERT INTO `assistant_message` VALUES (11, 7, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 13:55:46');
INSERT INTO `assistant_message` VALUES (12, 7, 'assistant', '**结论：** 系统中共有3个仓库。\n\n**关键记录摘要：**\n1. **三里庵一号仓**（WH-20260419093735433）- 综合仓，负责人：小王，联系电话：15245851252，状态：启用\n2. **一号综合仓**（WH-202604190001）- 综合仓，负责人：王仓管，联系电话：13800000011，状态：启用\n3. **冷链中心仓**（WH-202604190002）- 冷藏仓，负责人：李冷链，联系电话：13800000012，状态：启用\n\n所有仓库目前都处于启用状态。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 13:55:55');
INSERT INTO `assistant_message` VALUES (13, 7, 'user', '系统有哪些仓库？', 'TEXT', NULL, '2026-04-19 13:56:48');
INSERT INTO `assistant_message` VALUES (14, 7, 'assistant', '**结论：** 系统中共有3个仓库。\n\n**关键记录摘要：**\n1. **三里庵一号仓**（WH-20260419093735433）- 综合仓，负责人：小王，联系电话：15245851252，状态：启用\n2. **一号综合仓**（WH-202604190001）- 综合仓，负责人：王仓管，联系电话：13800000011，状态：启用\n3. **冷链中心仓**（WH-202604190002）- 冷藏仓，负责人：李冷链，联系电话：13800000012，状态：启用\n\n所有仓库目前都处于启用状态。', 'TEXT', NULL, '2026-04-19 13:56:54');
INSERT INTO `assistant_message` VALUES (15, 7, 'user', '查一下内酯豆腐还有多少库存', 'TEXT', NULL, '2026-04-19 14:56:15');
INSERT INTO `assistant_message` VALUES (16, 7, 'assistant', '**结论：** 内酯豆腐目前库存为45.0件。\n\n**关键记录摘要：**\n- **商品名称：** 内酯豆腐\n- **商品编号：** PROD-20260419083640261\n- **仓库位置：** 三里庵一号仓 → 冷藏一号库区 → 冷藏库区1-1\n- **库存详情：**\n  - 现存数量：45.0件\n  - 预留数量：0.0件\n  - 锁定数量：0.0件\n  - 可用数量：45.0件\n\n所有库存均为可用状态，没有预留或锁定的数量。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"stockQuantity\":\"45.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"45.0\"}]}]}', '2026-04-19 14:56:24');
INSERT INTO `assistant_message` VALUES (17, 8, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:04:19');
INSERT INTO `assistant_message` VALUES (18, 8, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 15:04:19');
INSERT INTO `assistant_message` VALUES (19, 9, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:05:35');
INSERT INTO `assistant_message` VALUES (20, 9, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 15:05:35');
INSERT INTO `assistant_message` VALUES (21, 10, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:06:57');
INSERT INTO `assistant_message` VALUES (22, 10, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 15:06:57');
INSERT INTO `assistant_message` VALUES (25, 12, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:08:17');
INSERT INTO `assistant_message` VALUES (26, 12, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 15:08:17');
INSERT INTO `assistant_message` VALUES (31, 15, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:09:56');
INSERT INTO `assistant_message` VALUES (32, 15, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 15:09:56');
INSERT INTO `assistant_message` VALUES (36, 18, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 15:13:04');
INSERT INTO `assistant_message` VALUES (37, 18, 'assistant', '**结论：** 系统中共有 **3 个仓库**。\n\n**关键记录摘要如下：**\n\n| 仓库编号 | 仓库名称 | 仓库类型 | 负责人 | 联系电话 | 状态 |\n| :--- | :--- | :--- | :--- | :--- | :--- |\n| WH-20260419093735433 | 三里庵一号仓 | 综合仓 | 小王 | 15245851252 | 启用 |\n| WH-202604190001 | 一号综合仓 | 综合仓 | 王仓管 | 13800000011 | 启用 |\n| WH-202604190002 | 冷链中心仓 | 冷藏仓 | 李冷链 | 13800000012 | 启用 |', 'RESULT', '{\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 15:13:14');
INSERT INTO `assistant_message` VALUES (38, 19, 'user', '查一下内酯豆腐库存记录', 'TEXT', NULL, '2026-04-19 15:44:56');
INSERT INTO `assistant_message` VALUES (39, 19, 'assistant', '找到了内酯豆腐的产品信息。现在查询该产品的库存记录：\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_inventory_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 15:45:04');
INSERT INTO `assistant_message` VALUES (40, 19, 'user', '查一下内酯豆腐的实时库存', 'TEXT', NULL, '2026-04-19 16:01:08');
INSERT INTO `assistant_message` VALUES (41, 19, 'assistant', '**结论**：内酯豆腐的实时库存为45.0箱。\n\n**关键记录摘要**：\n- **商品名称**：内酯豆腐\n- **仓库位置**：三里庵一号仓 / 冷藏一号库区 / 冷藏库区1-1\n- **库存详情**：现存数量45.0箱，预留和锁定数量均为0.0箱，可用数量45.0箱', 'RESULT', '{\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"stockQuantity\":\"45.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"45.0\"}]}]}', '2026-04-19 16:01:17');
INSERT INTO `assistant_message` VALUES (42, 20, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:09:06');
INSERT INTO `assistant_message` VALUES (43, 20, 'assistant', '找到了内酯豆腐的产品信息，现在查询该产品的出库记录：\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_outbound_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:09:13');
INSERT INTO `assistant_message` VALUES (46, 22, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 16:11:33');
INSERT INTO `assistant_message` VALUES (47, 22, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 16:11:33');
INSERT INTO `assistant_message` VALUES (50, 24, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:12:54');
INSERT INTO `assistant_message` VALUES (51, 24, 'assistant', '找到了内酯豆腐的产品信息，商品编号为 PROD-20260419083640261。现在查询该产品的出库记录：\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_outbound_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:13:03');
INSERT INTO `assistant_message` VALUES (52, 25, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:19:09');
INSERT INTO `assistant_message` VALUES (53, 25, 'assistant', '找到了内酯豆腐的产品信息，现在查询该产品的出库记录。\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_outbound_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:19:17');
INSERT INTO `assistant_message` VALUES (54, 25, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:24:49');
INSERT INTO `assistant_message` VALUES (55, 25, 'assistant', '找到了内酯豆腐的产品信息，现在查询该产品的出库记录。\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_outbound_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:24:56');
INSERT INTO `assistant_message` VALUES (62, 25, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:36:19');
INSERT INTO `assistant_message` VALUES (63, 25, 'assistant', '找到了内酯豆腐的产品信息，现在查询该产品的出库记录。\n\n<｜DSML｜function_calls>\n<｜DSML｜invoke name=\"query_outbound_records\">\n<｜DSML｜parameter name=\"productCode\" string=\"true\">PROD-20260419083640261</｜DSML｜parameter>\n<｜DSML｜parameter name=\"limit\" string=\"false\">20</｜DSML｜parameter>\n</｜DSML｜invoke>\n</｜DSML｜function_calls>', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:36:28');
INSERT INTO `assistant_message` VALUES (72, 33, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 16:40:55');
INSERT INTO `assistant_message` VALUES (73, 33, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 16:40:55');
INSERT INTO `assistant_message` VALUES (76, 35, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:41:59');
INSERT INTO `assistant_message` VALUES (77, 35, 'assistant', '**结论：** 系统中有1条内酯豆腐的出库记录。\n\n**关键记录摘要：**\n- **记录编号：** OR-202604190001\n- **出库单编号：** OUT-202604190003\n- **客户名称：** 上海鲜享商超\n- **仓库信息：** 三里庵一号仓 → 冷藏一号库区 → 冷藏库区1-1\n- **商品名称：** 内酯豆腐\n- **出库数量：** 5.0\n- **出库时间：** 2026-04-21 15:40\n\n这条记录显示内酯豆腐从冷藏库区出库，客户为上海鲜享商超，出库数量为5个单位。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', '2026-04-19 16:42:09');
INSERT INTO `assistant_message` VALUES (78, 36, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:49:08');
INSERT INTO `assistant_message` VALUES (79, 36, 'user', '查一下内酯豆腐的出库记录', 'TEXT', NULL, '2026-04-19 16:49:32');
INSERT INTO `assistant_message` VALUES (80, 36, 'assistant', '**结论：** 内酯豆腐共有1条出库记录。\n\n**关键记录摘要：**\n- **记录编号：** OR-202604190001\n- **出库单编号：** OUT-202604190003\n- **客户名称：** 上海鲜享商超\n- **仓库信息：** 三里庵一号仓 / 冷藏一号库区 / 冷藏库区1-1\n- **出库数量：** 5.0盒\n- **出库时间：** 2026-04-21 15:40\n\n内酯豆腐（规格：350g/盒）仅有一次出库记录，出库给了上海鲜享商超。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', '2026-04-19 16:49:53');
INSERT INTO `assistant_message` VALUES (87, 40, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 16:52:13');
INSERT INTO `assistant_message` VALUES (88, 40, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 16:52:13');
INSERT INTO `assistant_message` VALUES (91, 42, 'user', '你好', 'TEXT', NULL, '2026-04-19 16:54:14');
INSERT INTO `assistant_message` VALUES (92, 42, 'assistant', '你好！我是农产品仓储管理系统的智能助手，很高兴为您服务。\n\n作为系统管理员，您当前在出库记录查询页面。我可以帮助您：\n\n1. **查询系统数据**：出库记录、入库记录、库存信息、质检记录等\n2. **解释业务数据**：说明库存变化原因、分析业务记录\n3. **提供管理支持**：查询预警信息、基础主数据等\n\n请告诉我您需要什么帮助？比如：\n- 查看最近的出库记录\n- 查询特定产品的库存情况\n- 了解预警信息\n- 其他系统数据查询', 'TEXT', NULL, '2026-04-19 16:54:20');
INSERT INTO `assistant_message` VALUES (93, 42, 'user', '查看最近的出库记录', 'TEXT', NULL, '2026-04-19 16:54:47');
INSERT INTO `assistant_message` VALUES (94, 42, 'assistant', '**结论**：系统中共有1条出库记录。\n\n**关键记录摘要**：\n- **记录编号**：OR-202604190001\n- **出库单编号**：OUT-202604190003\n- **客户名称**：上海鲜享商超\n- **仓库信息**：三里庵一号仓 > 冷藏一号库区 > 冷藏库区1-1\n- **商品信息**：内酯豆腐，数量5.0\n- **发生时间**：2026-04-21 15:40\n\n这是系统中最新的出库记录。如果您需要查看更多历史记录或按特定条件筛选，请告诉我您的具体需求。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', '2026-04-19 16:55:03');
INSERT INTO `assistant_message` VALUES (95, 43, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 16:58:43');
INSERT INTO `assistant_message` VALUES (96, 43, 'assistant', '**结论**：系统中共有 **3 个仓库**。\n\n**仓库列表摘要**：\n1.  **三里庵一号仓** (WH-20260419093735433)\n    *   类型：综合仓\n    *   负责人：小王\n    *   状态：启用\n\n2.  **一号综合仓** (WH-202604190001)\n    *   类型：综合仓\n    *   负责人：王仓管\n    *   状态：启用\n\n3.  **冷链中心仓** (WH-202604190002)\n    *   类型：冷藏仓\n    *   负责人：李冷链\n    *   状态：启用', 'RESULT', '{\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', '2026-04-19 16:58:58');
INSERT INTO `assistant_message` VALUES (97, 44, 'user', '你好', 'TEXT', NULL, '2026-04-19 17:11:14');
INSERT INTO `assistant_message` VALUES (98, 44, 'assistant', '你好！我是农产品仓储管理系统的智能助手，很高兴为您服务。\n\n作为系统管理员，您当前在出库记录查询页面。我可以帮您：\n\n1. **查询出库业务数据** - 出库订单、出库任务、出库记录\n2. **查询库存数据** - 库存、交易记录、调整记录、盘点记录\n3. **查询基础主数据** - 产品、分类、单位、供应商、客户等\n4. **查询仓储结构** - 仓库、库区、库位信息\n5. **查询质检与损耗数据** - 质检记录、异常库存、损耗记录\n6. **查询预警与消息** - 预警规则、预警记录、消息通知\n\n请告诉我您需要查询什么信息，或者有什么具体问题需要帮助？', 'TEXT', NULL, '2026-04-19 17:11:19');
INSERT INTO `assistant_message` VALUES (99, 44, 'user', '查一下系统有没有预警信息', 'TEXT', NULL, '2026-04-19 17:11:29');
INSERT INTO `assistant_message` VALUES (100, 44, 'assistant', '**结论：** 系统当前没有预警信息。\n\n根据查询结果，系统中没有找到任何预警记录。这意味着目前所有监控指标（如库存量、保质期、温湿度等）都在正常范围内，没有触发预警规则。\n\n如果您需要查看预警规则配置或希望监控特定指标，我可以帮您进一步查询。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"预警记录查询\",\"summary\":\"预警记录查询：未找到匹配结果\",\"routePath\":\"/alerts\",\"routeLabel\":\"预警中心\",\"columns\":[],\"rows\":[]}]}', '2026-04-19 17:11:40');
INSERT INTO `assistant_message` VALUES (101, 44, 'user', '新增一个海鲜产品分类', 'TEXT', NULL, '2026-04-19 17:12:01');
INSERT INTO `assistant_message` VALUES (102, 44, 'assistant', '**关键记录摘要：**\n系统中共有 8 个产品分类，包括：\n- 一级分类：豆制品、新鲜蔬菜、时令水果\n- 二级分类：叶菜类、根茎类（停用）、柑橘类\n- 三级分类：菠菜、生菜\n\n**建议操作：**\n1. 请通过系统管理界面手动创建\"海鲜\"产品分类\n2. 创建时需指定：分类名称、层级（建议设为一级分类）、存储条件等\n3. 如果需要了解海鲜产品的存储条件要求，我可以帮您查询相关标准\n\n您是否需要查询其他信息来辅助创建海鲜分类？', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 8 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260419051005341\",\"categoryName\":\"豆制品\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"豆制品存储条件\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T21:10:05+08:00\"},{\"categoryCode\":\"CAT-A00\",\"categoryName\":\"新鲜蔬菜\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T19:00:01+08:00\"},{\"categoryCode\":\"CAT-A01\",\"categoryName\":\"叶菜类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A01-01\",\"categoryName\":\"菠菜\",\"categoryLevel\":\"3\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A01-02\",\"categoryName\":\"生菜\",\"categoryLevel\":\"3\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A02\",\"categoryName\":\"根茎类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"根茎阴凉干燥标准\",\"statusLabel\":\"停用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-B00\",\"categoryName\":\"时令水果\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T19:00:01+08:00\"},{\"categoryCode\":\"CAT-B01\",\"categoryName\":\"柑橘类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"水果冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"}]}]}', '2026-04-19 17:12:19');
INSERT INTO `assistant_message` VALUES (109, 60, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 18:10:00');
INSERT INTO `assistant_message` VALUES (110, 60, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 18:10:00');
INSERT INTO `assistant_message` VALUES (113, 62, 'user', '请新增一个产品单位，单位名称是助手测试盒，单位符号是ZTH，单位类型是包装单位，精度位数0，状态启用，排序值91，备注用于助手新增测试', 'TEXT', NULL, '2026-04-19 18:11:35');
INSERT INTO `assistant_message` VALUES (114, 62, 'assistant', '**结论**：已准备好新增产品单位的操作计划。\n\n**操作计划摘要**：\n- 单位名称：助手测试盒\n- 单位符号：ZTH\n- 单位类型：包装单位\n- 精度位数：0\n- 状态：启用\n- 排序值：91\n- 备注：用于助手新增测试\n\n请确认是否执行此新增操作？确认后我将为您创建这个产品单位。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021140316\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"助手测试盒\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"助手测试盒\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ZTH\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"91\"},{\"field\":\"remark\",\"label\":\"remark\",\"value\":\"用于助手新增测试\"}]}}', '2026-04-19 18:11:44');
INSERT INTO `assistant_message` VALUES (115, 62, 'assistant', '已新增产品单位：助手测试盒', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021140316\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"助手测试盒\",\"summary\":\"已新增产品单位：助手测试盒\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"助手测试盒\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ZTH\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"91\"},{\"field\":\"remark\",\"label\":\"remark\",\"value\":\"用于助手新增测试\"}]}}', '2026-04-19 18:12:04');
INSERT INTO `assistant_message` VALUES (116, 63, 'user', '请新增一个产品单位，单位名称是AI测试箱20260420，单位符号是ATX20，单位类型是包装单位，精度位数0，状态启用，排序值92，备注AI测试单位', 'TEXT', NULL, '2026-04-19 18:14:03');
INSERT INTO `assistant_message` VALUES (117, 63, 'assistant', '**结论**：已准备好新增产品单位的操作计划。\n\n**操作计划摘要**：\n- 单位名称：AI测试箱20260420\n- 单位符号：ATX20  \n- 单位类型：包装单位\n- 精度位数：0\n- 状态：启用\n- 排序值：92\n- 备注：AI测试单位\n\n请确认是否执行此新增操作？确认后我将为您创建这个产品单位。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021408292\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位\"}]}}', '2026-04-19 18:14:11');
INSERT INTO `assistant_message` VALUES (118, 63, 'assistant', '已新增产品单位：AI测试箱20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021408292\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已新增产品单位：AI测试箱20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位\"}]}}', '2026-04-19 18:14:33');
INSERT INTO `assistant_message` VALUES (119, 64, 'user', '请新增一个产地信息，产地名称是AI测试产地20260420，国家名称是中国，省份名称是安徽，城市名称是合肥，状态启用，排序值93，备注AI测试产地', 'TEXT', NULL, '2026-04-19 18:15:14');
INSERT INTO `assistant_message` VALUES (120, 64, 'assistant', '**结论**：产地信息新增操作计划已准备就绪，请确认是否执行。\n\n**操作摘要**：\n- 产地名称：AI测试产地20260420\n- 国家：中国\n- 省份：安徽\n- 城市：合肥\n- 状态：启用（对应值1）\n- 排序值：93\n- 备注：AI测试产地\n\n请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021526034\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好新增产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"合肥\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地\"}]}}', '2026-04-19 18:15:29');
INSERT INTO `assistant_message` VALUES (121, 64, 'assistant', '已新增产地信息：AI测试产地20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021526034\",\"status\":\"EXECUTED\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已新增产地信息：AI测试产地20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"合肥\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地\"}]}}', '2026-04-19 18:15:29');
INSERT INTO `assistant_message` VALUES (122, 65, 'user', '请新增一个储存条件，条件名称是AI测试储存条件20260420，储存类型是常温，避光要求是无特殊要求，通风要求是普通通风，状态启用，排序值94，备注AI测试储存条件', 'TEXT', NULL, '2026-04-19 18:15:29');
INSERT INTO `assistant_message` VALUES (123, 65, 'assistant', '**结论**：已准备好新增储存条件的操作计划，请确认是否执行。\n\n**操作详情**：\n- 条件名称：AI测试储存条件20260420\n- 储存类型：常温\n- 避光要求：无特殊要求\n- 通风要求：普通通风\n- 状态：启用\n- 排序值：94\n- 备注：AI测试储存条件\n\n请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021534637\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好新增储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"普通通风\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件\"}]}}', '2026-04-19 18:15:37');
INSERT INTO `assistant_message` VALUES (124, 65, 'assistant', '已新增储存条件：AI测试储存条件20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021534637\",\"status\":\"EXECUTED\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已新增储存条件：AI测试储存条件20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"普通通风\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件\"}]}}', '2026-04-19 18:15:37');
INSERT INTO `assistant_message` VALUES (125, 66, 'user', '请新增一个品质等级，等级名称是AI测试等级20260420，最低分90，最高分95，状态启用，排序值95，备注AI测试等级', 'TEXT', NULL, '2026-04-19 18:15:37');
INSERT INTO `assistant_message` VALUES (126, 66, 'assistant', '**结论**：已准备好新增品质等级的操作计划。\n\n**操作计划摘要**：\n- 操作类型：新增品质等级\n- 等级名称：AI测试等级20260420\n- 最低分：90\n- 最高分：95\n- 状态：启用\n- 排序值：95\n- 备注：AI测试等级\n\n请确认是否执行此新增操作？确认后我将为您创建这个品质等级。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021542676\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好新增品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"minScore\",\"label\":\"minScore\",\"value\":\"90\"},{\"field\":\"maxScore\",\"label\":\"maxScore\",\"value\":\"95\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试等级\"}]}}', '2026-04-19 18:15:45');
INSERT INTO `assistant_message` VALUES (127, 66, 'assistant', '已新增品质等级：AI测试等级20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021542676\",\"status\":\"EXECUTED\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已新增品质等级：AI测试等级20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"minScore\",\"label\":\"minScore\",\"value\":\"90\"},{\"field\":\"maxScore\",\"label\":\"maxScore\",\"value\":\"95\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试等级\"}]}}', '2026-04-19 18:15:45');
INSERT INTO `assistant_message` VALUES (128, 67, 'user', '请新增一个产品分类，分类名称是AI测试分类20260420，上级分类是豆制品，默认储存条件是AI测试储存条件20260420，保质期天数15，预警天数3，状态启用，排序值96，备注AI测试分类', 'TEXT', NULL, '2026-04-19 18:15:45');
INSERT INTO `assistant_message` VALUES (129, 67, 'assistant', '**结论**：已准备好新增产品分类的操作计划，请确认是否执行。\n\n**操作计划摘要**：\n- 分类名称：AI测试分类20260420\n- 上级分类：豆制品（CAT-20260419051005341）\n- 默认储存条件：AI测试储存条件20260420（SC-20260420021537855）\n- 保质期天数：15天\n- 预警提前天数：3天\n- 状态：启用\n- 排序值：96\n- 备注：AI测试分类\n\n请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420021600207\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420\"},{\"field\":\"parentCategoryCode\",\"label\":\"parentCategoryCode\",\"value\":\"CAT-20260419051005341\"},{\"field\":\"defaultStorageConditionCode\",\"label\":\"defaultStorageConditionCode\",\"value\":\"SC-20260420021537855\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类\"}]}}', '2026-04-19 18:16:04');
INSERT INTO `assistant_message` VALUES (130, 67, 'assistant', '已新增产品分类：AI测试分类20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021600207\",\"status\":\"EXECUTED\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已新增产品分类：AI测试分类20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420\"},{\"field\":\"parentCategoryCode\",\"label\":\"parentCategoryCode\",\"value\":\"CAT-20260419051005341\"},{\"field\":\"defaultStorageConditionCode\",\"label\":\"defaultStorageConditionCode\",\"value\":\"SC-20260420021537855\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类\"}]}}', '2026-04-19 18:16:04');
INSERT INTO `assistant_message` VALUES (131, 68, 'user', '请新增一个产品档案，产品名称是AI测试产品20260420，产品规格是100g/盒，产品分类是AI测试分类20260420，产品单位是AI测试箱20260420，产地信息是AI测试产地20260420，储存条件是AI测试储存条件20260420，保质期天数15，预警提前天数3，品质等级是AI测试等级20260420，状态启用，排序值97，备注AI测试产品档案', 'TEXT', NULL, '2026-04-19 18:16:04');
INSERT INTO `assistant_message` VALUES (132, 68, 'assistant', '储存条件查询：共匹配到 1 条记录', 'RESULT', '{\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', '2026-04-19 18:16:16');
INSERT INTO `assistant_message` VALUES (133, 69, 'user', '请新增一个产品档案，产品名称是AI测试产品20260420，产品规格是100g/盒，产品分类是AI测试分类20260420，产品单位是AI测试箱20260420，产地信息是AI测试产地20260420，储存条件是AI测试储存条件20260420，保质期天数15，预警提前天数3，品质等级是AI测试等级20260420，状态启用，排序值97，备注AI测试产品档案', 'TEXT', NULL, '2026-04-19 18:16:56');
INSERT INTO `assistant_message` VALUES (134, 69, 'assistant', '储存条件查询：共匹配到 1 条记录', 'RESULT', '{\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', '2026-04-19 18:17:08');
INSERT INTO `assistant_message` VALUES (135, 70, 'user', '请新增一个产品档案，产品名称是AI测试产品20260420，产品规格是100g/盒，产品分类是AI测试分类20260420，产品单位是AI测试箱20260420，产地信息是AI测试产地20260420，储存条件是AI测试储存条件20260420，保质期天数15，预警提前天数3，品质等级是AI测试等级20260420，状态启用，排序值97，备注AI测试产品档案', 'TEXT', NULL, '2026-04-19 18:18:59');
INSERT INTO `assistant_message` VALUES (136, 70, 'assistant', '产地信息查询：共匹配到 1 条记录', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:15:29+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420021905965\",\"status\":\"DRAFT\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"还缺少以下字段：产品分类、产品单位、产地信息、预警提前天数、排序值\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"categoryId\",\"label\":\"产品分类\",\"hint\":\"请提供名称或编号\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"hint\":\"请提供名称或编号\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"hint\":\"请提供名称或编号\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"hint\":null},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}],\"previewFields\":[{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420\"},{\"field\":\"产品规格\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"产品分类\",\"label\":\"产品分类\",\"value\":\"AI测试分类20260420\"},{\"field\":\"产品单位\",\"label\":\"产品单位\",\"value\":\"AI测试箱20260420\"},{\"field\":\"产地信息\",\"label\":\"产地信息\",\"value\":\"AI测试产地20260420\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"预警提前天数\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"97\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品档案\"}]}}', '2026-04-19 18:19:14');
INSERT INTO `assistant_message` VALUES (137, 71, 'user', '请新增一个产品档案，产品名称是AI测试产品20260420，产品规格是100g/盒，产品分类是AI测试分类20260420，产品单位是AI测试箱20260420，产地信息是AI测试产地20260420，储存条件是AI测试储存条件20260420，保质期天数15，预警提前天数3，品质等级是AI测试等级20260420，状态启用，排序值97，备注AI测试产品档案', 'TEXT', NULL, '2026-04-19 18:21:03');
INSERT INTO `assistant_message` VALUES (138, 71, 'assistant', '**结论**：已准备好新增产品档案的操作计划，请确认是否执行。\n\n**操作摘要**：\n- 产品名称：AI测试产品20260420\n- 产品规格：100g/盒\n- 产品分类：AI测试分类20260420（ID: 37）\n- 产品单位：AI测试箱20260420（ID: 40）\n- 产地信息：AI测试产地20260420（ID: 35）\n- 储存条件：AI测试储存条件20260420（ID: 43）\n- 保质期天数：15天\n- 预警提前天数：3天\n- 品质等级：AI测试等级20260420（ID: 34）\n- 状态：启用\n- 排序值：97\n- 备注：AI测试产品档案\n\n请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022110873\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已整理好新增产品档案的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"97\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品档案\"}]}}', '2026-04-19 18:21:16');
INSERT INTO `assistant_message` VALUES (139, 71, 'assistant', '已新增产品档案：AI测试产品20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022110873\",\"status\":\"EXECUTED\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已新增产品档案：AI测试产品20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"97\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品档案\"}]}}', '2026-04-19 18:21:36');
INSERT INTO `assistant_message` VALUES (140, 72, 'user', '请把产品单位AI测试箱20260420修改为名称AI测试箱20260420改，单位符号ATX20B，排序值101，备注AI测试单位更新', 'TEXT', NULL, '2026-04-19 18:22:13');
INSERT INTO `assistant_message` VALUES (141, 72, 'assistant', '**结论**：已准备好修改产品单位的操作计划。\n\n**修改详情**：\n- 目标单位：AI测试箱20260420\n- 新名称：AI测试箱20260420改\n- 新单位符号：ATX20B\n- 新排序值：101\n- 新备注：AI测试单位更新\n- 其他信息保持不变：单位类型为包装单位，精度位数为0，状态为启用\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022219020\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"}]}}', '2026-04-19 18:22:22');
INSERT INTO `assistant_message` VALUES (142, 72, 'assistant', '已更新产品单位：AI测试箱20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022219020\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已更新产品单位：AI测试箱20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"}]}}', '2026-04-19 18:22:22');
INSERT INTO `assistant_message` VALUES (143, 73, 'user', '请把产地信息AI测试产地20260420修改为名称AI测试产地20260420改，城市名称芜湖，排序值102，备注AI测试产地更新', 'TEXT', NULL, '2026-04-19 18:22:22');
INSERT INTO `assistant_message` VALUES (144, 73, 'assistant', '**结论**：产地信息修改计划已准备就绪。\n\n**修改计划摘要**：\n- 目标产地：AI测试产地20260420\n- 新名称：AI测试产地20260420改\n- 城市名称：芜湖\n- 排序值：102\n- 备注：AI测试产地更新\n- 其他信息保持不变：国家名称（中国）、省份名称（安徽）、状态（启用）\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022228373\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"}]}}', '2026-04-19 18:22:32');
INSERT INTO `assistant_message` VALUES (145, 73, 'assistant', '已更新产地信息：AI测试产地20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022228373\",\"status\":\"EXECUTED\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已更新产地信息：AI测试产地20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"}]}}', '2026-04-19 18:22:32');
INSERT INTO `assistant_message` VALUES (146, 74, 'user', '请把储存条件AI测试储存条件20260420修改为名称AI测试储存条件20260420改，通风要求密闭，排序值103，备注AI测试储存条件更新', 'TEXT', NULL, '2026-04-19 18:22:32');
INSERT INTO `assistant_message` VALUES (147, 74, 'assistant', '**结论**：已准备好修改储存条件的操作计划。\n\n**修改计划摘要**：\n- 目标对象：AI测试储存条件20260420\n- 新名称：AI测试储存条件20260420改\n- 通风要求：密闭（原为：无特殊要求）\n- 排序值：103（原为：94）\n- 备注：AI测试储存条件更新\n- 其他字段保持不变：储存类型为常温，避光要求为无特殊要求，状态为启用\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022238187\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"}]}}', '2026-04-19 18:22:42');
INSERT INTO `assistant_message` VALUES (148, 74, 'assistant', '已更新储存条件：AI测试储存条件20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022238187\",\"status\":\"EXECUTED\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已更新储存条件：AI测试储存条件20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"}]}}', '2026-04-19 18:22:42');
INSERT INTO `assistant_message` VALUES (149, 75, 'user', '请把品质等级AI测试等级20260420修改为名称AI测试等级20260420改，最高分96，排序值104，备注AI测试品质等级更新', 'TEXT', NULL, '2026-04-19 18:22:42');
INSERT INTO `assistant_message` VALUES (150, 75, 'assistant', '**结论**：修改操作已准备就绪，请确认执行。\n\n**操作摘要**：\n- 目标：品质等级\"AI测试等级20260420\"\n- 修改内容：\n  - 名称：AI测试等级20260420 → AI测试等级20260420改\n  - 最高分：保持原值 → 96\n  - 排序值：95 → 104\n  - 备注：更新为\"AI测试品质等级更新\"\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022247891\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"}]}}', '2026-04-19 18:22:52');
INSERT INTO `assistant_message` VALUES (151, 75, 'assistant', '已更新品质等级：AI测试等级20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022247891\",\"status\":\"EXECUTED\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已更新品质等级：AI测试等级20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"}]}}', '2026-04-19 18:22:52');
INSERT INTO `assistant_message` VALUES (152, 76, 'user', '请把产品分类AI测试分类20260420修改为名称AI测试分类20260420改，预警天数4，排序值105，备注AI测试分类更新', 'TEXT', NULL, '2026-04-19 18:22:52');
INSERT INTO `assistant_message` VALUES (153, 76, 'assistant', '**操作计划已准备就绪**\n\n修改内容预览：\n- **目标分类**：AI测试分类20260420\n- **新分类名称**：AI测试分类20260420改\n- **预警提前天数**：4天\n- **排序值**：105\n- **备注**：AI测试分类更新\n\n系统还显示了当前的其他信息：\n- 当前排序值：96\n- 状态：启用（1）\n- 保质期天数：15天\n\n**请确认是否执行此修改操作？**', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022256988\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已整理好修改产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420\"},{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"105\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类更新\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"}]}}', '2026-04-19 18:23:01');
INSERT INTO `assistant_message` VALUES (154, 76, 'assistant', '已更新产品分类：AI测试分类20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022256988\",\"status\":\"EXECUTED\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已更新产品分类：AI测试分类20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420\"},{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"105\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类更新\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"}]}}', '2026-04-19 18:23:01');
INSERT INTO `assistant_message` VALUES (155, 77, 'user', '请把产品档案AI测试产品20260420修改为名称AI测试产品20260420改，预警提前天数4，排序值106，备注AI测试产品更新', 'TEXT', NULL, '2026-04-19 18:23:01');
INSERT INTO `assistant_message` VALUES (156, 77, 'assistant', '**修改计划已准备就绪**\n\n修改内容预览：\n- 目标产品：AI测试产品20260420\n- 新名称：AI测试产品20260420改\n- 预警提前天数：4天\n- 排序值：106\n- 备注：AI测试产品更新\n\n其他字段保持不变：\n- 产品规格：100g/盒\n- 产品分类：37\n- 产品单位：40\n- 产地信息：35\n- 储存条件：43\n- 保质期天数：15天\n- 品质等级：34\n- 状态：启用\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022306808\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已整理好修改产品档案的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"106\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品更新\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:23:11');
INSERT INTO `assistant_message` VALUES (157, 77, 'assistant', '已更新产品档案：AI测试产品20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022306808\",\"status\":\"EXECUTED\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已更新产品档案：AI测试产品20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"106\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品更新\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:23:11');
INSERT INTO `assistant_message` VALUES (158, 78, 'user', '请把产品单位AI测试箱20260420修改为名称AI测试箱20260420改，单位符号ATX20B，排序值101，备注AI测试单位更新', 'TEXT', NULL, '2026-04-19 18:25:46');
INSERT INTO `assistant_message` VALUES (159, 78, 'assistant', '**结论**：已准备好修改产品单位的操作计划。\n\n**操作详情**：\n- 目标单位：AI测试箱20260420\n- 新单位名称：AI测试箱20260420改\n- 新单位符号：ATX20B\n- 排序值：101\n- 备注：AI测试单位更新\n- 单位类型：包装单位（保持不变）\n- 精度位数：0（保持不变）\n- 状态：启用（保持不变）\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022551918\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:25:56');
INSERT INTO `assistant_message` VALUES (160, 78, 'assistant', '已更新产品单位：AI测试箱20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022551918\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已更新产品单位：AI测试箱20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:25:56');
INSERT INTO `assistant_message` VALUES (161, 79, 'user', '请把产地信息AI测试产地20260420修改为名称AI测试产地20260420改，城市名称芜湖，排序值102，备注AI测试产地更新', 'TEXT', NULL, '2026-04-19 18:25:56');
INSERT INTO `assistant_message` VALUES (162, 79, 'assistant', '**结论**：产地信息修改计划已准备就绪。\n\n**修改计划摘要**：\n- 目标产地：AI测试产地20260420\n- 新产地名称：AI测试产地20260420改\n- 城市名称：芜湖\n- 排序值：102\n- 备注：AI测试产地更新\n- 国家：中国（保持不变）\n- 省份：安徽（保持不变）\n- 状态：启用（保持不变）\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022602709\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:07');
INSERT INTO `assistant_message` VALUES (163, 79, 'assistant', '已更新产地信息：AI测试产地20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022602709\",\"status\":\"EXECUTED\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已更新产地信息：AI测试产地20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:07');
INSERT INTO `assistant_message` VALUES (164, 80, 'user', '请把储存条件AI测试储存条件20260420修改为名称AI测试储存条件20260420改，通风要求密闭，排序值103，备注AI测试储存条件更新', 'TEXT', NULL, '2026-04-19 18:26:07');
INSERT INTO `assistant_message` VALUES (165, 80, 'assistant', '**结论：** 修改操作已准备就绪，请确认执行。\n\n**操作摘要：**\n- 目标储存条件：AI测试储存条件20260420\n- 新名称：AI测试储存条件20260420改\n- 通风要求：密闭\n- 排序值：103\n- 备注：AI测试储存条件更新\n- 其他字段保持原值：储存类型为常温，避光要求为无特殊要求，状态为启用\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022613331\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:18');
INSERT INTO `assistant_message` VALUES (166, 80, 'assistant', '已更新储存条件：AI测试储存条件20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022613331\",\"status\":\"EXECUTED\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已更新储存条件：AI测试储存条件20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:18');
INSERT INTO `assistant_message` VALUES (167, 81, 'user', '请把品质等级AI测试等级20260420修改为名称AI测试等级20260420改，最高分96，排序值104，备注AI测试品质等级更新', 'TEXT', NULL, '2026-04-19 18:26:18');
INSERT INTO `assistant_message` VALUES (168, 81, 'assistant', '**结论**：已准备好修改品质等级的操作计划。\n\n**操作摘要**：\n- 目标对象：AI测试等级20260420\n- 新名称：AI测试等级20260420改\n- 最高分：96分\n- 排序值：104\n- 备注：AI测试品质等级更新\n- 状态：启用（1）\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022625532\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:29');
INSERT INTO `assistant_message` VALUES (169, 81, 'assistant', '已更新品质等级：AI测试等级20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022625532\",\"status\":\"EXECUTED\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已更新品质等级：AI测试等级20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:26:29');
INSERT INTO `assistant_message` VALUES (170, 82, 'user', '请把储存条件AI测试储存条件20260420修改为名称AI测试储存条件20260420改，通风要求密闭，排序值103，备注AI测试储存条件更新', 'TEXT', NULL, '2026-04-19 18:28:28');
INSERT INTO `assistant_message` VALUES (171, 82, 'assistant', '**结论**：修改储存条件的操作计划已准备就绪。\n\n**操作摘要**：\n- 目标对象：AI测试储存条件20260420\n- 新名称：AI测试储存条件20260420改\n- 通风要求：密闭\n- 排序值：103\n- 备注：AI测试储存条件更新\n- 其他字段保持原值：储存类型为常温，避光要求为无特殊要求，状态为启用\n\n请确认是否执行此修改操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022834488\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:28:38');
INSERT INTO `assistant_message` VALUES (172, 82, 'assistant', '已更新储存条件：AI测试储存条件20260420', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022834488\",\"status\":\"EXECUTED\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已更新储存条件：AI测试储存条件20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', '2026-04-19 18:28:38');
INSERT INTO `assistant_message` VALUES (173, 83, 'user', '请删除产品档案AI测试产品20260420改', 'TEXT', NULL, '2026-04-19 18:29:10');
INSERT INTO `assistant_message` VALUES (174, 83, 'assistant', '**结论：** 已准备好删除产品档案\"AI测试产品20260420改\"的操作计划。\n\n**关键记录摘要：**\n- 产品名称：AI测试产品20260420改\n- 商品编号：PROD-20260420022136260\n- 规格：100g/盒\n- 当前状态：启用\n\n**风险提示：** 删除操作风险等级为HIGH（高风险），删除后将无法恢复。\n\n请确认是否要删除该产品档案？如果确认删除，请输入\"确认删除\"。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260420022136260\",\"productName\":\"AI测试产品20260420改\",\"productSpecification\":\"100g/盒\",\"categoryName\":\"AI测试分类20260420改\",\"unitName\":\"AI测试箱20260420改\",\"originName\":\"AI测试产地20260420改\",\"storageConditionName\":\"AI测试储存条件20260420改\",\"qualityGradeName\":\"AI测试等级20260420改\",\"statusLabel\":\"启用\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420022919938\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产品20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420改\"}]}}', '2026-04-19 18:29:24');
INSERT INTO `assistant_message` VALUES (175, 83, 'assistant', '已删除产品档案：AI测试产品20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022919938\",\"status\":\"EXECUTED\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产品20260420改\",\"summary\":\"已删除产品档案：AI测试产品20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420改\"}]}}', '2026-04-19 18:29:24');
INSERT INTO `assistant_message` VALUES (176, 84, 'user', '请删除产品分类AI测试分类20260420改', 'TEXT', NULL, '2026-04-19 18:29:24');
INSERT INTO `assistant_message` VALUES (177, 84, 'assistant', '**结论：** 已准备好删除产品分类\"AI测试分类20260420改\"的操作计划。\n\n**关键记录摘要：**\n- 分类名称：AI测试分类20260420改\n- 分类编号：CAT-20260420021604938\n- 层级：1级分类\n- 状态：启用\n- 更新时间：2026-04-19 18:23:01\n\n**风险提示：** 删除操作风险等级为HIGH，请确认该分类下没有关联的产品档案。如果分类下有产品，删除操作可能会影响相关业务数据。\n\n请确认是否要删除该产品分类？如果确认删除，请输入\"确认删除\"。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260420021604938\",\"categoryName\":\"AI测试分类20260420改\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:23:01+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420022933397\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试分类20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420改\"}]}}', '2026-04-19 18:29:38');
INSERT INTO `assistant_message` VALUES (178, 84, 'assistant', '已删除产品分类：AI测试分类20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022933397\",\"status\":\"EXECUTED\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试分类20260420改\",\"summary\":\"已删除产品分类：AI测试分类20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420改\"}]}}', '2026-04-19 18:29:38');
INSERT INTO `assistant_message` VALUES (179, 85, 'user', '请删除品质等级AI测试等级20260420改', 'TEXT', NULL, '2026-04-19 18:29:38');
INSERT INTO `assistant_message` VALUES (180, 85, 'assistant', '**结论：** 已准备好删除\"AI测试等级20260420改\"品质等级的操作计划。\n\n**关键记录摘要：**\n- 找到目标品质等级：AI测试等级20260420改（等级编号：GRADE-20260420021545898）\n- 当前状态：启用\n- 更新时间：2026-04-19 18:26:29\n\n**风险提示：** 删除操作风险等级为HIGH，此操作不可逆。\n\n请确认是否要删除该品质等级？如果确认删除，请输入\"确认删除\"。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"品质等级查询\",\"summary\":\"品质等级查询：共匹配到 1 条记录\",\"routePath\":\"/quality-grades\",\"routeLabel\":\"品质等级管理\",\"columns\":[{\"key\":\"gradeCode\",\"label\":\"等级编号\"},{\"key\":\"gradeName\",\"label\":\"等级名称\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"gradeCode\":\"GRADE-20260420021545898\",\"gradeName\":\"AI测试等级20260420改\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:26:29+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420022948861\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试等级20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420改\"}]}}', '2026-04-19 18:29:53');
INSERT INTO `assistant_message` VALUES (181, 85, 'assistant', '已删除品质等级：AI测试等级20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022948861\",\"status\":\"EXECUTED\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试等级20260420改\",\"summary\":\"已删除品质等级：AI测试等级20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420改\"}]}}', '2026-04-19 18:29:53');
INSERT INTO `assistant_message` VALUES (182, 86, 'user', '请删除储存条件AI测试储存条件20260420改', 'TEXT', NULL, '2026-04-19 18:29:53');
INSERT INTO `assistant_message` VALUES (183, 86, 'assistant', '**结论：** 已找到储存条件\"AI测试储存条件20260420改\"，删除操作已准备就绪。\n\n**关键记录摘要：**\n- 条件编号：SC-20260420021537855\n- 条件名称：AI测试储存条件20260420改\n- 储存类型：常温\n- 避光要求：无特殊要求\n- 通风要求：密闭\n- 状态：启用\n\n**注意：** 删除操作风险等级为HIGH（高风险），请确认是否要删除此储存条件。如果确认删除，请输入\"确认删除\"来执行操作。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420改\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"密闭\",\"statusLabel\":\"启用\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420023002277\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试储存条件20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420改\"}]}}', '2026-04-19 18:30:07');
INSERT INTO `assistant_message` VALUES (184, 86, 'assistant', '已删除储存条件：AI测试储存条件20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023002277\",\"status\":\"EXECUTED\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试储存条件20260420改\",\"summary\":\"已删除储存条件：AI测试储存条件20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420改\"}]}}', '2026-04-19 18:30:07');
INSERT INTO `assistant_message` VALUES (185, 87, 'user', '请删除产地信息AI测试产地20260420改', 'TEXT', NULL, '2026-04-19 18:30:07');
INSERT INTO `assistant_message` VALUES (186, 87, 'assistant', '**结论**：已找到要删除的产地信息，删除操作已准备就绪。\n\n**关键记录摘要**：\n- 产地编号：ORI-20260420021529698\n- 产地名称：AI测试产地20260420改\n- 省份：安徽\n- 状态：启用\n- 更新时间：2026-04-19 18:26:07\n\n删除操作风险等级为HIGH（高风险），需要您明确确认。请确认是否要删除产地信息\"AI测试产地20260420改\"？如果确认删除，请输入\"确认删除\"。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420改\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:26:07+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420023016796\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产地20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420改\"}]}}', '2026-04-19 18:30:21');
INSERT INTO `assistant_message` VALUES (187, 87, 'assistant', '已删除产地信息：AI测试产地20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023016796\",\"status\":\"EXECUTED\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产地20260420改\",\"summary\":\"已删除产地信息：AI测试产地20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420改\"}]}}', '2026-04-19 18:30:21');
INSERT INTO `assistant_message` VALUES (188, 88, 'user', '请删除产品单位AI测试箱20260420改', 'TEXT', NULL, '2026-04-19 18:30:21');
INSERT INTO `assistant_message` VALUES (189, 88, 'assistant', '**结论：** 已找到产品单位\"AI测试箱20260420改\"，删除操作已准备就绪。\n\n**关键记录摘要：**\n- 单位编号：UNIT-20260420021433233\n- 单位名称：AI测试箱20260420改\n- 单位符号：ATX20B\n- 单位类型：包装单位\n- 状态：启用\n- 更新时间：2026-04-19 18:25:56\n\n**注意：** 删除操作风险等级为HIGH（高风险），请确认是否要删除此产品单位。如果确认删除，请输入\"确认删除\"来执行操作。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420021433233\",\"unitName\":\"AI测试箱20260420改\",\"unitSymbol\":\"ATX20B\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:25:56+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420023031197\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试箱20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420改\"}]}}', '2026-04-19 18:30:36');
INSERT INTO `assistant_message` VALUES (190, 88, 'assistant', '已删除产品单位：AI测试箱20260420改', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023031197\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试箱20260420改\",\"summary\":\"已删除产品单位：AI测试箱20260420改\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420改\"}]}}', '2026-04-19 18:30:36');
INSERT INTO `assistant_message` VALUES (197, 96, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-19 18:31:40');
INSERT INTO `assistant_message` VALUES (198, 96, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-19 18:31:40');
INSERT INTO `assistant_message` VALUES (201, 96, 'user', '新增一个海鲜分类', 'TEXT', NULL, '2026-04-19 18:43:23');
INSERT INTO `assistant_message` VALUES (202, 96, 'assistant', '**结论**：新增海鲜分类的计划已准备，但缺少必要信息。\n\n**关键摘要**：\n- 操作类型：新增产品分类\n- 目标分类：海鲜\n- 缺少字段：排序值、状态\n\n**请补充以下信息**：\n1. **排序值**：请提供一个数字，用于决定该分类在列表中的显示顺序。\n2. **状态**：请选择“启用”或“停用”。\n\n请提供这些信息，我将为您生成完整的操作确认卡片。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024327093\",\"status\":\"DRAFT\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海鲜\",\"summary\":\"还缺少以下字段：排序值、状态\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null},{\"field\":\"status\",\"label\":\"状态\",\"hint\":null}],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海鲜\"}]}}', '2026-04-19 18:43:35');
INSERT INTO `assistant_message` VALUES (203, 96, 'user', '排序值：1 状态：启用', 'TEXT', NULL, '2026-04-19 18:43:50');
INSERT INTO `assistant_message` VALUES (204, 96, 'assistant', '**结论**：海鲜分类新增计划已准备就绪，可以执行。\n\n**关键摘要**：\n- 操作类型：新增产品分类\n- 分类名称：海鲜\n- 排序值：1\n- 状态：启用\n- 操作计划编号：AWA-20260420024327093\n\n**请确认是否执行新增海鲜分类的操作？**', 'TEXT', NULL, '2026-04-19 18:43:54');
INSERT INTO `assistant_message` VALUES (205, 96, 'user', '确认', 'TEXT', NULL, '2026-04-19 18:43:59');
INSERT INTO `assistant_message` VALUES (206, 96, 'user', '确认', 'TEXT', NULL, '2026-04-19 18:44:27');
INSERT INTO `assistant_message` VALUES (207, 98, 'user', '请新增一个产品单位，单位名称是AI会话确认单位20260420，单位符号是ACU20，单位类型是包装单位，精度位数0，状态启用，排序值108，备注AI会话确认测试', 'TEXT', NULL, '2026-04-19 18:46:21');
INSERT INTO `assistant_message` VALUES (208, 98, 'assistant', '**结论：** 已准备好新增产品单位的操作计划。\n\n**操作摘要：**\n- 单位名称：AI会话确认单位20260420\n- 单位符号：ACU20  \n- 单位类型：包装单位\n- 精度位数：0\n- 状态：启用\n- 排序值：108\n- 备注：AI会话确认测试\n\n操作计划已准备就绪（状态：READY），请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024626610\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI会话确认单位20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ACU20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"108\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI会话确认测试\"}]}}', '2026-04-19 18:46:30');
INSERT INTO `assistant_message` VALUES (209, 98, 'user', '确认', 'TEXT', NULL, '2026-04-19 18:46:30');
INSERT INTO `assistant_message` VALUES (210, 98, 'assistant', '**结论：** 已成功新增产品单位。\n\n**执行结果：**\n- 单位名称：AI会话确认单位20260420\n- 单位符号：ACU20\n- 单位类型：包装单位\n- 精度位数：0\n- 状态：启用\n- 排序值：108\n- 备注：AI会话确认测试\n\n产品单位已成功添加到系统中。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024626610\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已新增产品单位：AI会话确认单位20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI会话确认单位20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ACU20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"108\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI会话确认测试\"}]}}', '2026-04-19 18:46:37');
INSERT INTO `assistant_message` VALUES (211, 99, 'user', '请删除产品单位AI会话确认单位20260420', 'TEXT', NULL, '2026-04-19 18:47:01');
INSERT INTO `assistant_message` VALUES (212, 99, 'assistant', '**结论：** 已准备好删除产品单位\"AI会话确认单位20260420\"的操作。\n\n**关键记录摘要：**\n- 单位编号：UNIT-20260420024634014\n- 单位名称：AI会话确认单位20260420\n- 单位符号：ACU20\n- 单位类型：包装单位\n- 当前状态：启用\n\n**风险提示：** 删除操作风险等级为HIGH，删除后数据将无法恢复。\n\n请确认是否要删除该产品单位？如需删除，请输入\"确认删除\"。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420024634014\",\"unitName\":\"AI会话确认单位20260420\",\"unitSymbol\":\"ACU20\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:46:34+08:00\"}]}],\"actionCard\":{\"actionCode\":\"AWA-20260420024710925\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI会话确认单位20260420\"}]}}', '2026-04-19 18:47:15');
INSERT INTO `assistant_message` VALUES (213, 99, 'user', '确认删除', 'TEXT', NULL, '2026-04-19 18:47:15');
INSERT INTO `assistant_message` VALUES (214, 99, 'assistant', '**结论：** 已成功删除产品单位\"AI会话确认单位20260420\"。\n\n**操作详情：**\n- 操作编号：AWA-20260420024710925\n- 操作类型：删除产品单位\n- 目标单位：AI会话确认单位20260420\n- 执行状态：已完成\n\n该产品单位已从系统中永久删除，相关数据无法恢复。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024710925\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已删除产品单位：AI会话确认单位20260420\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI会话确认单位20260420\"}]}}', '2026-04-19 18:47:22');
INSERT INTO `assistant_message` VALUES (215, 100, 'user', '新增海鲜分类', 'TEXT', NULL, '2026-04-19 18:51:52');
INSERT INTO `assistant_message` VALUES (216, 100, 'assistant', '**结论：** 需要补充信息才能完成海鲜分类的新增。\n\n**关键信息摘要：**\n- 操作类型：新增产品分类\n- 当前状态：草稿（缺少必要字段）\n- 缺少字段：分类名称、排序值、状态\n\n为了完成新增海鲜分类的操作，我需要您提供以下信息：\n1. **分类名称**：您已经提到\"海鲜\"，这是否就是您要设置的分类名称？\n2. **排序值**：这个分类在列表中的显示顺序（数字越小越靠前）\n3. **状态**：启用还是停用？\n\n请提供这些信息，我将为您生成完整的操作计划。', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"DRAFT\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":null,\"summary\":\"还缺少以下字段：分类名称、排序值、状态\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"hint\":null},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null},{\"field\":\"status\",\"label\":\"状态\",\"hint\":null}],\"previewFields\":[]}}', '2026-04-19 18:52:04');
INSERT INTO `assistant_message` VALUES (217, 100, 'user', '海产品、1、启用', 'TEXT', NULL, '2026-04-19 18:59:42');
INSERT INTO `assistant_message` VALUES (218, 100, 'assistant', '**结论：** 新增\"海产品\"分类的操作计划已准备就绪，请确认执行。\n\n**操作预览：**\n- **操作类型**：新增产品分类\n- **分类名称**：海产品\n- **排序值**：1\n- **状态**：启用\n\n请确认是否执行此新增操作？', 'RESULT', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海产品\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海产品\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"1\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"1\"}]}}', '2026-04-19 18:59:59');
INSERT INTO `assistant_message` VALUES (219, 100, 'assistant', '已新增产品分类：海产品', 'ACTION', '{\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"EXECUTED\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海产品\",\"summary\":\"已新增产品分类：海产品\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海产品\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"1\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"1\"}]}}', '2026-04-19 19:00:04');
INSERT INTO `assistant_message` VALUES (220, 100, 'user', '请查一下系统有哪些仓库', 'TEXT', NULL, '2026-04-19 21:20:44');
INSERT INTO `assistant_message` VALUES (221, 100, 'assistant', '**结论：** 系统中共有4个仓库，全部处于启用状态。\n\n**关键记录摘要：**\n1. **三里庵一号仓**（WH-20260419093735433）- 综合仓，负责人：小王\n2. **王岗一号仓库**（WH-20260420051525680）- 综合仓，负责人：王立\n3. **一号综合仓**（WH-202604190001）- 综合仓，负责人：王仓管\n4. **冷链中心仓**（WH-202604190002）- 冷藏仓，负责人：李冷链\n\n所有仓库的联系电话均已记录，如需查看更详细信息或进行其他操作，请告诉我。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 4 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-20260420051525680\",\"warehouseName\":\"王岗一号仓库\",\"warehouseType\":\"综合仓\",\"managerName\":\"王立\",\"contactPhone\":\"18685415685\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', '2026-04-19 21:20:57');
INSERT INTO `assistant_message` VALUES (222, 100, 'user', '请查一下鳜鱼的实时库存', 'TEXT', NULL, '2026-04-19 21:21:07');
INSERT INTO `assistant_message` VALUES (223, 100, 'assistant', '**结论：** 鳜鱼在系统中有1条实时库存记录，总库存量为100.0。\n\n**关键记录摘要：**\n- **商品名称**：鳜鱼\n- **仓库位置**：王岗一号仓库 → 冷链一号库区 → 冷链库区1-1库位\n- **库存状态**：\n  - 现存数量：100.0\n  - 预留数量：0.0\n  - 锁定数量：0.0\n  - 可用数量：100.0\n\n鳜鱼目前全部库存均为可用状态，无预留或锁定数量。', 'RESULT', '{\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260420051231855\",\"productName\":\"鳜鱼\",\"warehouseName\":\"王岗一号仓库\",\"zoneName\":\"冷链一号库区\",\"locationName\":\"冷链库区1-1库位\",\"stockQuantity\":\"100.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"100.0\"}]}],\"actionCard\":null}', '2026-04-19 21:21:19');
INSERT INTO `assistant_message` VALUES (224, 96, 'user', '请查一下最近的入库记录', 'TEXT', NULL, '2026-04-19 21:21:42');
INSERT INTO `assistant_message` VALUES (230, 112, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-20 05:41:00');
INSERT INTO `assistant_message` VALUES (231, 112, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-20 05:41:00');
INSERT INTO `assistant_message` VALUES (242, 122, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-20 05:43:47');
INSERT INTO `assistant_message` VALUES (243, 122, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-20 05:43:47');
INSERT INTO `assistant_message` VALUES (252, 131, 'user', '有哪些仓库', 'TEXT', NULL, '2026-04-20 06:38:56');
INSERT INTO `assistant_message` VALUES (253, 131, 'assistant', '正在为你整理仓库结果。', 'TEXT', NULL, '2026-04-20 06:38:56');

-- ----------------------------
-- Table structure for assistant_session
-- ----------------------------
DROP TABLE IF EXISTS `assistant_session`;
CREATE TABLE `assistant_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话标题',
  `route_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最近一次页面路由',
  `route_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最近一次页面标题',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1有效 0已归档',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_assistant_session_code`(`session_code` ASC) USING BTREE,
  INDEX `idx_assistant_session_user`(`user_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 133 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能助手会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assistant_session
-- ----------------------------
INSERT INTO `assistant_session` VALUES (7, 'AS-1776606946834', 1, '有哪些仓库', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 13:55:46', '2026-04-19 14:56:24');
INSERT INTO `assistant_session` VALUES (8, 'AS-1776611059372', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:04:19', '2026-04-19 15:04:19');
INSERT INTO `assistant_session` VALUES (9, 'AS-1776611135350', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:05:35', '2026-04-19 15:05:35');
INSERT INTO `assistant_session` VALUES (10, 'AS-1776611217406', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:06:57', '2026-04-19 15:06:57');
INSERT INTO `assistant_session` VALUES (12, 'AS-1776611297906', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:08:17', '2026-04-19 15:08:17');
INSERT INTO `assistant_session` VALUES (15, 'AS-1776611396753', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:09:56', '2026-04-19 15:09:56');
INSERT INTO `assistant_session` VALUES (18, 'AS-1776611584061', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 15:13:04', '2026-04-19 15:13:14');
INSERT INTO `assistant_session` VALUES (19, 'AS-1776613496945', 1, '查一下内酯豆腐库存记录', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 15:44:56', '2026-04-19 16:01:17');
INSERT INTO `assistant_session` VALUES (20, 'AS-1776614946707', 1, '查一下内酯豆腐的出库记录', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 16:09:06', '2026-04-19 16:09:13');
INSERT INTO `assistant_session` VALUES (22, 'AS-1776615093500', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 16:11:33', '2026-04-19 16:11:33');
INSERT INTO `assistant_session` VALUES (24, 'AS-1776615174785', 1, '查一下内酯豆腐的出库记录', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 16:12:54', '2026-04-19 16:13:03');
INSERT INTO `assistant_session` VALUES (25, 'AS-1776615549629', 1, '查一下内酯豆腐的出库记录', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 16:19:09', '2026-04-19 16:36:28');
INSERT INTO `assistant_session` VALUES (33, 'AS-1776616855496', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 16:40:55', '2026-04-19 16:40:55');
INSERT INTO `assistant_session` VALUES (35, 'AS-1776616919822', 1, '查一下内酯豆腐的出库记录', '/outbound-records', '出库记录查询', 1, '2026-04-19 16:41:59', '2026-04-19 16:42:09');
INSERT INTO `assistant_session` VALUES (36, 'AS-1776617348135', 1, '查一下内酯豆腐的出库记录', '/inventory-stocktakings', '库存盘点管理', 1, '2026-04-19 16:49:08', '2026-04-19 16:49:53');
INSERT INTO `assistant_session` VALUES (40, 'AS-1776617533605', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 16:52:13', '2026-04-19 16:52:13');
INSERT INTO `assistant_session` VALUES (42, 'AS-1776617654466', 1, '你好', '/outbound-records', '出库记录查询', 1, '2026-04-19 16:54:14', '2026-04-19 16:55:03');
INSERT INTO `assistant_session` VALUES (43, 'AS-1776617923294', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 16:58:43', '2026-04-19 16:58:58');
INSERT INTO `assistant_session` VALUES (44, 'AS-1776618674118', 1, '你好', '/outbound-records', '出库记录查询', 1, '2026-04-19 17:11:14', '2026-04-19 17:12:19');
INSERT INTO `assistant_session` VALUES (60, 'AS-1776622200550', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-19 18:10:00', '2026-04-19 18:10:00');
INSERT INTO `assistant_session` VALUES (62, 'AS-1776622295035', 1, '请新增一个产品单位，单位名称是助手测试盒，单位符...', '/product-units', '产品单位管理', 1, '2026-04-19 18:11:35', '2026-04-19 18:11:44');
INSERT INTO `assistant_session` VALUES (63, 'AS-1776622443504', 1, '请新增一个产品单位，单位名称是AI测试箱2026...', '/product-units', '产品单位管理', 1, '2026-04-19 18:14:03', '2026-04-19 18:14:11');
INSERT INTO `assistant_session` VALUES (64, 'AS-1776622514923', 1, '请新增一个产地信息，产地名称是AI测试产地202...', '/product-origins', '产地信息管理', 1, '2026-04-19 18:15:14', '2026-04-19 18:15:29');
INSERT INTO `assistant_session` VALUES (65, 'AS-1776622529728', 1, '请新增一个储存条件，条件名称是AI测试储存条件2...', '/storage-conditions', '储存条件管理', 1, '2026-04-19 18:15:29', '2026-04-19 18:15:37');
INSERT INTO `assistant_session` VALUES (66, 'AS-1776622537884', 1, '请新增一个品质等级，等级名称是AI测试等级202...', '/quality-grades', '品质等级管理', 1, '2026-04-19 18:15:37', '2026-04-19 18:15:45');
INSERT INTO `assistant_session` VALUES (67, 'AS-1776622545927', 1, '请新增一个产品分类，分类名称是AI测试分类202...', '/', '产品分类管理', 1, '2026-04-19 18:15:45', '2026-04-19 18:16:04');
INSERT INTO `assistant_session` VALUES (68, 'AS-1776622564973', 1, '请新增一个产品档案，产品名称是AI测试产品202...', '/product-archives', '产品档案管理', 1, '2026-04-19 18:16:04', '2026-04-19 18:16:16');
INSERT INTO `assistant_session` VALUES (69, 'AS-1776622616594', 1, '请新增一个产品档案，产品名称是AI测试产品202...', '/product-archives', '产品档案管理', 1, '2026-04-19 18:16:56', '2026-04-19 18:17:08');
INSERT INTO `assistant_session` VALUES (70, 'AS-1776622739360', 1, '请新增一个产品档案，产品名称是AI测试产品202...', '/product-archives', '产品档案管理', 1, '2026-04-19 18:18:59', '2026-04-19 18:19:14');
INSERT INTO `assistant_session` VALUES (71, 'AS-1776622863364', 1, '请新增一个产品档案，产品名称是AI测试产品202...', '/product-archives', '产品档案管理', 1, '2026-04-19 18:21:03', '2026-04-19 18:21:16');
INSERT INTO `assistant_session` VALUES (72, 'AS-1776622933032', 1, '请把产品单位AI测试箱20260420修改为名称...', '/product-units', '产品单位管理', 1, '2026-04-19 18:22:13', '2026-04-19 18:22:22');
INSERT INTO `assistant_session` VALUES (73, 'AS-1776622942892', 1, '请把产地信息AI测试产地20260420修改为名...', '/product-origins', '产地信息管理', 1, '2026-04-19 18:22:22', '2026-04-19 18:22:32');
INSERT INTO `assistant_session` VALUES (74, 'AS-1776622952663', 1, '请把储存条件AI测试储存条件20260420修改...', '/storage-conditions', '储存条件管理', 1, '2026-04-19 18:22:32', '2026-04-19 18:22:42');
INSERT INTO `assistant_session` VALUES (75, 'AS-1776622962677', 1, '请把品质等级AI测试等级20260420修改为名...', '/quality-grades', '品质等级管理', 1, '2026-04-19 18:22:42', '2026-04-19 18:22:52');
INSERT INTO `assistant_session` VALUES (76, 'AS-1776622972142', 1, '请把产品分类AI测试分类20260420修改为名...', '/', '产品分类管理', 1, '2026-04-19 18:22:52', '2026-04-19 18:23:01');
INSERT INTO `assistant_session` VALUES (77, 'AS-1776622981548', 1, '请把产品档案AI测试产品20260420修改为名...', '/product-archives', '产品档案管理', 1, '2026-04-19 18:23:01', '2026-04-19 18:23:11');
INSERT INTO `assistant_session` VALUES (78, 'AS-1776623146218', 1, '请把产品单位AI测试箱20260420修改为名称...', '/product-units', '产品单位管理', 1, '2026-04-19 18:25:46', '2026-04-19 18:25:56');
INSERT INTO `assistant_session` VALUES (79, 'AS-1776623156416', 1, '请把产地信息AI测试产地20260420修改为名...', '/product-origins', '产地信息管理', 1, '2026-04-19 18:25:56', '2026-04-19 18:26:07');
INSERT INTO `assistant_session` VALUES (80, 'AS-1776623167310', 1, '请把储存条件AI测试储存条件20260420修改...', '/storage-conditions', '储存条件管理', 1, '2026-04-19 18:26:07', '2026-04-19 18:26:18');
INSERT INTO `assistant_session` VALUES (81, 'AS-1776623178158', 1, '请把品质等级AI测试等级20260420修改为名...', '/quality-grades', '品质等级管理', 1, '2026-04-19 18:26:18', '2026-04-19 18:26:29');
INSERT INTO `assistant_session` VALUES (82, 'AS-1776623308659', 1, '请把储存条件AI测试储存条件20260420修改...', '/storage-conditions', '储存条件管理', 1, '2026-04-19 18:28:28', '2026-04-19 18:28:38');
INSERT INTO `assistant_session` VALUES (83, 'AS-1776623350054', 1, '请删除产品档案AI测试产品20260420改', '/product-archives', '产品档案管理', 1, '2026-04-19 18:29:10', '2026-04-19 18:29:24');
INSERT INTO `assistant_session` VALUES (84, 'AS-1776623364529', 1, '请删除产品分类AI测试分类20260420改', '/', '产品分类管理', 1, '2026-04-19 18:29:24', '2026-04-19 18:29:38');
INSERT INTO `assistant_session` VALUES (85, 'AS-1776623378803', 1, '请删除品质等级AI测试等级20260420改', '/quality-grades', '品质等级管理', 1, '2026-04-19 18:29:38', '2026-04-19 18:29:53');
INSERT INTO `assistant_session` VALUES (86, 'AS-1776623393804', 1, '请删除储存条件AI测试储存条件20260420改', '/storage-conditions', '储存条件管理', 1, '2026-04-19 18:29:53', '2026-04-19 18:30:07');
INSERT INTO `assistant_session` VALUES (87, 'AS-1776623407306', 1, '请删除产地信息AI测试产地20260420改', '/product-origins', '产地信息管理', 1, '2026-04-19 18:30:07', '2026-04-19 18:30:21');
INSERT INTO `assistant_session` VALUES (88, 'AS-1776623421505', 1, '请删除产品单位AI测试箱20260420改', '/product-units', '产品单位管理', 1, '2026-04-19 18:30:21', '2026-04-19 18:30:36');
INSERT INTO `assistant_session` VALUES (96, 'AS-1776623500902', 1, '有哪些仓库', '/product-archives', '产品档案管理', 1, '2026-04-19 18:31:40', '2026-04-19 18:43:54');
INSERT INTO `assistant_session` VALUES (98, 'AS-1776624381153', 1, '请新增一个产品单位，单位名称是AI会话确认单位2...', '/product-units', '产品单位管理', 1, '2026-04-19 18:46:21', '2026-04-19 18:46:37');
INSERT INTO `assistant_session` VALUES (99, 'AS-1776624421224', 1, '请删除产品单位AI会话确认单位20260420', '/product-units', '产品单位管理', 1, '2026-04-19 18:47:01', '2026-04-19 18:47:22');
INSERT INTO `assistant_session` VALUES (100, 'AS-1776624712460', 1, '新增海鲜分类', '/alerts', '预警中心', 1, '2026-04-19 18:51:52', '2026-04-19 21:21:19');
INSERT INTO `assistant_session` VALUES (112, 'AS-1776663660143', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-20 05:41:00', '2026-04-20 05:41:00');
INSERT INTO `assistant_session` VALUES (122, 'AS-1776663827772', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-20 05:43:47', '2026-04-20 05:43:47');
INSERT INTO `assistant_session` VALUES (131, 'AS-1776667136878', 1, '有哪些仓库', '/warehouses', '仓库信息管理', 1, '2026-04-20 06:38:56', '2026-04-20 06:38:56');

-- ----------------------------
-- Table structure for assistant_tool_audit
-- ----------------------------
DROP TABLE IF EXISTS `assistant_tool_audit`;
CREATE TABLE `assistant_tool_audit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工具审计ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `message_id` bigint NULL DEFAULT NULL COMMENT '触发该工具的用户消息ID',
  `tool_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具名称',
  `tool_arguments_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工具入参',
  `tool_result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工具结果摘要',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '执行结果：1成功 0失败',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assistant_tool_audit_session`(`session_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_assistant_tool_audit_message`(`message_id` ASC) USING BTREE,
  CONSTRAINT `fk_assistant_tool_audit_message` FOREIGN KEY (`message_id`) REFERENCES `assistant_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_assistant_tool_audit_session` FOREIGN KEY (`session_id`) REFERENCES `assistant_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能助手工具调用审计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assistant_tool_audit
-- ----------------------------
INSERT INTO `assistant_tool_audit` VALUES (3, 7, 11, 'query_warehouse_data', '{\"entityType\": \"warehouse\", \"limit\": 10}', '{\"toolName\":\"query_warehouse_data\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 13:55:50');
INSERT INTO `assistant_tool_audit` VALUES (4, 7, 15, 'query_inventory_data', '{\"entityType\": \"stock\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_inventory_data\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"stockQuantity\":\"45.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"45.0\"}]}]}', 1, '2026-04-19 14:56:19');
INSERT INTO `assistant_tool_audit` VALUES (7, 18, 36, 'query_warehouse_data', '{\"entityType\": \"warehouse\", \"limit\": 10}', '{\"toolName\":\"query_warehouse_data\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 15:13:07');
INSERT INTO `assistant_tool_audit` VALUES (8, 19, 38, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 15:45:00');
INSERT INTO `assistant_tool_audit` VALUES (9, 19, 40, 'query_inventory_data', '{\"entityType\": \"stock\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_inventory_data\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"stockQuantity\":\"45.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"45.0\"}]}]}', 1, '2026-04-19 16:01:12');
INSERT INTO `assistant_tool_audit` VALUES (10, 20, 42, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:09:10');
INSERT INTO `assistant_tool_audit` VALUES (12, 24, 50, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:12:59');
INSERT INTO `assistant_tool_audit` VALUES (13, 25, 52, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:19:13');
INSERT INTO `assistant_tool_audit` VALUES (14, 25, 54, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:24:53');
INSERT INTO `assistant_tool_audit` VALUES (21, 25, 62, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:36:24');
INSERT INTO `assistant_tool_audit` VALUES (28, 35, 76, 'query_outbound_data', '{\"entityType\": \"outbound_record\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_outbound_data\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', 1, '2026-04-19 16:42:04');
INSERT INTO `assistant_tool_audit` VALUES (29, 36, 78, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:49:12');
INSERT INTO `assistant_tool_audit` VALUES (30, 36, 78, 'query_outbound_data', '{\"entityType\": \"outbound_record\", \"keyword\": \"内酯豆腐\", \"limit\": 10}', '{\"toolName\":\"query_outbound_data\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', 1, '2026-04-19 16:49:15');
INSERT INTO `assistant_tool_audit` VALUES (31, 36, 79, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"内酯豆腐\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260419083640261\",\"productName\":\"内酯豆腐\",\"productSpecification\":\"350g/盒\",\"categoryName\":\"豆制品\",\"unitName\":\"盒\",\"originName\":\"合肥龙岗\",\"storageConditionName\":\"豆制品存储条件\",\"qualityGradeName\":\"一级\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:49:36');
INSERT INTO `assistant_tool_audit` VALUES (32, 36, 79, 'query_outbound_data', '{\"entityType\": \"outbound_record\", \"relatedKeyword\": \"PROD-20260419083640261\", \"limit\": 10}', '{\"toolName\":\"query_outbound_data\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', 1, '2026-04-19 16:49:41');
INSERT INTO `assistant_tool_audit` VALUES (38, 42, 93, 'query_outbound_data', '{\"entityType\": \"outbound_record\", \"limit\": 10}', '{\"toolName\":\"query_outbound_data\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"出库记录查询\",\"summary\":\"出库记录查询：共匹配到 1 条记录\",\"routePath\":\"/outbound-records\",\"routeLabel\":\"出库记录查询\",\"columns\":[{\"key\":\"recordCode\",\"label\":\"记录编号\"},{\"key\":\"outboundOrderCode\",\"label\":\"出库单编号\"},{\"key\":\"customerName\",\"label\":\"客户名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"quantity\",\"label\":\"数量\"},{\"key\":\"occurredAt\",\"label\":\"发生时间\"}],\"rows\":[{\"recordCode\":\"OR-202604190001\",\"outboundOrderCode\":\"OUT-202604190003\",\"customerName\":\"上海鲜享商超\",\"warehouseName\":\"三里庵一号仓\",\"zoneName\":\"冷藏一号库区\",\"locationName\":\"冷藏库区1-1\",\"productName\":\"内酯豆腐\",\"quantity\":\"5.0\",\"occurredAt\":\"2026-04-21T15:40+08:00\"}]}]}', 1, '2026-04-19 16:54:51');
INSERT INTO `assistant_tool_audit` VALUES (39, 43, 95, 'query_warehouse_data', '{\"entityType\": \"warehouse\", \"limit\": 10}', '{\"toolName\":\"query_warehouse_data\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 3 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}]}', 1, '2026-04-19 16:58:46');
INSERT INTO `assistant_tool_audit` VALUES (40, 44, 99, 'query_alert_message_data', '{\"entityType\": \"alert_record\", \"limit\": 10}', '{\"toolName\":\"query_alert_message_data\",\"summary\":\"预警记录查询：未找到匹配结果\",\"resultBlocks\":[{\"title\":\"预警记录查询\",\"summary\":\"预警记录查询：未找到匹配结果\",\"routePath\":\"/alerts\",\"routeLabel\":\"预警中心\",\"columns\":[],\"rows\":[]}]}', 1, '2026-04-19 17:11:33');
INSERT INTO `assistant_tool_audit` VALUES (41, 44, 101, 'query_basic_master_data', '{\"entityType\": \"category\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 8 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 8 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260419051005341\",\"categoryName\":\"豆制品\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"豆制品存储条件\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T21:10:05+08:00\"},{\"categoryCode\":\"CAT-A00\",\"categoryName\":\"新鲜蔬菜\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T19:00:01+08:00\"},{\"categoryCode\":\"CAT-A01\",\"categoryName\":\"叶菜类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A01-01\",\"categoryName\":\"菠菜\",\"categoryLevel\":\"3\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A01-02\",\"categoryName\":\"生菜\",\"categoryLevel\":\"3\",\"defaultStorageCondition\":\"叶菜冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-A02\",\"categoryName\":\"根茎类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"根茎阴凉干燥标准\",\"statusLabel\":\"停用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"},{\"categoryCode\":\"CAT-B00\",\"categoryName\":\"时令水果\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T19:00:01+08:00\"},{\"categoryCode\":\"CAT-B01\",\"categoryName\":\"柑橘类\",\"categoryLevel\":\"2\",\"defaultStorageCondition\":\"水果冷藏标准\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T20:51:10+08:00\"}]}]}', 1, '2026-04-19 17:12:07');
INSERT INTO `assistant_tool_audit` VALUES (47, 62, 113, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"CREATE\", \"fields\": {\"name\": \"助手测试盒\", \"symbol\": \"ZTH\", \"unitType\": \"包装单位\", \"precision\": 0, \"status\": \"启用\", \"sortOrder\": 91, \"remark\": \"用于助手新增测试\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021140316\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"助手测试盒\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"助手测试盒\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ZTH\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"91\"},{\"field\":\"remark\",\"label\":\"remark\",\"value\":\"用于助手新增测试\"}]}}', 1, '2026-04-19 18:11:40');
INSERT INTO `assistant_tool_audit` VALUES (48, 63, 116, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"CREATE\", \"fields\": {\"name\": \"AI测试箱20260420\", \"symbol\": \"ATX20\", \"unitType\": \"包装单位\", \"precision\": 0, \"status\": \"启用\", \"sortOrder\": 92, \"remark\": \"AI测试单位\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021408292\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位\"}]}}', 1, '2026-04-19 18:14:08');
INSERT INTO `assistant_tool_audit` VALUES (49, 64, 119, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_origin\", \"actionType\": \"CREATE\", \"fields\": {\"name\": \"AI测试产地20260420\", \"country\": \"中国\", \"province\": \"安徽\", \"city\": \"合肥\", \"status\": \"启用\", \"sort_order\": 93, \"remark\": \"AI测试产地\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"还缺少以下字段：排序值\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021520089\",\"status\":\"DRAFT\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"还缺少以下字段：排序值\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}],\"previewFields\":[{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"合肥\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sort_order\",\"label\":\"sort_order\",\"value\":\"93\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地\"}]}}', 1, '2026-04-19 18:15:20');
INSERT INTO `assistant_tool_audit` VALUES (50, 64, 119, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_origin\", \"actionType\": \"CREATE\", \"fields\": {\"originName\":\"AI测试产地20260420\",\"countryName\":\"中国\",\"provinceName\":\"安徽\",\"cityName\":\"合肥\",\"status\":\"1\",\"sortOrder\":93,\"remarks\":\"AI测试产地\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产地信息的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021526034\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好新增产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"合肥\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地\"}]}}', 1, '2026-04-19 18:15:26');
INSERT INTO `assistant_tool_audit` VALUES (51, 65, 122, 'prepare_basic_info_write_action', '{\"resourceType\": \"storage_condition\", \"actionType\": \"CREATE\", \"fields\": {\"name\": \"AI测试储存条件20260420\", \"storageType\": \"常温\", \"lightRequirement\": \"无特殊要求\", \"ventilationRequirement\": \"普通通风\", \"status\": \"启用\", \"sortOrder\": 94, \"remark\": \"AI测试储存条件\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增储存条件的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021534637\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好新增储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"普通通风\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件\"}]}}', 1, '2026-04-19 18:15:34');
INSERT INTO `assistant_tool_audit` VALUES (52, 66, 125, 'prepare_basic_info_write_action', '{\"resourceType\": \"quality_grade\", \"actionType\": \"CREATE\", \"fields\": {\"gradeName\": \"AI测试等级20260420\", \"minScore\": 90, \"maxScore\": 95, \"status\": \"启用\", \"sortOrder\": 95, \"remark\": \"AI测试等级\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增品质等级的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021542676\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好新增品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"minScore\",\"label\":\"minScore\",\"value\":\"90\"},{\"field\":\"maxScore\",\"label\":\"maxScore\",\"value\":\"95\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试等级\"}]}}', 1, '2026-04-19 18:15:42');
INSERT INTO `assistant_tool_audit` VALUES (53, 67, 128, 'query_basic_master_data', '{\"entityType\": \"category\", \"keyword\": \"豆制品\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260419051005341\",\"categoryName\":\"豆制品\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"豆制品存储条件\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-18T21:10:05+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:15:49');
INSERT INTO `assistant_tool_audit` VALUES (54, 67, 128, 'query_basic_master_data', '{\"entityType\": \"storage_condition\", \"keyword\": \"AI测试储存条件20260420\", \"limit\": 5}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:15:53');
INSERT INTO `assistant_tool_audit` VALUES (55, 67, 128, 'prepare_basic_info_write_action', '{\"resourceType\": \"category\", \"actionType\": \"CREATE\", \"fields\": {\"categoryName\": \"AI测试分类20260420\", \"parentCategoryCode\": \"CAT-20260419051005341\", \"defaultStorageConditionCode\": \"SC-20260420021537855\", \"shelfLifeDays\": 15, \"warningDays\": 3, \"status\": \"ENABLED\", \"sortOrder\": 96, \"remark\": \"AI测试分类\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021600207\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420\"},{\"field\":\"parentCategoryCode\",\"label\":\"parentCategoryCode\",\"value\":\"CAT-20260419051005341\"},{\"field\":\"defaultStorageConditionCode\",\"label\":\"defaultStorageConditionCode\",\"value\":\"SC-20260420021537855\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类\"}]}}', 1, '2026-04-19 18:16:00');
INSERT INTO `assistant_tool_audit` VALUES (56, 68, 131, 'query_basic_master_data', '{\"entityType\": \"category\", \"keyword\": \"AI测试分类20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260420021604938\",\"categoryName\":\"AI测试分类20260420\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:16:04+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:16:08');
INSERT INTO `assistant_tool_audit` VALUES (57, 68, 131, 'query_basic_master_data', '{\"entityType\": \"unit\", \"keyword\": \"AI测试箱20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420021433233\",\"unitName\":\"AI测试箱20260420\",\"unitSymbol\":\"ATX20\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:14:33+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:16:11');
INSERT INTO `assistant_tool_audit` VALUES (58, 68, 131, 'query_basic_master_data', '{\"entityType\": \"origin\", \"keyword\": \"AI测试产地20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:15:29+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:16:13');
INSERT INTO `assistant_tool_audit` VALUES (59, 68, 131, 'query_basic_master_data', '{\"entityType\": \"storage_condition\", \"keyword\": \"AI测试储存条件20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:16:16');
INSERT INTO `assistant_tool_audit` VALUES (60, 69, 133, 'query_basic_master_data', '{\"entityType\": \"category\", \"keyword\": \"AI测试分类20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260420021604938\",\"categoryName\":\"AI测试分类20260420\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:16:04+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:17:00');
INSERT INTO `assistant_tool_audit` VALUES (61, 69, 133, 'query_basic_master_data', '{\"entityType\": \"unit\", \"keyword\": \"AI测试箱20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420021433233\",\"unitName\":\"AI测试箱20260420\",\"unitSymbol\":\"ATX20\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:14:33+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:17:02');
INSERT INTO `assistant_tool_audit` VALUES (62, 69, 133, 'query_basic_master_data', '{\"entityType\": \"origin\", \"keyword\": \"AI测试产地20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:15:29+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:17:05');
INSERT INTO `assistant_tool_audit` VALUES (63, 69, 133, 'query_basic_master_data', '{\"entityType\": \"storage_condition\", \"keyword\": \"AI测试储存条件20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"普通通风\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:17:08');
INSERT INTO `assistant_tool_audit` VALUES (64, 70, 135, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_archive\", \"actionType\": \"CREATE\", \"fields\": {\"产品名称\": \"AI测试产品20260420\", \"产品规格\": \"100g/盒\", \"产品分类\": \"AI测试分类20260420\", \"产品单位\": \"AI测试箱20260420\", \"产地信息\": \"AI测试产地20260420\", \"储存条件\": \"AI测试储存条件20260420\", \"保质期天数\": 15, \"预警提前天数\": 3, \"品质等级\": \"AI测试等级20260420\", \"状态\": \"启用\", \"排序值\": 97, \"备注\": \"AI测试产品档案\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"还缺少以下字段：产品分类、产品单位、产地信息、预警提前天数、排序值\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420021905965\",\"status\":\"DRAFT\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"还缺少以下字段：产品分类、产品单位、产地信息、预警提前天数、排序值\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"categoryId\",\"label\":\"产品分类\",\"hint\":\"请提供名称或编号\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"hint\":\"请提供名称或编号\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"hint\":\"请提供名称或编号\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"hint\":null},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}],\"previewFields\":[{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420\"},{\"field\":\"产品规格\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"产品分类\",\"label\":\"产品分类\",\"value\":\"AI测试分类20260420\"},{\"field\":\"产品单位\",\"label\":\"产品单位\",\"value\":\"AI测试箱20260420\"},{\"field\":\"产地信息\",\"label\":\"产地信息\",\"value\":\"AI测试产地20260420\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"预警提前天数\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"97\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品档案\"}]}}', 1, '2026-04-19 18:19:05');
INSERT INTO `assistant_tool_audit` VALUES (65, 70, 135, 'query_basic_master_data', '{\"entityType\": \"category\", \"keyword\": \"AI测试分类20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260420021604938\",\"categoryName\":\"AI测试分类20260420\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:16:04+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:19:09');
INSERT INTO `assistant_tool_audit` VALUES (66, 70, 135, 'query_basic_master_data', '{\"entityType\": \"unit\", \"keyword\": \"AI测试箱20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420021433233\",\"unitName\":\"AI测试箱20260420\",\"unitSymbol\":\"ATX20\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:14:33+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:19:12');
INSERT INTO `assistant_tool_audit` VALUES (67, 70, 135, 'query_basic_master_data', '{\"entityType\": \"origin\", \"keyword\": \"AI测试产地20260420\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:15:29+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:19:14');
INSERT INTO `assistant_tool_audit` VALUES (68, 71, 137, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_archive\", \"actionType\": \"CREATE\", \"fields\": {\"产品名称\": \"AI测试产品20260420\", \"产品规格\": \"100g/盒\", \"产品分类\": \"AI测试分类20260420\", \"产品单位\": \"AI测试箱20260420\", \"产地信息\": \"AI测试产地20260420\", \"储存条件\": \"AI测试储存条件20260420\", \"保质期天数\": 15, \"预警提前天数\": 3, \"品质等级\": \"AI测试等级20260420\", \"状态\": \"启用\", \"排序值\": 97, \"备注\": \"AI测试产品档案\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品档案的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022110873\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已整理好新增产品档案的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"3\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"97\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品档案\"}]}}', 1, '2026-04-19 18:21:10');
INSERT INTO `assistant_tool_audit` VALUES (69, 72, 140, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"UPDATE\", \"target\": \"AI测试箱20260420\", \"fields\": {\"名称\": \"AI测试箱20260420改\", \"单位符号\": \"ATX20B\", \"排序值\": 101, \"备注\": \"AI测试单位更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022219020\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"92\"}]}}', 1, '2026-04-19 18:22:19');
INSERT INTO `assistant_tool_audit` VALUES (70, 73, 143, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_origin\", \"actionType\": \"UPDATE\", \"target\": \"AI测试产地20260420\", \"fields\": {\"名称\": \"AI测试产地20260420改\", \"城市名称\": \"芜湖\", \"排序值\": 102, \"备注\": \"AI测试产地更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022228373\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"93\"}]}}', 1, '2026-04-19 18:22:28');
INSERT INTO `assistant_tool_audit` VALUES (71, 74, 146, 'prepare_basic_info_write_action', '{\"resourceType\": \"storage_condition\", \"actionType\": \"UPDATE\", \"target\": \"AI测试储存条件20260420\", \"fields\": {\"名称\": \"AI测试储存条件20260420改\", \"通风要求\": \"密闭\", \"排序值\": 103, \"备注\": \"AI测试储存条件更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022238187\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"名称\",\"label\":\"名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"94\"}]}}', 1, '2026-04-19 18:22:38');
INSERT INTO `assistant_tool_audit` VALUES (72, 75, 149, 'prepare_basic_info_write_action', '{\"resourceType\": \"quality_grade\", \"actionType\": \"UPDATE\", \"target\": \"AI测试等级20260420\", \"fields\": {\"新名称\": \"AI测试等级20260420改\", \"最高分\": 96, \"排序值\": 104, \"备注\": \"AI测试品质等级更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022247891\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"95\"}]}}', 1, '2026-04-19 18:22:47');
INSERT INTO `assistant_tool_audit` VALUES (73, 76, 152, 'prepare_basic_info_write_action', '{\"resourceType\": \"category\", \"actionType\": \"UPDATE\", \"target\": \"AI测试分类20260420\", \"fields\": {\"分类名称\": \"AI测试分类20260420改\", \"预警天数\": 4, \"排序值\": 105, \"备注\": \"AI测试分类更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产品分类的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022256988\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试分类20260420\",\"summary\":\"已整理好修改产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420\"},{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"AI测试分类20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"105\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试分类更新\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"96\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"}]}}', 1, '2026-04-19 18:22:56');
INSERT INTO `assistant_tool_audit` VALUES (74, 77, 155, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_archive\", \"actionType\": \"UPDATE\", \"target\": \"AI测试产品20260420\", \"fields\": {\"产品名称\": \"AI测试产品20260420改\", \"预警提前天数\": 4, \"排序值\": 106, \"备注\": \"AI测试产品更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产品档案的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022306808\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产品20260420\",\"summary\":\"已整理好修改产品档案的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420\"},{\"field\":\"productName\",\"label\":\"产品名称\",\"value\":\"AI测试产品20260420改\"},{\"field\":\"warningDays\",\"label\":\"预警提前天数\",\"value\":\"4\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"106\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产品更新\"},{\"field\":\"productSpecification\",\"label\":\"产品规格\",\"value\":\"100g/盒\"},{\"field\":\"categoryId\",\"label\":\"产品分类\",\"value\":\"37\"},{\"field\":\"unitId\",\"label\":\"产品单位\",\"value\":\"40\"},{\"field\":\"originId\",\"label\":\"产地信息\",\"value\":\"35\"},{\"field\":\"storageConditionId\",\"label\":\"储存条件\",\"value\":\"43\"},{\"field\":\"shelfLifeDays\",\"label\":\"保质期天数\",\"value\":\"15\"},{\"field\":\"qualityGradeId\",\"label\":\"品质等级\",\"value\":\"34\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:23:06');
INSERT INTO `assistant_tool_audit` VALUES (75, 78, 158, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"UPDATE\", \"target\": \"AI测试箱20260420\", \"fields\": {\"name\": \"AI测试箱20260420改\", \"symbol\": \"ATX20B\", \"sortOrder\": 101, \"remark\": \"AI测试单位更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022551918\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试箱20260420\",\"summary\":\"已整理好修改产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420\"},{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI测试箱20260420改\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ATX20B\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"101\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试单位更新\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:25:51');
INSERT INTO `assistant_tool_audit` VALUES (76, 79, 161, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_origin\", \"actionType\": \"UPDATE\", \"target\": \"AI测试产地20260420\", \"fields\": {\"originName\": \"AI测试产地20260420改\", \"city\": \"芜湖\", \"sortOrder\": 102, \"remark\": \"AI测试产地更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022602709\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试产地20260420\",\"summary\":\"已整理好修改产地信息的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420\"},{\"field\":\"originName\",\"label\":\"产地名称\",\"value\":\"AI测试产地20260420改\"},{\"field\":\"cityName\",\"label\":\"城市名称\",\"value\":\"芜湖\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"102\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试产地更新\"},{\"field\":\"countryName\",\"label\":\"国家名称\",\"value\":\"中国\"},{\"field\":\"provinceName\",\"label\":\"省份名称\",\"value\":\"安徽\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:26:02');
INSERT INTO `assistant_tool_audit` VALUES (77, 80, 164, 'prepare_basic_info_write_action', '{\"resourceType\": \"storage_condition\", \"actionType\": \"UPDATE\", \"target\": \"AI测试储存条件20260420\", \"fields\": {\"新名称\": \"AI测试储存条件20260420改\", \"通风要求\": \"密闭\", \"排序值\": 103, \"备注\": \"AI测试储存条件更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022613331\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"新名称\",\"label\":\"新名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:26:13');
INSERT INTO `assistant_tool_audit` VALUES (78, 81, 167, 'prepare_basic_info_write_action', '{\"resourceType\": \"quality_grade\", \"actionType\": \"UPDATE\", \"target\": \"AI测试等级20260420\", \"fields\": {\"名称\": \"AI测试等级20260420改\", \"最高分\": 96, \"排序值\": 104, \"备注\": \"AI测试品质等级更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022625532\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试等级20260420\",\"summary\":\"已整理好修改品质等级的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420\"},{\"field\":\"gradeName\",\"label\":\"等级名称\",\"value\":\"AI测试等级20260420改\"},{\"field\":\"scoreMax\",\"label\":\"最高分\",\"value\":\"96\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"104\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试品质等级更新\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:26:25');
INSERT INTO `assistant_tool_audit` VALUES (79, 82, 170, 'prepare_basic_info_write_action', '{\"resourceType\": \"storage_condition\", \"actionType\": \"UPDATE\", \"target\": \"AI测试储存条件20260420\", \"fields\": {\"名称\": \"AI测试储存条件20260420改\", \"通风要求\": \"密闭\", \"排序值\": 103, \"备注\": \"AI测试储存条件更新\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022834488\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"UPDATE\",\"actionLabel\":\"修改\",\"targetLabel\":\"AI测试储存条件20260420\",\"summary\":\"已整理好修改储存条件的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420\"},{\"field\":\"conditionName\",\"label\":\"条件名称\",\"value\":\"AI测试储存条件20260420改\"},{\"field\":\"ventilationRequirement\",\"label\":\"通风要求\",\"value\":\"密闭\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"103\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI测试储存条件更新\"},{\"field\":\"storageType\",\"label\":\"储存类型\",\"value\":\"常温\"},{\"field\":\"lightRequirement\",\"label\":\"避光要求\",\"value\":\"无特殊要求\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:28:34');
INSERT INTO `assistant_tool_audit` VALUES (80, 83, 173, 'query_basic_master_data', '{\"entityType\": \"product\", \"keyword\": \"AI测试产品20260420改\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品档案查询\",\"summary\":\"产品档案查询：共匹配到 1 条记录\",\"routePath\":\"/product-archives\",\"routeLabel\":\"产品档案管理\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"productSpecification\",\"label\":\"规格\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"storageConditionName\",\"label\":\"储存条件\"},{\"key\":\"qualityGradeName\",\"label\":\"quality Grade Name\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"productCode\":\"PROD-20260420022136260\",\"productName\":\"AI测试产品20260420改\",\"productSpecification\":\"100g/盒\",\"categoryName\":\"AI测试分类20260420改\",\"unitName\":\"AI测试箱20260420改\",\"originName\":\"AI测试产地20260420改\",\"storageConditionName\":\"AI测试储存条件20260420改\",\"qualityGradeName\":\"AI测试等级20260420改\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:29:15');
INSERT INTO `assistant_tool_audit` VALUES (81, 83, 173, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_archive\", \"actionType\": \"DELETE\", \"target\": \"AI测试产品20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022919938\",\"status\":\"READY\",\"resourceType\":\"product_archive\",\"resourceLabel\":\"产品档案\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产品20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产品20260420改\"}]}}', 1, '2026-04-19 18:29:19');
INSERT INTO `assistant_tool_audit` VALUES (82, 84, 176, 'query_basic_master_data', '{\"entityType\": \"category\", \"keyword\": \"AI测试分类20260420改\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品分类查询\",\"summary\":\"产品分类查询：共匹配到 1 条记录\",\"routePath\":\"/\",\"routeLabel\":\"产品分类管理\",\"columns\":[{\"key\":\"categoryCode\",\"label\":\"分类编号\"},{\"key\":\"categoryName\",\"label\":\"分类名称\"},{\"key\":\"categoryLevel\",\"label\":\"层级\"},{\"key\":\"defaultStorageCondition\",\"label\":\"default Storage Condition\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"categoryCode\":\"CAT-20260420021604938\",\"categoryName\":\"AI测试分类20260420改\",\"categoryLevel\":\"1\",\"defaultStorageCondition\":\"\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:23:01+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:29:29');
INSERT INTO `assistant_tool_audit` VALUES (83, 84, 176, 'prepare_basic_info_write_action', '{\"resourceType\": \"category\", \"actionType\": \"DELETE\", \"target\": \"AI测试分类20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022933397\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试分类20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试分类20260420改\"}]}}', 1, '2026-04-19 18:29:33');
INSERT INTO `assistant_tool_audit` VALUES (84, 85, 179, 'query_basic_master_data', '{\"entityType\": \"quality_grade\", \"keyword\": \"AI测试等级20260420改\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"品质等级查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"品质等级查询\",\"summary\":\"品质等级查询：共匹配到 1 条记录\",\"routePath\":\"/quality-grades\",\"routeLabel\":\"品质等级管理\",\"columns\":[{\"key\":\"gradeCode\",\"label\":\"等级编号\"},{\"key\":\"gradeName\",\"label\":\"等级名称\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"gradeCode\":\"GRADE-20260420021545898\",\"gradeName\":\"AI测试等级20260420改\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:26:29+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:29:43');
INSERT INTO `assistant_tool_audit` VALUES (85, 85, 179, 'prepare_basic_info_write_action', '{\"resourceType\": \"quality_grade\", \"actionType\": \"DELETE\", \"target\": \"AI测试等级20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420022948861\",\"status\":\"READY\",\"resourceType\":\"quality_grade\",\"resourceLabel\":\"品质等级\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试等级20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试等级20260420改\"}]}}', 1, '2026-04-19 18:29:48');
INSERT INTO `assistant_tool_audit` VALUES (86, 86, 182, 'query_basic_master_data', '{\"entityType\": \"storage_condition\", \"keyword\": \"AI测试储存条件20260420改\"}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"储存条件查询\",\"summary\":\"储存条件查询：共匹配到 1 条记录\",\"routePath\":\"/storage-conditions\",\"routeLabel\":\"储存条件管理\",\"columns\":[{\"key\":\"conditionCode\",\"label\":\"条件编号\"},{\"key\":\"conditionName\",\"label\":\"条件名称\"},{\"key\":\"storageType\",\"label\":\"储存类型\"},{\"key\":\"lightRequirement\",\"label\":\"避光要求\"},{\"key\":\"ventilationRequirement\",\"label\":\"通风要求\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"conditionCode\":\"SC-20260420021537855\",\"conditionName\":\"AI测试储存条件20260420改\",\"storageType\":\"常温\",\"lightRequirement\":\"无特殊要求\",\"ventilationRequirement\":\"密闭\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:29:57');
INSERT INTO `assistant_tool_audit` VALUES (87, 86, 182, 'prepare_basic_info_write_action', '{\"resourceType\": \"storage_condition\", \"actionType\": \"DELETE\", \"target\": \"AI测试储存条件20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023002277\",\"status\":\"READY\",\"resourceType\":\"storage_condition\",\"resourceLabel\":\"储存条件\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试储存条件20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试储存条件20260420改\"}]}}', 1, '2026-04-19 18:30:02');
INSERT INTO `assistant_tool_audit` VALUES (88, 87, 185, 'query_basic_master_data', '{\"entityType\": \"origin\", \"keyword\": \"AI测试产地20260420改\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产地信息查询\",\"summary\":\"产地信息查询：共匹配到 1 条记录\",\"routePath\":\"/product-origins\",\"routeLabel\":\"产地信息管理\",\"columns\":[{\"key\":\"originCode\",\"label\":\"产地编号\"},{\"key\":\"originName\",\"label\":\"产地名称\"},{\"key\":\"provinceName\",\"label\":\"省份\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"originCode\":\"ORI-20260420021529698\",\"originName\":\"AI测试产地20260420改\",\"provinceName\":\"安徽\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:26:07+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:30:12');
INSERT INTO `assistant_tool_audit` VALUES (89, 87, 185, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_origin\", \"actionType\": \"DELETE\", \"target\": \"AI测试产地20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023016796\",\"status\":\"READY\",\"resourceType\":\"product_origin\",\"resourceLabel\":\"产地信息\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试产地20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试产地20260420改\"}]}}', 1, '2026-04-19 18:30:16');
INSERT INTO `assistant_tool_audit` VALUES (90, 88, 188, 'query_basic_master_data', '{\"entityType\": \"unit\", \"keyword\": \"AI测试箱20260420改\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420021433233\",\"unitName\":\"AI测试箱20260420改\",\"unitSymbol\":\"ATX20B\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:25:56+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:30:26');
INSERT INTO `assistant_tool_audit` VALUES (91, 88, 188, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"DELETE\", \"target\": \"AI测试箱20260420改\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420023031197\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI测试箱20260420改\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI测试箱20260420改\"}]}}', 1, '2026-04-19 18:30:31');
INSERT INTO `assistant_tool_audit` VALUES (97, 96, 201, 'prepare_basic_info_write_action', '{\"resourceType\": \"category\", \"actionType\": \"CREATE\", \"fields\": {\"分类名称\": \"海鲜\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"还缺少以下字段：排序值、状态\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024327093\",\"status\":\"DRAFT\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海鲜\",\"summary\":\"还缺少以下字段：排序值、状态\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null},{\"field\":\"status\",\"label\":\"状态\",\"hint\":null}],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海鲜\"}]}}', 1, '2026-04-19 18:43:27');
INSERT INTO `assistant_tool_audit` VALUES (98, 98, 207, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"CREATE\", \"fields\": {\"单位名称\": \"AI会话确认单位20260420\", \"单位符号\": \"ACU20\", \"单位类型\": \"包装单位\", \"精度位数\": 0, \"状态\": \"启用\", \"排序值\": 108, \"备注\": \"AI会话确认测试\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024626610\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已整理好新增产品单位的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI会话确认单位20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ACU20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"108\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI会话确认测试\"}]}}', 1, '2026-04-19 18:46:26');
INSERT INTO `assistant_tool_audit` VALUES (99, 98, 209, 'execute_basic_info_write_action', '{\"actionCode\": \"AWA-20260420024626610\", \"confirmationText\": \"确认执行新增产品单位操作\"}', '{\"toolName\":\"execute_basic_info_write_action\",\"summary\":\"已新增产品单位：AI会话确认单位20260420\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024626610\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已新增产品单位：AI会话确认单位20260420\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"unitName\",\"label\":\"单位名称\",\"value\":\"AI会话确认单位20260420\"},{\"field\":\"unitSymbol\",\"label\":\"单位符号\",\"value\":\"ACU20\"},{\"field\":\"unitType\",\"label\":\"单位类型\",\"value\":\"包装单位\"},{\"field\":\"precisionDigits\",\"label\":\"精度位数\",\"value\":\"0\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"108\"},{\"field\":\"remarks\",\"label\":\"备注\",\"value\":\"AI会话确认测试\"}]}}', 1, '2026-04-19 18:46:34');
INSERT INTO `assistant_tool_audit` VALUES (100, 99, 211, 'query_basic_master_data', '{\"entityType\": \"unit\", \"keyword\": \"AI会话确认单位20260420\", \"limit\": 10}', '{\"toolName\":\"query_basic_master_data\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"产品单位查询\",\"summary\":\"产品单位查询：共匹配到 1 条记录\",\"routePath\":\"/product-units\",\"routeLabel\":\"产品单位管理\",\"columns\":[{\"key\":\"unitCode\",\"label\":\"单位编号\"},{\"key\":\"unitName\",\"label\":\"单位名称\"},{\"key\":\"unitSymbol\",\"label\":\"单位符号\"},{\"key\":\"unitType\",\"label\":\"单位类型\"},{\"key\":\"statusLabel\",\"label\":\"状态\"},{\"key\":\"updatedAt\",\"label\":\"更新时间\"}],\"rows\":[{\"unitCode\":\"UNIT-20260420024634014\",\"unitName\":\"AI会话确认单位20260420\",\"unitSymbol\":\"ACU20\",\"unitType\":\"包装单位\",\"statusLabel\":\"启用\",\"updatedAt\":\"2026-04-19T18:46:34+08:00\"}]}],\"actionCard\":null}', 1, '2026-04-19 18:47:06');
INSERT INTO `assistant_tool_audit` VALUES (101, 99, 211, 'prepare_basic_info_write_action', '{\"resourceType\": \"product_unit\", \"actionType\": \"DELETE\", \"target\": \"AI会话确认单位20260420\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024710925\",\"status\":\"READY\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已整理好删除操作预览，请输入“确认删除”后执行。\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI会话确认单位20260420\"}]}}', 1, '2026-04-19 18:47:10');
INSERT INTO `assistant_tool_audit` VALUES (102, 99, 213, 'execute_basic_info_write_action', '{\"actionCode\": \"AWA-20260420024710925\", \"confirmationText\": \"确认删除\"}', '{\"toolName\":\"execute_basic_info_write_action\",\"summary\":\"已删除产品单位：AI会话确认单位20260420\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420024710925\",\"status\":\"EXECUTED\",\"resourceType\":\"product_unit\",\"resourceLabel\":\"产品单位\",\"actionType\":\"DELETE\",\"actionLabel\":\"删除\",\"targetLabel\":\"AI会话确认单位20260420\",\"summary\":\"已删除产品单位：AI会话确认单位20260420\",\"riskLevel\":\"HIGH\",\"confirmationMode\":\"CONFIRM_DELETE_TEXT\",\"confirmationTextHint\":\"请输入“确认删除”\",\"missingFields\":[],\"previewFields\":[{\"field\":\"target\",\"label\":\"目标对象\",\"value\":\"AI会话确认单位20260420\"}]}}', 1, '2026-04-19 18:47:19');
INSERT INTO `assistant_tool_audit` VALUES (103, 100, 215, 'prepare_basic_info_write_action', '{\"resourceType\": \"category\", \"actionType\": \"CREATE\", \"fields\": {}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"还缺少以下字段：分类名称、排序值、状态\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"DRAFT\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":null,\"summary\":\"还缺少以下字段：分类名称、排序值、状态\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"hint\":null},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null},{\"field\":\"status\",\"label\":\"状态\",\"hint\":null}],\"previewFields\":[]}}', 1, '2026-04-19 18:51:56');
INSERT INTO `assistant_tool_audit` VALUES (104, 100, 217, 'prepare_basic_info_write_action', '{\"actionCode\": \"AWA-20260420025156201\", \"resourceType\": \"category\", \"actionType\": \"CREATE\", \"fields\": {\"分类名称\": \"海产品\", \"排序值\": 1, \"状态\": \"启用\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"还缺少以下字段：排序值\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"DRAFT\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海产品\",\"summary\":\"还缺少以下字段：排序值\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[{\"field\":\"sortOrder\",\"label\":\"排序值\",\"hint\":null}],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海产品\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"1\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:59:47');
INSERT INTO `assistant_tool_audit` VALUES (105, 100, 217, 'prepare_basic_info_write_action', '{\"actionCode\": \"AWA-20260420025156201\", \"resourceType\": \"category\", \"actionType\": \"CREATE\", \"fields\": {\"分类名称\":\"海产品\",\"sortOrder\":1,\"状态\":\"启用\"}}', '{\"toolName\":\"prepare_basic_info_write_action\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"resultBlocks\":[],\"actionCard\":{\"actionCode\":\"AWA-20260420025156201\",\"status\":\"READY\",\"resourceType\":\"category\",\"resourceLabel\":\"产品分类\",\"actionType\":\"CREATE\",\"actionLabel\":\"新增\",\"targetLabel\":\"海产品\",\"summary\":\"已整理好新增产品分类的操作预览，请确认执行。\",\"riskLevel\":\"MEDIUM\",\"confirmationMode\":\"CONFIRM_CARD\",\"confirmationTextHint\":null,\"missingFields\":[],\"previewFields\":[{\"field\":\"categoryName\",\"label\":\"分类名称\",\"value\":\"海产品\"},{\"field\":\"排序值\",\"label\":\"排序值\",\"value\":\"1\"},{\"field\":\"status\",\"label\":\"状态\",\"value\":\"1\"},{\"field\":\"sortOrder\",\"label\":\"排序值\",\"value\":\"1\"}]}}', 1, '2026-04-19 18:59:54');
INSERT INTO `assistant_tool_audit` VALUES (106, 100, 220, 'query_warehouse_data', '{\"entityType\": \"warehouse\", \"limit\": 10}', '{\"toolName\":\"query_warehouse_data\",\"summary\":\"仓库查询：共匹配到 4 条记录\",\"resultBlocks\":[{\"title\":\"仓库查询\",\"summary\":\"仓库查询：共匹配到 4 条记录\",\"routePath\":\"/warehouses\",\"routeLabel\":\"仓库信息管理\",\"columns\":[{\"key\":\"warehouseCode\",\"label\":\"仓库编号\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"warehouseType\",\"label\":\"仓库类型\"},{\"key\":\"managerName\",\"label\":\"manager Name\"},{\"key\":\"contactPhone\",\"label\":\"联系电话\"},{\"key\":\"statusLabel\",\"label\":\"状态\"}],\"rows\":[{\"warehouseCode\":\"WH-20260419093735433\",\"warehouseName\":\"三里庵一号仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"小王\",\"contactPhone\":\"15245851252\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-20260420051525680\",\"warehouseName\":\"王岗一号仓库\",\"warehouseType\":\"综合仓\",\"managerName\":\"王立\",\"contactPhone\":\"18685415685\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190001\",\"warehouseName\":\"一号综合仓\",\"warehouseType\":\"综合仓\",\"managerName\":\"王仓管\",\"contactPhone\":\"13800000011\",\"statusLabel\":\"启用\"},{\"warehouseCode\":\"WH-202604190002\",\"warehouseName\":\"冷链中心仓\",\"warehouseType\":\"冷藏仓\",\"managerName\":\"李冷链\",\"contactPhone\":\"13800000012\",\"statusLabel\":\"启用\"}]}],\"actionCard\":null}', 1, '2026-04-19 21:20:47');
INSERT INTO `assistant_tool_audit` VALUES (107, 100, 222, 'query_inventory_data', '{\"entityType\": \"stock\", \"keyword\": \"鳜鱼\", \"limit\": 10}', '{\"toolName\":\"query_inventory_data\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"resultBlocks\":[{\"title\":\"实时库存查询\",\"summary\":\"实时库存查询：共匹配到 1 条记录\",\"routePath\":\"/inventory-stocks\",\"routeLabel\":\"实时库存查询\",\"columns\":[{\"key\":\"productCode\",\"label\":\"商品编号\"},{\"key\":\"productName\",\"label\":\"商品名称\"},{\"key\":\"warehouseName\",\"label\":\"仓库名称\"},{\"key\":\"zoneName\",\"label\":\"库区名称\"},{\"key\":\"locationName\",\"label\":\"库位名称\"},{\"key\":\"stockQuantity\",\"label\":\"现存数量\"},{\"key\":\"reservedQuantity\",\"label\":\"预留数量\"},{\"key\":\"lockedQuantity\",\"label\":\"锁定数量\"},{\"key\":\"availableQuantity\",\"label\":\"可用数量\"}],\"rows\":[{\"productCode\":\"PROD-20260420051231855\",\"productName\":\"鳜鱼\",\"warehouseName\":\"王岗一号仓库\",\"zoneName\":\"冷链一号库区\",\"locationName\":\"冷链库区1-1库位\",\"stockQuantity\":\"100.0\",\"reservedQuantity\":\"0.0\",\"lockedQuantity\":\"0.0\",\"availableQuantity\":\"100.0\"}]}],\"actionCard\":null}', 1, '2026-04-19 21:21:10');

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户编号',
  `customer_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `customer_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户类型',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `region_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在地区',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_customer_code`(`customer_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_customer_name`(`customer_name` ASC) USING BTREE,
  INDEX `idx_customer_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer
-- ----------------------------
INSERT INTO `customer` VALUES (1, 'CUS-202604190001', '华北社区采购中心', '批发客户', '赵采购', '13800000401', '北京市朝阳区', '社区团购园区1号', 10, 1, '社区团购主要客户', '2026-04-19 02:34:25', '2026-04-19 02:34:25');
INSERT INTO `customer` VALUES (2, 'CUS-202604190002', '上海鲜享商超', '商超客户', '周经理', '13800000402', '上海市浦东新区', '商超配送中心2号', 20, 1, '连锁商超客户', '2026-04-19 02:34:25', '2026-04-19 02:34:25');

-- ----------------------------
-- Table structure for inbound_order
-- ----------------------------
DROP TABLE IF EXISTS `inbound_order`;
CREATE TABLE `inbound_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `order_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `expected_arrival_at` datetime NOT NULL COMMENT '预计到货时间',
  `actual_arrival_at` datetime NULL DEFAULT NULL COMMENT '实际到货时间',
  `total_item_count` int NOT NULL DEFAULT 0 COMMENT '商品条数',
  `total_quantity` decimal(14, 3) NOT NULL DEFAULT 0.000 COMMENT '总数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1待到货，2待上架，3已完成，4已取消',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbound_order_code`(`order_code` ASC) USING BTREE,
  INDEX `idx_inbound_order_supplier`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_inbound_order_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_inbound_order_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_inbound_order_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_order_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inbound_order
-- ----------------------------
INSERT INTO `inbound_order` VALUES (1, 'IN-202604190001', 1, 1, '2026-04-20 09:00:00', NULL, 1, 100.000, 1, '叶菜类到货计划', '2026-04-19 02:58:36', '2026-04-19 02:58:36');
INSERT INTO `inbound_order` VALUES (6, 'IN-202604190002', 2, 2, '2026-04-20 10:00:00', '2026-04-20 09:30:00', 1, 48.000, 2, '待上架示例单', '2026-04-19 03:26:09', '2026-04-19 03:26:09');
INSERT INTO `inbound_order` VALUES (16, 'IN-202604190003', 2, 2, '2026-04-20 11:00:00', '2026-04-20 10:30:00', 1, 36.000, 3, '已完成入库示例', '2026-04-19 03:56:42', '2026-04-19 03:56:42');
INSERT INTO `inbound_order` VALUES (25, 'IN-20260419121010769', 9, 11, '2026-04-19 14:00:00', '2026-04-19 12:10:29', 1, 50.000, 3, '测试一下备注', '2026-04-19 04:10:10', '2026-04-19 06:26:50');
INSERT INTO `inbound_order` VALUES (70, 'IN-20260420051657615', 1, 34, '2026-04-21 05:16:00', '2026-04-20 05:17:03', 1, 100.000, 3, NULL, '2026-04-19 21:16:57', '2026-04-19 21:17:15');
INSERT INTO `inbound_order` VALUES (98, 'IN-20260420152213920', 1, 11, '2026-04-20 15:22:00', '2026-04-20 15:22:36', 1, 9.000, 3, NULL, '2026-04-20 07:22:13', '2026-04-20 07:22:44');
INSERT INTO `inbound_order` VALUES (99, 'IN-20260420152431329', 1, 11, '2026-04-20 15:24:00', '2026-04-20 15:24:35', 1, 1.000, 3, NULL, '2026-04-20 07:24:31', '2026-04-20 07:24:42');
INSERT INTO `inbound_order` VALUES (100, 'IN-20260420155216058', 1, 11, '2026-04-20 15:51:00', '2026-04-20 15:52:21', 1, 10.000, 2, NULL, '2026-04-20 07:52:16', '2026-04-20 07:52:20');

-- ----------------------------
-- Table structure for inbound_order_item
-- ----------------------------
DROP TABLE IF EXISTS `inbound_order_item`;
CREATE TABLE `inbound_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库单明细ID',
  `inbound_order_id` bigint NOT NULL COMMENT '入库单ID',
  `product_id` bigint NOT NULL COMMENT '产品档案ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '入库数量',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inbound_order_item_order`(`inbound_order_id` ASC) USING BTREE,
  INDEX `idx_inbound_order_item_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_inbound_order_item_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inbound_order_item
-- ----------------------------
INSERT INTO `inbound_order_item` VALUES (1, 1, 1, 100.000, 1, '首批到货', '2026-04-19 02:58:36', '2026-04-19 02:58:36');
INSERT INTO `inbound_order_item` VALUES (6, 6, 3, 48.000, 1, '待上架示例明细', '2026-04-19 03:26:09', '2026-04-19 03:26:09');
INSERT INTO `inbound_order_item` VALUES (16, 16, 3, 36.000, 1, '已完成入库示例明细', '2026-04-19 03:56:42', '2026-04-19 03:56:42');
INSERT INTO `inbound_order_item` VALUES (25, 25, 12, 50.000, 1, '新增一个内酯豆腐', '2026-04-19 04:10:10', '2026-04-19 06:26:50');
INSERT INTO `inbound_order_item` VALUES (70, 70, 44, 100.000, 1, NULL, '2026-04-19 21:16:57', '2026-04-19 21:16:57');
INSERT INTO `inbound_order_item` VALUES (98, 98, 58, 9.000, 1, NULL, '2026-04-20 07:22:13', '2026-04-20 07:22:13');
INSERT INTO `inbound_order_item` VALUES (99, 99, 58, 1.000, 1, NULL, '2026-04-20 07:24:31', '2026-04-20 07:24:31');
INSERT INTO `inbound_order_item` VALUES (100, 100, 58, 10.000, 1, NULL, '2026-04-20 07:52:16', '2026-04-20 07:52:16');

-- ----------------------------
-- Table structure for inbound_record
-- ----------------------------
DROP TABLE IF EXISTS `inbound_record`;
CREATE TABLE `inbound_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库记录ID',
  `record_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '记录编号',
  `inbound_order_id` bigint NOT NULL COMMENT '入库单ID',
  `putaway_task_id` bigint NOT NULL COMMENT '上架任务ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '入库数量',
  `shelf_life_days_snapshot` int NULL DEFAULT NULL COMMENT '入库时保质期天数快照',
  `warning_days_snapshot` int NULL DEFAULT NULL COMMENT '入库时预警提前天数快照',
  `expected_expire_at` datetime NULL DEFAULT NULL COMMENT '预计到期时间',
  `occurred_at` datetime NOT NULL COMMENT '入库时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inbound_record_code`(`record_code` ASC) USING BTREE,
  INDEX `idx_inbound_record_order`(`inbound_order_id` ASC) USING BTREE,
  INDEX `idx_inbound_record_task`(`putaway_task_id` ASC) USING BTREE,
  INDEX `idx_inbound_record_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_inbound_record_product`(`product_id` ASC) USING BTREE,
  INDEX `fk_inbound_record_supplier`(`supplier_id` ASC) USING BTREE,
  INDEX `fk_inbound_record_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_inbound_record_location`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_inbound_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_task` FOREIGN KEY (`putaway_task_id`) REFERENCES `putaway_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inbound_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inbound_record
-- ----------------------------
INSERT INTO `inbound_record` VALUES (1, 'IR-202604190001', 16, 8, 2, 2, 2, 2, 3, 36.000, 12, 2, '2026-05-02 10:40:00', '2026-04-20 10:40:00', '已完成入库记录示例', '2026-04-19 03:56:42');
INSERT INTO `inbound_record` VALUES (6, 'IR-20260419121132300', 25, 15, 9, 11, 9, 9, 12, 50.000, 4, 1, '2026-04-23 12:11:32', '2026-04-19 12:11:32', '新增一个内酯豆腐', '2026-04-19 04:11:32');
INSERT INTO `inbound_record` VALUES (26, 'IR-20260420051715735', 70, 47, 1, 34, 34, 32, 44, 100.000, 5, 1, '2026-04-25 05:17:16', '2026-04-20 05:17:16', NULL, '2026-04-19 21:17:15');
INSERT INTO `inbound_record` VALUES (46, 'IR-20260420152244637', 98, 71, 1, 11, 9, 9, 58, 9.000, 3, 1, '2026-04-23 15:22:45', '2026-04-20 15:22:45', NULL, '2026-04-20 07:22:44');
INSERT INTO `inbound_record` VALUES (47, 'IR-20260420152442787', 99, 72, 1, 11, 9, 9, 58, 1.000, 3, 1, '2026-04-23 15:24:43', '2026-04-20 15:24:43', NULL, '2026-04-20 07:24:42');

-- ----------------------------
-- Table structure for inventory_adjustment
-- ----------------------------
DROP TABLE IF EXISTS `inventory_adjustment`;
CREATE TABLE `inventory_adjustment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存调整ID',
  `adjustment_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调整单编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `adjustment_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调整方向：INCREASE/DECREASE',
  `quantity` decimal(14, 3) NOT NULL COMMENT '调整数量',
  `reason` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调整原因',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_adjustment_code`(`adjustment_code` ASC) USING BTREE,
  INDEX `idx_inventory_adjustment_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_inventory_adjustment_location`(`location_id` ASC) USING BTREE,
  INDEX `idx_inventory_adjustment_product`(`product_id` ASC) USING BTREE,
  INDEX `fk_inventory_adjustment_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_adjustment_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_adjustment_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_adjustment_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_adjustment_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存调整表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_adjustment
-- ----------------------------
INSERT INTO `inventory_adjustment` VALUES (1, 'ADJ-202604190001', 1, 1, 1, 1, 'INCREASE', 20.000, '系统初始化修正', '库存调整示例', '2026-04-19 05:38:08', '2026-04-19 06:59:50');

-- ----------------------------
-- Table structure for inventory_batch
-- ----------------------------
DROP TABLE IF EXISTS `inventory_batch`;
CREATE TABLE `inventory_batch`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存批次ID',
  `batch_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次编号',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源记录ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `base_occurred_at` datetime NOT NULL COMMENT '效期计算基准时间',
  `shelf_life_days_snapshot` int NOT NULL COMMENT '保质期天数快照',
  `warning_days_snapshot` int NOT NULL DEFAULT 0 COMMENT '预警提前天数快照',
  `warning_at` datetime NOT NULL COMMENT '预警触发时间',
  `expected_expire_at` datetime NOT NULL COMMENT '预计到期时间',
  `initial_quantity` decimal(14, 3) NOT NULL COMMENT '初始数量',
  `remaining_quantity` decimal(14, 3) NOT NULL COMMENT '剩余数量',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_batch_code`(`batch_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_inventory_batch_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_inventory_batch_product_status_expire`(`product_id` ASC, `status` ASC, `expected_expire_at` ASC) USING BTREE,
  INDEX `idx_inventory_batch_location_status`(`location_id` ASC, `status` ASC) USING BTREE,
  INDEX `fk_inventory_batch_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_inventory_batch_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_batch_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_batch_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_batch_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_batch_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存批次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_batch
-- ----------------------------
INSERT INTO `inventory_batch` VALUES (1, 'BATCH-MIG-4', 'MIGRATION_OPENING', 4, 3, 2, 2, 2, '2026-04-19 03:56:42', 12, 2, '2026-04-29 03:56:42', '2026-05-01 03:56:42', 36.000, 36.000, 'ACTIVE', '2026-04-20 04:05:01', '2026-04-20 04:05:01');
INSERT INTO `inventory_batch` VALUES (2, 'BATCH-MIG-9', 'MIGRATION_OPENING', 9, 12, 11, 9, 9, '2026-04-19 06:59:50', 4, 1, '2026-04-22 06:59:50', '2026-04-23 06:59:50', 45.000, 45.000, 'ACTIVE', '2026-04-20 04:05:01', '2026-04-20 04:05:01');
INSERT INTO `inventory_batch` VALUES (3, 'BATCH-MIG-29', 'MIGRATION_OPENING', 29, 1, 1, 1, 1, '2026-04-19 06:59:50', 5, 1, '2026-04-23 06:59:50', '2026-04-24 06:59:50', 18.000, 18.000, 'ACTIVE', '2026-04-20 04:05:01', '2026-04-20 04:05:01');
INSERT INTO `inventory_batch` VALUES (4, 'BATCH-MIG-57', 'MIGRATION_OPENING', 57, 44, 34, 34, 32, '2026-04-19 21:17:15', 5, 1, '2026-04-23 21:17:15', '2026-04-24 21:17:15', 100.000, 100.000, 'ACTIVE', '2026-04-20 04:05:01', '2026-04-20 04:05:01');
INSERT INTO `inventory_batch` VALUES (27, 'BATCH-20260420152244641', 'INBOUND_RECORD', 46, 58, 11, 9, 9, '2026-04-20 15:22:45', 3, 1, '2026-04-22 15:22:45', '2026-04-23 15:22:45', 9.000, 9.000, 'ACTIVE', '2026-04-20 07:22:44', '2026-04-20 07:22:44');
INSERT INTO `inventory_batch` VALUES (28, 'BATCH-20260420152442790', 'INBOUND_RECORD', 47, 58, 11, 9, 9, '2026-04-20 15:24:43', 3, 1, '2026-04-22 15:24:43', '2026-04-23 15:24:43', 1.000, 1.000, 'ACTIVE', '2026-04-20 07:24:42', '2026-04-20 07:24:42');

-- ----------------------------
-- Table structure for inventory_stock
-- ----------------------------
DROP TABLE IF EXISTS `inventory_stock`;
CREATE TABLE `inventory_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存快照ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `quantity` decimal(14, 3) NOT NULL DEFAULT 0.000 COMMENT '当前库存数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_stock_location_product`(`product_id` ASC, `location_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 87 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存快照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_stock
-- ----------------------------
INSERT INTO `inventory_stock` VALUES (4, 3, 2, 2, 2, 36.000, '2026-04-19 03:56:42', '2026-04-19 03:56:42');
INSERT INTO `inventory_stock` VALUES (9, 12, 11, 9, 9, 45.000, '2026-04-19 04:11:32', '2026-04-19 06:59:50');
INSERT INTO `inventory_stock` VALUES (29, 1, 1, 1, 1, 18.000, '2026-04-19 06:59:50', '2026-04-19 06:59:50');
INSERT INTO `inventory_stock` VALUES (57, 44, 34, 34, 32, 100.000, '2026-04-19 21:17:15', '2026-04-19 21:17:15');
INSERT INTO `inventory_stock` VALUES (85, 58, 11, 9, 9, 10.000, '2026-04-20 07:22:44', '2026-04-20 07:24:42');

-- ----------------------------
-- Table structure for inventory_stocktaking_item
-- ----------------------------
DROP TABLE IF EXISTS `inventory_stocktaking_item`;
CREATE TABLE `inventory_stocktaking_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点单明细ID',
  `stocktaking_order_id` bigint NOT NULL COMMENT '所属盘点单ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `system_quantity` decimal(14, 3) NOT NULL COMMENT '系统数量',
  `counted_quantity` decimal(14, 3) NULL DEFAULT NULL COMMENT '实盘数量',
  `difference_quantity` decimal(14, 3) NULL DEFAULT NULL COMMENT '差异数量',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inventory_stocktaking_item_order`(`stocktaking_order_id` ASC) USING BTREE,
  INDEX `idx_inventory_stocktaking_item_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_inventory_stocktaking_item_location`(`location_id` ASC) USING BTREE,
  INDEX `fk_inventory_stocktaking_item_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_inventory_stocktaking_item_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_stocktaking_item_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_stocktaking_item_order` FOREIGN KEY (`stocktaking_order_id`) REFERENCES `inventory_stocktaking_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_stocktaking_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_stocktaking_item_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_stocktaking_item_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存盘点单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_stocktaking_item
-- ----------------------------
INSERT INTO `inventory_stocktaking_item` VALUES (1, 1, 1, 1, 1, 1, 20.000, 18.000, -2.000, '已完成盘点示例明细', '2026-04-19 05:56:56', '2026-04-19 06:59:50');

-- ----------------------------
-- Table structure for inventory_stocktaking_order
-- ----------------------------
DROP TABLE IF EXISTS `inventory_stocktaking_order`;
CREATE TABLE `inventory_stocktaking_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `stocktaking_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NULL DEFAULT NULL COMMENT '库区ID，为空表示盘点整个仓库',
  `status` tinyint NOT NULL COMMENT '状态：1待盘点 2待确认 3已完成 4已取消',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_stocktaking_code`(`stocktaking_code` ASC) USING BTREE,
  INDEX `idx_inventory_stocktaking_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_inventory_stocktaking_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_stocktaking_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_stocktaking_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存盘点单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_stocktaking_order
-- ----------------------------
INSERT INTO `inventory_stocktaking_order` VALUES (1, 'STK-202604190001', 1, 1, 3, '已完成盘点示例', '2026-04-19 05:56:56', '2026-04-19 06:59:50');

-- ----------------------------
-- Table structure for inventory_transaction
-- ----------------------------
DROP TABLE IF EXISTS `inventory_transaction`;
CREATE TABLE `inventory_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  `transaction_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流水编号',
  `transaction_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流水类型',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '数量变化',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源ID',
  `occurred_at` datetime NOT NULL COMMENT '发生时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_transaction_code`(`transaction_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 151 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_transaction
-- ----------------------------
INSERT INTO `inventory_transaction` VALUES (4, 'INVTX-202604190001', 'INBOUND', 3, 2, 2, 2, 36.000, 'PUTAWAY_TASK', 8, '2026-04-20 10:40:00', '已完成入库流水示例', '2026-04-19 03:56:42');
INSERT INTO `inventory_transaction` VALUES (9, 'INVTX-20260419121132296', 'INBOUND', 12, 11, 9, 9, 50.000, 'PUTAWAY_TASK', 15, '2026-04-19 12:11:32', '新增一个内酯豆腐', '2026-04-19 04:11:32');
INSERT INTO `inventory_transaction` VALUES (11, 'INVTX-202604190003', 'OUTBOUND', 12, 11, 9, 9, -5.000, 'OUTBOUND_TASK', 4, '2026-04-21 15:40:00', '已完成出库流水示例', '2026-04-19 05:02:38');
INSERT INTO `inventory_transaction` VALUES (18, 'INVTX-202604190004', 'ADJUSTMENT', 1, 1, 1, 1, 20.000, 'INVENTORY_ADJUSTMENT', 1, '2026-04-19 05:38:08', '库存调整示例', '2026-04-19 05:38:08');
INSERT INTO `inventory_transaction` VALUES (20, 'INVTX-202604190005', 'STOCKTAKING', 1, 1, 1, 1, -2.000, 'INVENTORY_STOCKTAKING', 1, '2026-04-19 05:56:56', '已完成盘点示例明细', '2026-04-19 05:56:56');
INSERT INTO `inventory_transaction` VALUES (96, 'INVTX-20260420051715731', 'INBOUND', 44, 34, 34, 32, 100.000, 'PUTAWAY_TASK', 47, '2026-04-20 05:17:16', NULL, '2026-04-19 21:17:15');
INSERT INTO `inventory_transaction` VALUES (149, 'INVTX-20260420152244635', 'INBOUND', 58, 11, 9, 9, 9.000, 'PUTAWAY_TASK', 71, '2026-04-20 15:22:45', NULL, '2026-04-20 07:22:44');
INSERT INTO `inventory_transaction` VALUES (150, 'INVTX-20260420152442785', 'INBOUND', 58, 11, 9, 9, 1.000, 'PUTAWAY_TASK', 72, '2026-04-20 15:24:43', NULL, '2026-04-20 07:24:42');

-- ----------------------------
-- Table structure for loss_record
-- ----------------------------
DROP TABLE IF EXISTS `loss_record`;
CREATE TABLE `loss_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '损耗记录ID',
  `loss_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '损耗记录编号',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型：ABNORMAL_STOCK/DIRECT',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '损耗数量',
  `loss_reason` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '损耗原因',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_loss_record_code`(`loss_code` ASC) USING BTREE,
  INDEX `idx_loss_record_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_loss_record_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_loss_record_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_loss_record_location`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_loss_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_loss_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_loss_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_loss_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '损耗记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of loss_record
-- ----------------------------

-- ----------------------------
-- Table structure for message_notice
-- ----------------------------
DROP TABLE IF EXISTS `message_notice`;
CREATE TABLE `message_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `notice_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息编号',
  `alert_record_id` bigint NULL DEFAULT NULL COMMENT '关联预警记录ID',
  `notice_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型',
  `severity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '严重级别',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源ID',
  `source_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源编号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1未读 2已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_notice_code`(`notice_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_message_notice_alert`(`alert_record_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_notice_alert` FOREIGN KEY (`alert_record_id`) REFERENCES `alert_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 158 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_notice
-- ----------------------------
INSERT INTO `message_notice` VALUES (129, 'MSG-20260420030614910', 129, 'ALERT', 'MEDIUM', '菠菜鲜菜 低库存预警', '菠菜鲜菜 在 一号综合仓/A-01-01 的可用库存为 18.000，低于阈值 50.000', 'INVENTORY_STOCK', 29, 'STOCK-29', 1, '2026-04-19 19:06:14', NULL);
INSERT INTO `message_notice` VALUES (130, 'MSG-20260420030614913', 130, 'ALERT', 'MEDIUM', '内酯豆腐 低库存预警', '内酯豆腐 在 三里庵一号仓/冷藏库区1-1 的可用库存为 45.000，低于阈值 50.000', 'INVENTORY_STOCK', 9, 'STOCK-9', 1, '2026-04-19 19:06:14', NULL);
INSERT INTO `message_notice` VALUES (131, 'MSG-20260420030614916', 131, 'ALERT', 'MEDIUM', '武鸣柑橘 低库存预警', '武鸣柑橘 在 冷链中心仓/B-02-01 的可用库存为 36.000，低于阈值 50.000', 'INVENTORY_STOCK', 4, 'STOCK-4', 1, '2026-04-19 19:06:14', NULL);
INSERT INTO `message_notice` VALUES (132, 'MSG-20260420030614925', 132, 'ALERT', 'MEDIUM', 'PT-202604190001 待上架超时', '上架任务 PT-202604190001 已超过 4.000 小时未完成上架', 'PUTAWAY_TASK', 1, 'PT-202604190001', 1, '2026-04-19 19:06:14', NULL);
INSERT INTO `message_notice` VALUES (133, 'MSG-20260420030614945', 133, 'ALERT', 'MEDIUM', 'IR-20260419121132300 待质检超时', '入库记录 IR-20260419121132300 已超过 8.000 小时未完成质检', 'INBOUND_RECORD', 6, 'IR-20260419121132300', 1, '2026-04-19 19:06:14', NULL);
INSERT INTO `message_notice` VALUES (145, 'MSG-20260420143556389', 145, 'ALERT', 'MEDIUM', 'IR-20260420051715735 待质检超时', '入库记录 IR-20260420051715735 已超过 8.000 小时未完成质检', 'INBOUND_RECORD', 26, 'IR-20260420051715735', 1, '2026-04-20 06:35:56', NULL);
INSERT INTO `message_notice` VALUES (150, 'MSG-20260420152740138', 150, 'ALERT', 'MEDIUM', '猪肋排条 低库存预警', '猪肋排条 在 三里庵一号仓/冷藏库区1-1 的可用库存为 10.000，低于阈值 50.000', 'INVENTORY_STOCK', 85, 'STOCK-85', 1, '2026-04-20 07:27:40', NULL);
INSERT INTO `message_notice` VALUES (151, 'MSG-20260420152740141', 152, 'ALERT', 'MEDIUM', '菠菜鲜菜 低库存预警', '菠菜鲜菜 在 一号综合仓/A-01-01 的可用库存为 18.000，低于阈值 50.000', 'INVENTORY_STOCK', 29, 'STOCK-29', 1, '2026-04-20 07:27:40', NULL);
INSERT INTO `message_notice` VALUES (152, 'MSG-20260420152740144', 153, 'ALERT', 'MEDIUM', '内酯豆腐 低库存预警', '内酯豆腐 在 三里庵一号仓/冷藏库区1-1 的可用库存为 45.000，低于阈值 50.000', 'INVENTORY_STOCK', 9, 'STOCK-9', 1, '2026-04-20 07:27:40', NULL);
INSERT INTO `message_notice` VALUES (153, 'MSG-20260420152740148', 154, 'ALERT', 'MEDIUM', '武鸣柑橘 低库存预警', '武鸣柑橘 在 冷链中心仓/B-02-01 的可用库存为 36.000，低于阈值 50.000', 'INVENTORY_STOCK', 4, 'STOCK-4', 1, '2026-04-20 07:27:40', NULL);
INSERT INTO `message_notice` VALUES (156, 'MSG-20260420155639045', 157, 'ALERT', 'MEDIUM', 'PT-20260420155220823 待上架超时', '上架任务 PT-20260420155220823 已超过 1.000 分钟 未完成上架', 'PUTAWAY_TASK', 73, 'PT-20260420155220823', 1, '2026-04-20 07:56:39', NULL);
INSERT INTO `message_notice` VALUES (157, 'MSG-20260420155639051', 158, 'ALERT', 'MEDIUM', 'PT-20260420155220823 待上架超时', '上架任务 PT-20260420155220823 已超过 1.000 分钟 未完成上架', 'PUTAWAY_TASK', 73, 'PT-20260420155220823', 1, '2026-04-20 07:56:39', NULL);

-- ----------------------------
-- Table structure for outbound_order
-- ----------------------------
DROP TABLE IF EXISTS `outbound_order`;
CREATE TABLE `outbound_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `order_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出库单编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `expected_delivery_at` datetime NOT NULL COMMENT '预计发货时间',
  `actual_outbound_at` datetime NULL DEFAULT NULL COMMENT '实际出库时间',
  `total_item_count` int NOT NULL COMMENT '商品条数',
  `total_quantity` decimal(14, 3) NOT NULL COMMENT '总数量',
  `status` tinyint NOT NULL COMMENT '状态：1待分配 2待拣货 3待出库 4已完成 5已取消',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_order_code`(`order_code` ASC) USING BTREE,
  INDEX `idx_outbound_order_customer`(`customer_id` ASC) USING BTREE,
  INDEX `idx_outbound_order_warehouse`(`warehouse_id` ASC) USING BTREE,
  CONSTRAINT `fk_outbound_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_order_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 155 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_order
-- ----------------------------
INSERT INTO `outbound_order` VALUES (1, 'OUT-202604190001', 1, 2, '2026-04-21 09:30:00', NULL, 1, 18.000, 1, '待分配出库示例', '2026-04-19 04:41:28', '2026-04-19 04:41:28');
INSERT INTO `outbound_order` VALUES (5, 'OUT-202604190002', 2, 2, '2026-04-21 14:00:00', NULL, 1, 12.000, 2, '待拣货示例', '2026-04-19 04:56:11', '2026-04-19 04:56:11');
INSERT INTO `outbound_order` VALUES (11, 'OUT-202604190003', 2, 11, '2026-04-21 16:00:00', '2026-04-21 15:40:00', 1, 5.000, 4, '已完成出库示例', '2026-04-19 05:02:38', '2026-04-19 05:02:38');
INSERT INTO `outbound_order` VALUES (112, 'OUT-20260420051749918', 1, 34, '2026-04-22 05:17:00', NULL, 1, 10.000, 2, NULL, '2026-04-19 21:17:49', '2026-04-19 21:17:53');

-- ----------------------------
-- Table structure for outbound_order_item
-- ----------------------------
DROP TABLE IF EXISTS `outbound_order_item`;
CREATE TABLE `outbound_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库单明细ID',
  `outbound_order_id` bigint NOT NULL COMMENT '所属出库单ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '出库数量',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_outbound_order_item_order`(`outbound_order_id` ASC) USING BTREE,
  INDEX `idx_outbound_order_item_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_outbound_order_item_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 174 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_order_item
-- ----------------------------
INSERT INTO `outbound_order_item` VALUES (1, 1, 3, 18.000, 1, '待分配出库示例明细', '2026-04-19 04:41:28', '2026-04-19 04:41:28');
INSERT INTO `outbound_order_item` VALUES (6, 5, 3, 12.000, 1, '待拣货示例明细', '2026-04-19 04:56:11', '2026-04-19 04:56:11');
INSERT INTO `outbound_order_item` VALUES (13, 11, 12, 5.000, 1, '已完成出库示例明细', '2026-04-19 05:02:38', '2026-04-19 05:02:38');
INSERT INTO `outbound_order_item` VALUES (127, 112, 45, 10.000, 1, NULL, '2026-04-19 21:17:49', '2026-04-19 21:17:49');

-- ----------------------------
-- Table structure for outbound_record
-- ----------------------------
DROP TABLE IF EXISTS `outbound_record`;
CREATE TABLE `outbound_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库记录ID',
  `record_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '记录编号',
  `outbound_order_id` bigint NOT NULL COMMENT '出库单ID',
  `outbound_task_id` bigint NOT NULL COMMENT '拣货出库任务ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '出库数量',
  `occurred_at` datetime NOT NULL COMMENT '出库时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_record_code`(`record_code` ASC) USING BTREE,
  INDEX `idx_outbound_record_order`(`outbound_order_id` ASC) USING BTREE,
  INDEX `idx_outbound_record_task`(`outbound_task_id` ASC) USING BTREE,
  INDEX `idx_outbound_record_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_outbound_record_product`(`product_id` ASC) USING BTREE,
  INDEX `fk_outbound_record_customer`(`customer_id` ASC) USING BTREE,
  INDEX `fk_outbound_record_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_outbound_record_location`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_outbound_record_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_task` FOREIGN KEY (`outbound_task_id`) REFERENCES `outbound_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_record
-- ----------------------------
INSERT INTO `outbound_record` VALUES (1, 'OR-202604190001', 11, 4, 2, 11, 9, 9, 12, 5.000, '2026-04-21 15:40:00', '已完成出库记录示例', '2026-04-19 05:02:38');

-- ----------------------------
-- Table structure for outbound_task
-- ----------------------------
DROP TABLE IF EXISTS `outbound_task`;
CREATE TABLE `outbound_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拣货出库任务ID',
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编号',
  `outbound_order_id` bigint NOT NULL COMMENT '来源出库单ID',
  `outbound_order_item_id` bigint NOT NULL COMMENT '来源出库单明细ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NULL DEFAULT NULL COMMENT '库区ID',
  `location_id` bigint NULL DEFAULT NULL COMMENT '库位ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '出库数量',
  `status` tinyint NOT NULL COMMENT '状态：1待分配 2待拣货 3待出库 4已完成 5已取消',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `picked_at` datetime NULL DEFAULT NULL COMMENT '拣货完成时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '出库完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_task_code`(`task_code` ASC) USING BTREE,
  INDEX `idx_outbound_task_order`(`outbound_order_id` ASC) USING BTREE,
  INDEX `idx_outbound_task_order_item`(`outbound_order_item_id` ASC) USING BTREE,
  INDEX `idx_outbound_task_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_outbound_task_location`(`location_id` ASC) USING BTREE,
  INDEX `idx_outbound_task_product`(`product_id` ASC) USING BTREE,
  INDEX `fk_outbound_task_customer`(`customer_id` ASC) USING BTREE,
  INDEX `fk_outbound_task_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_outbound_task_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_order` FOREIGN KEY (`outbound_order_id`) REFERENCES `outbound_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_order_item` FOREIGN KEY (`outbound_order_item_id`) REFERENCES `outbound_order_item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拣货出库任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_task
-- ----------------------------
INSERT INTO `outbound_task` VALUES (1, 'OT-202604190001', 5, 6, 2, 2, NULL, NULL, 3, 12.000, 1, '待分配拣货任务示例', NULL, NULL, '2026-04-19 04:56:11', '2026-04-19 04:56:11');
INSERT INTO `outbound_task` VALUES (4, 'OT-202604190002', 11, 13, 2, 11, 9, 9, 12, 5.000, 4, '已完成出库任务示例', '2026-04-21 15:20:00', '2026-04-21 15:40:00', '2026-04-19 05:02:38', '2026-04-19 05:02:38');
INSERT INTO `outbound_task` VALUES (66, 'OT-20260420051753229', 112, 127, 1, 34, NULL, NULL, 45, 10.000, 1, NULL, NULL, NULL, '2026-04-19 21:17:53', '2026-04-19 21:17:53');

-- ----------------------------
-- Table structure for outbound_task_batch_allocation
-- ----------------------------
DROP TABLE IF EXISTS `outbound_task_batch_allocation`;
CREATE TABLE `outbound_task_batch_allocation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拣货批次分配ID',
  `outbound_task_id` bigint NOT NULL COMMENT '出库任务ID',
  `inventory_batch_id` bigint NOT NULL COMMENT '库存批次ID',
  `allocated_quantity` decimal(14, 3) NOT NULL COMMENT '分配数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_outbound_task_batch_allocation`(`outbound_task_id` ASC, `inventory_batch_id` ASC) USING BTREE,
  INDEX `fk_outbound_task_batch_inventory`(`inventory_batch_id` ASC) USING BTREE,
  CONSTRAINT `fk_outbound_task_batch_inventory` FOREIGN KEY (`inventory_batch_id`) REFERENCES `inventory_batch` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_outbound_task_batch_task` FOREIGN KEY (`outbound_task_id`) REFERENCES `outbound_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库任务批次分配表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_task_batch_allocation
-- ----------------------------

-- ----------------------------
-- Table structure for product_archive
-- ----------------------------
DROP TABLE IF EXISTS `product_archive`;
CREATE TABLE `product_archive`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品档案ID',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品编号',
  `product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
  `product_specification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品规格',
  `category_id` bigint NOT NULL COMMENT '产品分类ID',
  `unit_id` bigint NOT NULL COMMENT '产品单位ID',
  `origin_id` bigint NOT NULL COMMENT '产地信息ID',
  `storage_condition_id` bigint NOT NULL COMMENT '储存条件ID',
  `shelf_life_days` int NOT NULL COMMENT '保质期天数',
  `warning_days` int NOT NULL DEFAULT 0 COMMENT '预警提前天数',
  `quality_grade_id` bigint NOT NULL COMMENT '品质等级ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_archive_code`(`product_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_product_archive_name`(`product_name` ASC) USING BTREE,
  INDEX `idx_product_archive_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_product_archive_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_product_archive_unit`(`unit_id` ASC) USING BTREE,
  INDEX `idx_product_archive_origin`(`origin_id` ASC) USING BTREE,
  INDEX `idx_product_archive_storage_condition`(`storage_condition_id` ASC) USING BTREE,
  INDEX `idx_product_archive_quality_grade`(`quality_grade_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_archive_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_archive_origin` FOREIGN KEY (`origin_id`) REFERENCES `product_origin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_archive_quality_grade` FOREIGN KEY (`quality_grade_id`) REFERENCES `quality_grade` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_archive_storage_condition` FOREIGN KEY (`storage_condition_id`) REFERENCES `storage_condition` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_archive_unit` FOREIGN KEY (`unit_id`) REFERENCES `product_unit` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_archive
-- ----------------------------
INSERT INTO `product_archive` VALUES (1, 'PROD-202604190001', '菠菜鲜菜', '500g/袋', 3, 1, 1, 1, 5, 1, 1, 10, 1, '叶菜类标准产品', '2026-04-18 22:03:49', '2026-04-19 00:29:55');
INSERT INTO `product_archive` VALUES (2, 'PROD-202604190002', '根茎胡萝卜', '1kg/袋', 5, 1, 1, 2, 15, 3, 2, 20, 1, '根茎类标准产品', '2026-04-18 22:03:49', '2026-04-19 00:29:55');
INSERT INTO `product_archive` VALUES (3, 'PROD-202604190003', '武鸣柑橘', '2kg/箱', 7, 2, 3, 3, 12, 2, 1, 30, 1, '水果类标准产品', '2026-04-18 22:03:49', '2026-04-19 00:29:55');
INSERT INTO `product_archive` VALUES (12, 'PROD-20260419083640261', '内酯豆腐', '350g/盒', 20, 11, 10, 6, 4, 1, 2, 0, 1, '测试备注', '2026-04-19 00:36:40', '2026-04-19 00:36:40');
INSERT INTO `product_archive` VALUES (44, 'PROD-20260420051231855', '鳜鱼', '500g/条', 40, 1, 10, 46, 5, 1, 2, 0, 1, NULL, '2026-04-19 21:12:31', '2026-04-19 21:12:31');
INSERT INTO `product_archive` VALUES (45, 'PROD-20260420051437749', '梭子蟹', '200g/只', 41, 45, 10, 46, 3, 1, 2, 0, 1, NULL, '2026-04-19 21:14:37', '2026-04-19 21:14:37');
INSERT INTO `product_archive` VALUES (58, 'PROD-20260420141659764', '猪肋排条', '700g/盒', 48, 11, 10, 51, 3, 1, 2, 0, 1, NULL, '2026-04-20 06:16:59', '2026-04-20 06:16:59');

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类编号',
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '上级分类ID',
  `category_level` int NOT NULL COMMENT '层级，从1开始',
  `ancestor_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '/' COMMENT '祖先路径，形如 /1/2/',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `default_storage_condition_id` bigint NULL DEFAULT NULL COMMENT '默认储存条件ID',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_product_category_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_product_category_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_product_category_storage_condition`(`default_storage_condition_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `product_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_category_storage_condition` FOREIGN KEY (`default_storage_condition_id`) REFERENCES `storage_condition` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '农产品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_category
-- ----------------------------
INSERT INTO `product_category` VALUES (1, 'CAT-A00', '新鲜蔬菜', NULL, 1, '/', 10, 1, NULL, '蔬菜大类', '2026-04-18 19:00:01', '2026-04-18 19:00:01');
INSERT INTO `product_category` VALUES (2, 'CAT-A01', '叶菜类', 1, 2, '/1/', 1, 1, 1, '叶菜默认规则', '2026-04-18 19:00:01', '2026-04-18 20:51:10');
INSERT INTO `product_category` VALUES (3, 'CAT-A01-01', '菠菜', 2, 3, '/1/2/', 1, 1, 1, '菠菜细分类', '2026-04-18 19:00:01', '2026-04-18 20:51:10');
INSERT INTO `product_category` VALUES (4, 'CAT-A01-02', '生菜', 2, 3, '/1/2/', 2, 1, 1, '生菜细分类', '2026-04-18 19:00:01', '2026-04-18 20:51:10');
INSERT INTO `product_category` VALUES (5, 'CAT-A02', '根茎类', 1, 2, '/1/', 2, 0, 2, '根茎类默认规则', '2026-04-18 19:00:01', '2026-04-18 20:51:10');
INSERT INTO `product_category` VALUES (6, 'CAT-B00', '时令水果', NULL, 1, '/', 20, 1, NULL, '水果大类', '2026-04-18 19:00:01', '2026-04-18 19:00:01');
INSERT INTO `product_category` VALUES (7, 'CAT-B01', '柑橘类', 6, 2, '/6/', 1, 1, 3, '柑橘类默认规则', '2026-04-18 19:00:01', '2026-04-18 20:51:10');
INSERT INTO `product_category` VALUES (20, 'CAT-20260419051005341', '豆制品', NULL, 1, '/', 0, 1, 6, NULL, '2026-04-18 21:10:05', '2026-04-18 21:10:05');
INSERT INTO `product_category` VALUES (39, 'CAT-20260420030004571', '海产品', NULL, 1, '/', 1, 1, NULL, NULL, '2026-04-19 19:00:04', '2026-04-19 19:00:04');
INSERT INTO `product_category` VALUES (40, 'CAT-20260420051137914', '鱼类', 39, 2, '/39/', 0, 1, 46, NULL, '2026-04-19 21:11:37', '2026-04-19 21:11:37');
INSERT INTO `product_category` VALUES (41, 'CAT-20260420051328158', '蟹类', 39, 2, '/39/', 0, 1, 46, NULL, '2026-04-19 21:13:28', '2026-04-19 21:13:28');
INSERT INTO `product_category` VALUES (47, 'CAT-20260420141252680', '肉禽蛋', NULL, 1, '/', 0, 1, NULL, NULL, '2026-04-20 06:12:52', '2026-04-20 06:12:52');
INSERT INTO `product_category` VALUES (48, 'CAT-20260420141457747', '猪肉/黑猪', 47, 2, '/47/', 0, 1, 51, NULL, '2026-04-20 06:14:57', '2026-04-20 06:14:57');

-- ----------------------------
-- Table structure for product_origin
-- ----------------------------
DROP TABLE IF EXISTS `product_origin`;
CREATE TABLE `product_origin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产地ID',
  `origin_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产地编号',
  `origin_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产地名称',
  `country_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家名称',
  `province_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份名称',
  `city_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_origin_code`(`origin_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_product_origin_name`(`origin_name` ASC) USING BTREE,
  INDEX `idx_product_origin_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品产地表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_origin
-- ----------------------------
INSERT INTO `product_origin` VALUES (1, 'ORI-202604190001', '山东寿光', '中国', '山东省', '潍坊市', 10, 1, '设施蔬菜核心产区', '2026-04-18 21:38:01', '2026-04-18 21:38:01');
INSERT INTO `product_origin` VALUES (2, 'ORI-202604190002', '云南西双版纳', '中国', '云南省', '西双版纳州', 20, 1, '热带水果代表产区', '2026-04-18 21:38:01', '2026-04-18 21:38:01');
INSERT INTO `product_origin` VALUES (3, 'ORI-202604190003', '广西武鸣', '中国', '广西壮族自治区', '南宁市', 30, 1, '柑橘类特色产区', '2026-04-18 21:38:01', '2026-04-18 21:38:01');
INSERT INTO `product_origin` VALUES (10, 'ORI-20260419081020747', '合肥龙岗', '中国', '安徽', '合肥', 0, 1, NULL, '2026-04-19 00:10:20', '2026-04-19 00:10:20');

-- ----------------------------
-- Table structure for product_unit
-- ----------------------------
DROP TABLE IF EXISTS `product_unit`;
CREATE TABLE `product_unit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '单位ID',
  `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位编号',
  `unit_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位名称',
  `unit_symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位符号',
  `unit_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位类型',
  `precision_digits` int NOT NULL DEFAULT 0 COMMENT '精度位数',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_unit_code`(`unit_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_product_unit_name`(`unit_name` ASC) USING BTREE,
  INDEX `idx_product_unit_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品单位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_unit
-- ----------------------------
INSERT INTO `product_unit` VALUES (1, 'UNIT-202604190001', '千克', 'kg', '重量', 3, 10, 1, '适用于农产品重量计量', '2026-04-18 21:30:07', '2026-04-18 21:30:07');
INSERT INTO `product_unit` VALUES (2, 'UNIT-202604190002', '箱', '箱', '包装', 0, 20, 1, '适用于整箱计数', '2026-04-18 21:30:07', '2026-04-18 21:30:07');
INSERT INTO `product_unit` VALUES (3, 'UNIT-202604190003', '件', '件', '数量', 0, 30, 1, '适用于单件计数', '2026-04-18 21:30:07', '2026-04-18 21:30:07');
INSERT INTO `product_unit` VALUES (10, 'UNIT-20260419080949057', '个', 'ge', '数量', 0, 0, 1, NULL, '2026-04-19 00:09:49', '2026-04-19 00:09:49');
INSERT INTO `product_unit` VALUES (11, 'UNIT-20260419081220675', '盒', 'He', '包装', 0, 0, 1, NULL, '2026-04-19 00:12:20', '2026-04-19 00:12:20');
INSERT INTO `product_unit` VALUES (39, 'UNIT-20260420021204661', '助手测试盒', 'ZTH', '包装单位', 0, 91, 1, NULL, '2026-04-19 18:12:04', '2026-04-19 18:12:04');
INSERT INTO `product_unit` VALUES (45, 'UNIT-20260420051350713', '只', 'ZHI', '数量', 0, 0, 1, NULL, '2026-04-19 21:13:50', '2026-04-19 21:13:50');

-- ----------------------------
-- Table structure for putaway_task
-- ----------------------------
DROP TABLE IF EXISTS `putaway_task`;
CREATE TABLE `putaway_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '上架任务ID',
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编号',
  `inbound_order_id` bigint NOT NULL COMMENT '来源入库单ID',
  `inbound_order_item_id` bigint NOT NULL COMMENT '来源入库单明细ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NULL DEFAULT NULL COMMENT '库区ID',
  `location_id` bigint NULL DEFAULT NULL COMMENT '库位ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` decimal(14, 3) NOT NULL COMMENT '待上架数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1待分配，2待上架，3已完成，4已取消',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_putaway_task_code`(`task_code` ASC) USING BTREE,
  INDEX `idx_putaway_task_status`(`status` ASC) USING BTREE,
  INDEX `idx_putaway_task_order`(`inbound_order_id` ASC) USING BTREE,
  INDEX `idx_putaway_task_item`(`inbound_order_item_id` ASC) USING BTREE,
  INDEX `idx_putaway_task_location`(`location_id` ASC) USING BTREE,
  INDEX `fk_putaway_task_supplier`(`supplier_id` ASC) USING BTREE,
  INDEX `fk_putaway_task_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_putaway_task_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_putaway_task_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_putaway_task_item` FOREIGN KEY (`inbound_order_item_id`) REFERENCES `inbound_order_item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_order` FOREIGN KEY (`inbound_order_id`) REFERENCES `inbound_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_putaway_task_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '上架任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of putaway_task
-- ----------------------------
INSERT INTO `putaway_task` VALUES (1, 'PT-202604190001', 6, 6, 2, 2, NULL, NULL, 3, 48.000, 4, '待分配库位', NULL, '2026-04-19 03:26:09', '2026-04-20 08:06:45');
INSERT INTO `putaway_task` VALUES (8, 'PT-202604190002', 16, 16, 2, 2, 2, 2, 3, 36.000, 3, '已完成上架示例', '2026-04-20 10:40:00', '2026-04-19 03:56:42', '2026-04-19 03:56:42');
INSERT INTO `putaway_task` VALUES (15, 'PT-20260419121028848', 25, 25, 9, 11, 9, 9, 12, 50.000, 3, '新增一个内酯豆腐', '2026-04-19 12:11:32', '2026-04-19 04:10:28', '2026-04-19 06:26:50');
INSERT INTO `putaway_task` VALUES (47, 'PT-20260420051703375', 70, 70, 1, 34, 34, 32, 44, 100.000, 3, NULL, '2026-04-20 05:17:16', '2026-04-19 21:17:03', '2026-04-19 21:17:15');
INSERT INTO `putaway_task` VALUES (71, 'PT-20260420152235602', 98, 98, 1, 11, 9, 9, 58, 9.000, 3, NULL, '2026-04-20 15:22:45', '2026-04-20 07:22:35', '2026-04-20 07:22:44');
INSERT INTO `putaway_task` VALUES (72, 'PT-20260420152434834', 99, 99, 1, 11, 9, 9, 58, 1.000, 3, NULL, '2026-04-20 15:24:43', '2026-04-20 07:24:34', '2026-04-20 07:24:42');
INSERT INTO `putaway_task` VALUES (73, 'PT-20260420155220823', 100, 100, 1, 11, NULL, NULL, 58, 10.000, 1, NULL, NULL, '2026-04-20 07:52:20', '2026-04-20 07:52:20');

-- ----------------------------
-- Table structure for quality_grade
-- ----------------------------
DROP TABLE IF EXISTS `quality_grade`;
CREATE TABLE `quality_grade`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品质等级ID',
  `grade_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '品质等级编号',
  `grade_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '品质等级名称',
  `score_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低分值',
  `score_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高分值',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quality_grade_code`(`grade_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_quality_grade_name`(`grade_name` ASC) USING BTREE,
  INDEX `idx_quality_grade_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '品质等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quality_grade
-- ----------------------------
INSERT INTO `quality_grade` VALUES (1, 'GRADE-202604190001', '特级', 90.00, 100.00, 10, 1, '适用于高品质精选农产品', '2026-04-18 21:45:08', '2026-04-18 21:45:08');
INSERT INTO `quality_grade` VALUES (2, 'GRADE-202604190002', '一级', 80.00, 89.90, 20, 1, '适用于优选农产品', '2026-04-18 21:45:08', '2026-04-18 21:45:08');
INSERT INTO `quality_grade` VALUES (3, 'GRADE-202604190003', '二级', 70.00, 79.90, 30, 1, '适用于标准农产品', '2026-04-18 21:45:08', '2026-04-18 21:45:08');

-- ----------------------------
-- Table structure for quality_inspection
-- ----------------------------
DROP TABLE IF EXISTS `quality_inspection`;
CREATE TABLE `quality_inspection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '质检单ID',
  `inspection_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '质检单编号',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型：INBOUND_RECORD/INVENTORY_STOCK',
  `source_id` bigint NOT NULL COMMENT '来源ID',
  `source_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源编号',
  `source_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源展示文本',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `zone_id` bigint NOT NULL COMMENT '库区ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `inspect_quantity` decimal(14, 3) NOT NULL COMMENT '送检数量',
  `qualified_quantity` decimal(14, 3) NOT NULL COMMENT '合格数量',
  `unqualified_quantity` decimal(14, 3) NOT NULL COMMENT '不合格数量',
  `result_status` tinyint NOT NULL COMMENT '结果：1合格 2部分不合格 3不合格',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quality_inspection_code`(`inspection_code` ASC) USING BTREE,
  INDEX `idx_quality_inspection_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_quality_inspection_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_quality_inspection_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `fk_quality_inspection_zone`(`zone_id` ASC) USING BTREE,
  INDEX `fk_quality_inspection_location`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_quality_inspection_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_quality_inspection_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_quality_inspection_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_quality_inspection_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '质检单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quality_inspection
-- ----------------------------

-- ----------------------------
-- Table structure for storage_condition
-- ----------------------------
DROP TABLE IF EXISTS `storage_condition`;
CREATE TABLE `storage_condition`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '储存条件ID',
  `condition_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条件编号',
  `condition_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条件名称',
  `storage_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '储存类型',
  `temperature_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低温度',
  `temperature_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高温度',
  `humidity_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低湿度',
  `humidity_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高湿度',
  `light_requirement` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '避光要求',
  `ventilation_requirement` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通风要求',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_storage_condition_code`(`condition_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_storage_condition_name`(`condition_name` ASC) USING BTREE,
  INDEX `idx_storage_condition_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '储存条件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of storage_condition
-- ----------------------------
INSERT INTO `storage_condition` VALUES (1, 'SC-202604190001', '叶菜冷藏标准', '冷藏', 2.00, 8.00, 75.00, 90.00, '需避强光', '普通通风', 1, 10, '适用于叶菜、菠菜、生菜等鲜蔬', '2026-04-18 19:56:11', '2026-04-18 19:56:11');
INSERT INTO `storage_condition` VALUES (2, 'SC-202604190002', '根茎阴凉干燥标准', '阴凉干燥', 10.00, 15.00, 50.00, 70.00, '无特殊要求', '普通通风', 1, 20, '适用于根茎类、薯类、洋葱等', '2026-04-18 19:56:12', '2026-04-18 19:56:12');
INSERT INTO `storage_condition` VALUES (3, 'SC-202604190003', '水果冷藏标准', '冷藏', 4.00, 8.00, 65.00, 85.00, '避免直射阳光', '普通通风', 1, 30, '适用于柑橘类、时令水果', '2026-04-18 19:56:12', '2026-04-18 19:56:12');
INSERT INTO `storage_condition` VALUES (6, 'SC-20260419043325175', '豆制品存储条件', '冷藏', 0.00, 5.00, 40.00, 74.80, '需避强光', '普通通风', 1, 0, NULL, '2026-04-18 20:33:25', '2026-04-18 20:33:25');
INSERT INTO `storage_condition` VALUES (46, 'SC-20260420051106984', '鱼类存储条件', '恒温', 10.00, 20.00, 70.00, 90.00, '避免直射阳光', '普通通风', 1, 0, NULL, '2026-04-19 21:11:06', '2026-04-19 21:11:06');
INSERT INTO `storage_condition` VALUES (51, 'SC-20260420141427288', '肉类储存', '冷藏', 0.00, 4.00, 10.00, 20.00, '避免直射阳光', '普通通风', 1, 0, NULL, '2026-04-20 06:14:27', '2026-04-20 06:14:27');

-- ----------------------------
-- Table structure for supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编号',
  `supplier_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `supplier_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商类型',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `region_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在地区',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_code`(`supplier_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_supplier_name`(`supplier_name` ASC) USING BTREE,
  INDEX `idx_supplier_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supplier
-- ----------------------------
INSERT INTO `supplier` VALUES (1, 'SUP-202604190001', '寿光绿源农业合作社', '产地直供', '王菜农', '13800000201', '山东省寿光市', '蔬菜产业园1号', 10, 1, '叶菜类核心供应商', '2026-04-19 02:24:58', '2026-04-19 02:24:58');
INSERT INTO `supplier` VALUES (2, 'SUP-202604190002', '云南丰鲜果业有限公司', '贸易商', '李果商', '13800000202', '云南省昆明市', '水果集散中心2号', 20, 1, '热带水果供应商', '2026-04-19 02:24:58', '2026-04-19 02:24:58');
INSERT INTO `supplier` VALUES (9, 'SUP-20260419104529624', '三里庵农业大棚', '产地直供', '李广', '15685451285', '安徽合肥', '安徽省合肥市蜀山区三里庵', 0, 1, NULL, '2026-04-19 02:45:29', '2026-04-19 02:45:29');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户编号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '负责仓库ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_code`(`user_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_sys_user_role_status`(`role_code` ASC, `status` ASC) USING BTREE,
  INDEX `fk_sys_user_warehouse`(`warehouse_id` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'U-202604200001', 'admin', '$2a$10$BdOdW0ddWawa3Pt4mPM2Mu7WzzYOhhYy/WlPHN63W7qecZitVbTMW', '系统管理员', '13800000000', 'ADMIN', NULL, 1, '系统初始化管理员', '2026-04-19 20:16:34', '2026-04-19 20:16:34');
INSERT INTO `sys_user` VALUES (15, 'USER-20260420162237037', 'cangku', '$2a$10$cRss1SgGnErH.Cfe0TWT6ujm7bxAJBjZNlr2HfgSCdCcOdYQXeSZa', '小仓', '15234569876', 'WAREHOUSE_ADMIN', 11, 1, NULL, '2026-04-20 08:22:37', '2026-04-20 08:22:37');

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `warehouse_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库类型',
  `manager_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_code`(`warehouse_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_warehouse_name`(`warehouse_name` ASC) USING BTREE,
  INDEX `idx_warehouse_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warehouse
-- ----------------------------
INSERT INTO `warehouse` VALUES (1, 'WH-202604190001', '一号综合仓', '综合仓', '王仓管', '13800000011', '山东省寿光市农产品园区1号', 10, 1, '主要承接蔬菜类商品', '2026-04-19 01:02:13', '2026-04-19 01:02:13');
INSERT INTO `warehouse` VALUES (2, 'WH-202604190002', '冷链中心仓', '冷藏仓', '李冷链', '13800000012', '山东省寿光市冷链物流园2号', 20, 1, '主要承接冷藏冷链商品', '2026-04-19 01:02:13', '2026-04-19 01:02:13');
INSERT INTO `warehouse` VALUES (11, 'WH-20260419093735433', '三里庵一号仓', '综合仓', '小王', '15245851252', '安徽省合肥市蜀山区三里庵XXX', 0, 1, NULL, '2026-04-19 01:37:35', '2026-04-19 01:37:35');
INSERT INTO `warehouse` VALUES (34, 'WH-20260420051525680', '王岗一号仓库', '综合仓', '王立', '18685415685', '安徽省合肥市王岗', 0, 1, NULL, '2026-04-19 21:15:25', '2026-04-19 21:15:25');

-- ----------------------------
-- Table structure for warehouse_location
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_location`;
CREATE TABLE `warehouse_location`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库位ID',
  `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位编号',
  `warehouse_id` bigint NOT NULL COMMENT '所属仓库ID',
  `zone_id` bigint NOT NULL COMMENT '所属库区ID',
  `location_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位名称',
  `location_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位类型',
  `capacity` int NULL DEFAULT NULL COMMENT '容量上限',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_location_code`(`location_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_warehouse_location_name`(`zone_id` ASC, `location_name` ASC) USING BTREE,
  INDEX `idx_warehouse_location_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_warehouse_location_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_warehouse_location_zone`(`zone_id` ASC) USING BTREE,
  CONSTRAINT `fk_warehouse_location_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_warehouse_location_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库库位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warehouse_location
-- ----------------------------
INSERT INTO `warehouse_location` VALUES (1, 'LOC-202604190001', 1, 1, 'A-01-01', '货架位', 50, 10, 1, '蔬菜收货暂存位', '2026-04-19 01:22:24', '2026-04-19 01:22:24');
INSERT INTO `warehouse_location` VALUES (2, 'LOC-202604190002', 2, 2, 'B-02-01', '托盘位', 24, 20, 1, '冷藏商品托盘位', '2026-04-19 01:22:24', '2026-04-19 01:22:24');
INSERT INTO `warehouse_location` VALUES (9, 'LOC-20260419100042806', 11, 9, '冷藏库区1-1', '货架位', 4, 0, 1, NULL, '2026-04-19 02:00:42', '2026-04-19 02:00:42');
INSERT INTO `warehouse_location` VALUES (32, 'LOC-20260420051620465', 34, 34, '冷链库区1-1库位', '货架位', 199, 0, 1, NULL, '2026-04-19 21:16:20', '2026-04-19 21:16:20');

-- ----------------------------
-- Table structure for warehouse_zone
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_zone`;
CREATE TABLE `warehouse_zone`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库区ID',
  `zone_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区编号',
  `warehouse_id` bigint NOT NULL COMMENT '所属仓库ID',
  `zone_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区名称',
  `zone_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区类型',
  `temperature_min` decimal(6, 2) NULL DEFAULT NULL COMMENT '最低温度',
  `temperature_max` decimal(6, 2) NULL DEFAULT NULL COMMENT '最高温度',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_zone_code`(`zone_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_warehouse_zone_name`(`warehouse_id` ASC, `zone_name` ASC) USING BTREE,
  INDEX `idx_warehouse_zone_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_warehouse_zone_warehouse`(`warehouse_id` ASC) USING BTREE,
  CONSTRAINT `fk_warehouse_zone_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库库区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warehouse_zone
-- ----------------------------
INSERT INTO `warehouse_zone` VALUES (1, 'ZONE-202604190001', 1, '蔬菜收货区', '收货区', NULL, NULL, 10, 1, '用于蔬菜收货和临时复核', '2026-04-19 01:11:06', '2026-04-19 01:11:06');
INSERT INTO `warehouse_zone` VALUES (2, 'ZONE-202604190002', 2, '冷藏存储区', '冷藏区', 2.00, 8.00, 20, 1, '用于叶菜和冷链水果存放', '2026-04-19 01:11:06', '2026-04-19 01:11:06');
INSERT INTO `warehouse_zone` VALUES (9, 'ZONE-20260419094454480', 11, '冷藏一号库区', '冷藏区', -10.00, -1.00, 0, 1, NULL, '2026-04-19 01:44:54', '2026-04-19 01:44:54');
INSERT INTO `warehouse_zone` VALUES (34, 'ZONE-20260420051549133', 34, '冷链一号库区', '冷藏区', -20.00, 0.00, 0, 1, NULL, '2026-04-19 21:15:49', '2026-04-19 21:15:49');

SET FOREIGN_KEY_CHECKS = 1;
