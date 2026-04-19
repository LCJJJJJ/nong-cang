USE `nong-cang`;

UPDATE inbound_order_item ioi
JOIN product_archive pa ON pa.id = ioi.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET ioi.quantity = ROUND(ioi.quantity, pu.precision_digits)
WHERE ioi.quantity <> ROUND(ioi.quantity, pu.precision_digits);

UPDATE inbound_order io
JOIN (
  SELECT inbound_order_id, SUM(quantity) AS total_quantity, COUNT(*) AS total_item_count
  FROM inbound_order_item
  GROUP BY inbound_order_id
) agg ON agg.inbound_order_id = io.id
SET io.total_quantity = agg.total_quantity,
    io.total_item_count = agg.total_item_count;

UPDATE putaway_task pt
JOIN product_archive pa ON pa.id = pt.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET pt.quantity = ROUND(pt.quantity, pu.precision_digits)
WHERE pt.quantity <> ROUND(pt.quantity, pu.precision_digits);

UPDATE inbound_record ir
JOIN product_archive pa ON pa.id = ir.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET ir.quantity = ROUND(ir.quantity, pu.precision_digits)
WHERE ir.quantity <> ROUND(ir.quantity, pu.precision_digits);

UPDATE outbound_order_item ooi
JOIN product_archive pa ON pa.id = ooi.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET ooi.quantity = ROUND(ooi.quantity, pu.precision_digits)
WHERE ooi.quantity <> ROUND(ooi.quantity, pu.precision_digits);

UPDATE outbound_order oo
JOIN (
  SELECT outbound_order_id, SUM(quantity) AS total_quantity, COUNT(*) AS total_item_count
  FROM outbound_order_item
  GROUP BY outbound_order_id
) agg ON agg.outbound_order_id = oo.id
SET oo.total_quantity = agg.total_quantity,
    oo.total_item_count = agg.total_item_count;

UPDATE outbound_task ot
JOIN product_archive pa ON pa.id = ot.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET ot.quantity = ROUND(ot.quantity, pu.precision_digits)
WHERE ot.quantity <> ROUND(ot.quantity, pu.precision_digits);

UPDATE outbound_record orc
JOIN product_archive pa ON pa.id = orc.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET orc.quantity = ROUND(orc.quantity, pu.precision_digits)
WHERE orc.quantity <> ROUND(orc.quantity, pu.precision_digits);

UPDATE inventory_adjustment ia
JOIN product_archive pa ON pa.id = ia.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET ia.quantity = ROUND(ia.quantity, pu.precision_digits)
WHERE ia.quantity <> ROUND(ia.quantity, pu.precision_digits);

UPDATE inventory_stocktaking_item si
JOIN product_archive pa ON pa.id = si.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET si.system_quantity = ROUND(si.system_quantity, pu.precision_digits),
    si.counted_quantity = CASE
      WHEN si.counted_quantity IS NULL THEN NULL
      ELSE ROUND(si.counted_quantity, pu.precision_digits)
    END,
    si.difference_quantity = CASE
      WHEN si.counted_quantity IS NULL THEN NULL
      ELSE ROUND(si.counted_quantity, pu.precision_digits) - ROUND(si.system_quantity, pu.precision_digits)
    END
WHERE si.system_quantity <> ROUND(si.system_quantity, pu.precision_digits)
   OR (si.counted_quantity IS NOT NULL AND si.counted_quantity <> ROUND(si.counted_quantity, pu.precision_digits))
   OR (si.difference_quantity IS NOT NULL AND si.counted_quantity IS NOT NULL
       AND si.difference_quantity <> (ROUND(si.counted_quantity, pu.precision_digits) - ROUND(si.system_quantity, pu.precision_digits)));

UPDATE inventory_transaction it
JOIN product_archive pa ON pa.id = it.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET it.quantity = SIGN(it.quantity) * ROUND(ABS(it.quantity), pu.precision_digits)
WHERE ABS(it.quantity) <> ROUND(ABS(it.quantity), pu.precision_digits);

UPDATE inventory_stock s
JOIN product_archive pa ON pa.id = s.product_id
JOIN product_unit pu ON pu.id = pa.unit_id
SET s.quantity = ROUND(s.quantity, pu.precision_digits)
WHERE s.quantity <> ROUND(s.quantity, pu.precision_digits);
