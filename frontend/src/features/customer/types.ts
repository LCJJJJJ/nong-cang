export interface CustomerListItem {
  id: string
  customerCode: string
  customerName: string
  customerType: string
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

export type CustomerDetail = CustomerListItem

export interface CustomerOption {
  id: string
  label: string
  customerType: string
  status: number
}

export interface CustomerListQuery {
  customerCode?: string
  customerName?: string
  contactName?: string
  status?: number
}

export interface CustomerFormPayload {
  customerName: string
  customerType: string
  contactName: string | null
  contactPhone: string | null
  regionName: string | null
  address: string | null
  status: number
  sortOrder: number
  remarks: string | null
}
