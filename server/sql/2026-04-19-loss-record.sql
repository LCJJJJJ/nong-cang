USE `nong-cang`;

CREATE TABLE IF NOT EXISTS `loss_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '损耗记录ID',
  `loss_code` VARCHAR(64) NOT NULL COMMENT '损耗记录编号',
  `source_type` VARCHAR(32) NOT NULL COMMENT '来源类型：ABNORMAL_STOCK/DIRECT',
  `source_id` BIGINT DEFAULT NULL COMMENT '来源ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `zone_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `quantity` DECIMAL(14,3) NOT NULL COMMENT '损耗数量',
  `loss_reason` VARCHAR(64) NOT NULL COMMENT '损耗原因',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_loss_record_code` (`loss_code`),
  KEY `idx_loss_record_product` (`product_id`),
  KEY `idx_loss_record_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_loss_record_product` FOREIGN KEY (`product_id`) REFERENCES `product_archive` (`id`),
  CONSTRAINT `fk_loss_record_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`),
  CONSTRAINT `fk_loss_record_zone` FOREIGN KEY (`zone_id`) REFERENCES `warehouse_zone` (`id`),
  CONSTRAINT `fk_loss_record_location` FOREIGN KEY (`location_id`) REFERENCES `warehouse_location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='损耗记录表';
