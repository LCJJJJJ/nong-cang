export interface OutboundTaskListItem {
  id: string
  taskCode: string
  outboundOrderId: string
  outboundOrderCode: string
  customerId: string
  customerName: string
  warehouseId: string
  warehouseName: string
  zoneId: string | null
  zoneName: string | null
  locationId: string | null
  locationName: string | null
  productId: string
  productCode: string
  productName: string
  quantity: number
  status: number
  statusLabel: string
  remarks: string | null
  pickedAt: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export type OutboundTaskDetail = OutboundTaskListItem

export interface OutboundTaskListQuery {
  taskCode?: string
  warehouseId?: string
  status?: number
}

export interface OutboundTaskStockOption {
  warehouseId: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  stockQuantity: number
  reservedQuantity: number
  availableQuantity: number
}

export interface OutboundAssignPayload {
  zoneId: number
  locationId: number
}
