USE `nong-cang`;

ALTER TABLE `product_archive`
  ADD COLUMN `shelf_life_days` INT NULL COMMENT '保质期天数' AFTER `storage_condition_id`,
  ADD COLUMN `warning_days` INT NULL COMMENT '预警提前天数' AFTER `shelf_life_days`;

UPDATE `product_archive` pa
JOIN `shelf_life_rule` slr
  ON slr.`id` = pa.`shelf_life_rule_id`
SET
  pa.`shelf_life_days` = slr.`shelf_life_days`,
  pa.`warning_days` = slr.`warning_days`
WHERE pa.`shelf_life_days` IS NULL
   OR pa.`warning_days` IS NULL;

ALTER TABLE `product_archive`
  DROP FOREIGN KEY `fk_product_archive_shelf_life_rule`;

ALTER TABLE `product_archive`
  DROP INDEX `idx_product_archive_shelf_life_rule`;

ALTER TABLE `product_archive`
  DROP COLUMN `shelf_life_rule_id`;

ALTER TABLE `product_archive`
  MODIFY COLUMN `shelf_life_days` INT NOT NULL COMMENT '保质期天数',
  MODIFY COLUMN `warning_days` INT NOT NULL DEFAULT 0 COMMENT '预警提前天数';

DROP TABLE `shelf_life_rule`;
