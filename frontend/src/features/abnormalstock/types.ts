export interface AbnormalStockListItem {
  id: string
  abnormalCode: string
  qualityInspectionId: string
  inspectionCode: string
  productId: string
  productCode: string
  productName: string
  unitName: string
  unitSymbol: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  lockedQuantity: number
  status: number
  statusLabel: string
  reason: string
  remarks: string | null
  processedAt: string | null
  createdAt: string
  updatedAt: string
}

export type AbnormalStockDetail = AbnormalStockListItem

export interface AbnormalStockListQuery {
  abnormalCode?: string
  productId?: string
  warehouseId?: string
  status?: number
}

export interface AbnormalStockOption {
  id: string
  label: string
  lockedQuantity: number
  status: number
}
