export interface LossRecordListItem {
  id: string
  lossCode: string
  sourceType: string
  sourceId: string | null
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
  quantity: number
  lossReason: string
  remarks: string | null
  createdAt: string
}

export type LossRecordDetail = LossRecordListItem

export interface LossRecordListQuery {
  lossCode?: string
  sourceType?: string
  warehouseId?: string
  productId?: string
}

export interface LossRecordDirectPayload {
  warehouseId: number
  zoneId: number
  locationId: number
  productId: number
  quantity: number
  lossReason: string
  remarks: string | null
}
