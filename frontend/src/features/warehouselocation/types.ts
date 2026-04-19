export interface WarehouseLocationListItem {
  id: string
  locationCode: string
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationName: string
  locationType: string
  capacity: number | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type WarehouseLocationDetail = WarehouseLocationListItem

export interface WarehouseLocationOption {
  id: string
  label: string
  zoneName: string
  status: number
}

export interface WarehouseLocationListQuery {
  locationCode?: string
  locationName?: string
  warehouseId?: string
  zoneId?: string
  status?: number
}

export interface WarehouseLocationFormPayload {
  warehouseId: number
  zoneId: number
  locationName: string
  locationType: string
  capacity: number | null
  status: number
  sortOrder: number
  remarks: string | null
}
