export interface InventoryStocktakingListItem {
  id: string
  stocktakingCode: string
  warehouseId: string
  warehouseName: string
  zoneId: string | null
  zoneName: string | null
  status: number
  statusLabel: string
  itemCount: number
  countedItemCount: number
  totalDifferenceQuantity: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export interface InventoryStocktakingItem {
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
  systemQuantity: number
  countedQuantity: number | null
  differenceQuantity: number | null
  remarks: string | null
}

export interface InventoryStocktakingDetail extends InventoryStocktakingListItem {
  items: InventoryStocktakingItem[]
}

export interface InventoryStocktakingListQuery {
  stocktakingCode?: string
  warehouseId?: string
  status?: number
}

export interface InventoryStocktakingCreatePayload {
  warehouseId: number
  zoneId: number | null
  remarks: string | null
}

export interface InventoryStocktakingItemSavePayload {
  itemId: number
  countedQuantity: number
  remarks: string | null
}
