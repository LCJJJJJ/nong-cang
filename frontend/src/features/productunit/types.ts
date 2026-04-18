export interface ProductUnitListItem {
  id: string
  unitCode: string
  unitName: string
  unitSymbol: string
  unitType: string
  precisionDigits: number
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type ProductUnitDetail = ProductUnitListItem

export interface ProductUnitOption {
  id: string
  label: string
  unitSymbol: string
  status: number
}

export interface ProductUnitListQuery {
  unitCode?: string
  unitName?: string
  unitType?: string
  status?: number
}

export interface ProductUnitFormPayload {
  unitName: string
  unitSymbol: string
  unitType: string
  precisionDigits: number
  status: number
  sortOrder: number
  remarks: string | null
}
