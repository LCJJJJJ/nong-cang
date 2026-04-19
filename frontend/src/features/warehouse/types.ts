export interface WarehouseListItem {
  id: string
  warehouseCode: string
  warehouseName: string
  warehouseType: string
  managerName: string | null
  contactPhone: string | null
  address: string | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type WarehouseDetail = WarehouseListItem

export interface WarehouseOption {
  id: string
  label: string
  warehouseType: string
  status: number
}

export interface WarehouseListQuery {
  warehouseCode?: string
  warehouseName?: string
  warehouseType?: string
  status?: number
}

export interface WarehouseFormPayload {
  warehouseName: string
  warehouseType: string
  managerName: string | null
  contactPhone: string | null
  address: string | null
  status: number
  sortOrder: number
  remarks: string | null
}
