export interface WarehouseZoneListItem {
  id: string
  zoneCode: string
  warehouseId: string
  warehouseName: string
  zoneName: string
  zoneType: string
  temperatureMin: number | null
  temperatureMax: number | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type WarehouseZoneDetail = WarehouseZoneListItem

export interface WarehouseZoneOption {
  id: string
  label: string
  warehouseName: string
  status: number
}

export interface WarehouseZoneListQuery {
  zoneCode?: string
  zoneName?: string
  warehouseId?: string
  status?: number
}

export interface WarehouseZoneFormPayload {
  warehouseId: number
  zoneName: string
  zoneType: string
  temperatureMin: number | null
  temperatureMax: number | null
  status: number
  sortOrder: number
  remarks: string | null
}
