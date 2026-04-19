export interface PutawayTaskListItem {
  id: string
  taskCode: string
  inboundOrderId: string
  inboundOrderCode: string
  supplierId: string
  supplierName: string
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
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export type PutawayTaskDetail = PutawayTaskListItem

export interface PutawayTaskListQuery {
  taskCode?: string
  warehouseId?: string
  status?: number
}

export interface PutawayAssignPayload {
  zoneId: number
  locationId: number
}
