export interface ProductOriginListItem {
  id: string
  originCode: string
  originName: string
  countryName: string
  provinceName: string
  cityName: string | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type ProductOriginDetail = ProductOriginListItem

export interface ProductOriginOption {
  id: string
  label: string
  locationText: string
  status: number
}

export interface ProductOriginListQuery {
  originCode?: string
  originName?: string
  provinceName?: string
  status?: number
}

export interface ProductOriginFormPayload {
  originName: string
  countryName: string
  provinceName: string
  cityName: string | null
  status: number
  sortOrder: number
  remarks: string | null
}
