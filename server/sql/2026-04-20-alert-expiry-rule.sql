USE `nong-cang`;

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
