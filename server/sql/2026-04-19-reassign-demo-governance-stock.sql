USE `nong-cang`;

UPDATE `inventory_adjustment`
SET `warehouse_id` = 1,
    `zone_id` = 1,
    `location_id` = 1,
    `product_id` = 1,
    `quantity` = 20.000
WHERE `adjustment_code` = 'ADJ-202604190001';

UPDATE `inventory_stocktaking_order`
SET `warehouse_id` = 1,
    `zone_id` = 1
WHERE `stocktaking_code` = 'STK-202604190001';

UPDATE `inventory_stocktaking_item`
SET `warehouse_id` = 1,
    `zone_id` = 1,
    `location_id` = 1,
    `product_id` = 1,
    `system_quantity` = 20.000,
    `counted_quantity` = 18.000,
    `difference_quantity` = -2.000
WHERE `stocktaking_order_id` = (
  SELECT `id` FROM `inventory_stocktaking_order` WHERE `stocktaking_code` = 'STK-202604190001'
);

UPDATE `inventory_transaction`
SET `product_id` = 1,
    `warehouse_id` = 1,
    `zone_id` = 1,
    `location_id` = 1,
    `quantity` = 20.000
WHERE `transaction_code` = 'INVTX-202604190004';

UPDATE `inventory_transaction`
SET `product_id` = 1,
    `warehouse_id` = 1,
    `zone_id` = 1,
    `location_id` = 1,
    `quantity` = -2.000
WHERE `transaction_code` = 'INVTX-202604190005';

INSERT INTO `inventory_stock` (
  `product_id`,
  `warehouse_id`,
  `zone_id`,
  `location_id`,
  `quantity`
)
VALUES (1, 1, 1, 1, 18.000)
ON DUPLICATE KEY UPDATE
  `quantity` = VALUES(`quantity`);

UPDATE `inventory_stock`
SET `quantity` = 45.000
WHERE `product_id` = 12
  AND `warehouse_id` = 11
  AND `zone_id` = 9
  AND `location_id` = 9;
