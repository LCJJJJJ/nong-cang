export interface InventoryStockListItem {
  id: string
  productId: string
  productCode: string
  productName: string
  productSpecification: string | null
  unitId: string
  unitName: string
  unitSymbol: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  stockQuantity: number
  reservedQuantity: number
  lockedQuantity: number
  availableQuantity: number
  nearestExpireAt: string | null
  remainingShelfLifeDays: number | null
  updatedAt: string
}

export interface InventoryStockListQuery {
  productId?: string
  warehouseId?: string
  zoneId?: string
}
