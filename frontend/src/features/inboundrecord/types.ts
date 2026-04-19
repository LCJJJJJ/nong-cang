export interface InboundRecordListItem {
  id: string
  recordCode: string
  inboundOrderId: string
  inboundOrderCode: string
  putawayTaskId: string
  supplierId: string
  supplierName: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  productId: string
  productCode: string
  productName: string
  quantity: number
  occurredAt: string
  remarks: string | null
  createdAt: string
}

export interface InboundRecordListQuery {
  recordCode?: string
  orderCode?: string
  warehouseId?: string
  productId?: string
}
