export interface SupplierListItem {
  id: string
  supplierCode: string
  supplierName: string
  supplierType: string
  contactName: string | null
  contactPhone: string | null
  regionName: string | null
  address: string | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type SupplierDetail = SupplierListItem

export interface SupplierOption {
  id: string
  label: string
  supplierType: string
  status: number
}

export interface SupplierListQuery {
  supplierCode?: string
  supplierName?: string
  contactName?: string
  status?: number
}

export interface SupplierFormPayload {
  supplierName: string
  supplierType: string
  contactName: string | null
  contactPhone: string | null
  regionName: string | null
  address: string | null
  status: number
  sortOrder: number
  remarks: string | null
}
