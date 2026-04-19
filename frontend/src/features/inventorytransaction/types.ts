export interface InventoryTransactionListItem {
  id: string
  transactionCode: string
  transactionType: string
  productId: string
  productCode: string
  productName: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  quantity: number
  sourceType: string
  sourceId: string
  occurredAt: string
  remarks: string | null
  createdAt: string
}

export interface InventoryTransactionListQuery {
  transactionCode?: string
  warehouseId?: string
  productId?: string
  transactionType?: string
}
