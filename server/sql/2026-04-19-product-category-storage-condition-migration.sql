USE `nong-cang`;

ALTER TABLE `product_category`
  ADD COLUMN `default_storage_condition_id` BIGINT NULL COMMENT '默认储存条件ID' AFTER `status`;

UPDATE `product_category` pc
SET `default_storage_condition_id` = (
  SELECT sc.`id`
  FROM `storage_condition` sc
  WHERE sc.`condition_name` = '叶菜冷藏标准'
  LIMIT 1
)
WHERE pc.`default_storage_condition_id` IS NULL
  AND pc.`default_storage_type` = '冷藏'
  AND pc.`default_storage_condition` = '2-8°C';

UPDATE `product_category` pc
SET `default_storage_condition_id` = (
  SELECT sc.`id`
  FROM `storage_condition` sc
  WHERE sc.`condition_name` = '根茎阴凉干燥标准'
  LIMIT 1
)
WHERE pc.`default_storage_condition_id` IS NULL
  AND pc.`default_storage_type` = '阴凉干燥'
  AND pc.`default_storage_condition` = '10-15°C';

UPDATE `product_category` pc
SET `default_storage_condition_id` = (
  SELECT sc.`id`
  FROM `storage_condition` sc
  WHERE sc.`condition_name` = '水果冷藏标准'
  LIMIT 1
)
WHERE pc.`default_storage_condition_id` IS NULL
  AND pc.`default_storage_type` = '冷藏'
  AND pc.`default_storage_condition` = '4-8°C';

ALTER TABLE `product_category`
  ADD KEY `idx_product_category_storage_condition` (`default_storage_condition_id`);

ALTER TABLE `product_category`
  ADD CONSTRAINT `fk_product_category_storage_condition`
  FOREIGN KEY (`default_storage_condition_id`) REFERENCES `storage_condition` (`id`);

ALTER TABLE `product_category`
  DROP COLUMN `default_storage_type`,
  DROP COLUMN `default_storage_condition`,
  DROP COLUMN `require_quality_check`;
