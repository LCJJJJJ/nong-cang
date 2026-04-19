export interface OutboundRecordListItem {
  id: string
  recordCode: string
  outboundOrderId: string
  outboundOrderCode: string
  outboundTaskId: string
  customerId: string
  customerName: string
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

export interface OutboundRecordListQuery {
  recordCode?: string
  orderCode?: string
  warehouseId?: string
  productId?: string
}
