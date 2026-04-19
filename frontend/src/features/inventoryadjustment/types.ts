export interface InventoryAdjustmentListItem {
  id: string
  adjustmentCode: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  productId: string
  productCode: string
  productName: string
  adjustmentType: string
  quantity: number
  reason: string
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type InventoryAdjustmentDetail = InventoryAdjustmentListItem

export interface InventoryAdjustmentListQuery {
  adjustmentCode?: string
  warehouseId?: string
  productId?: string
  adjustmentType?: string
}

export interface InventoryAdjustmentFormPayload {
  warehouseId: number
  zoneId: number
  locationId: number
  productId: number
  adjustmentType: string
  quantity: number
  reason: string
  remarks: string | null
}
