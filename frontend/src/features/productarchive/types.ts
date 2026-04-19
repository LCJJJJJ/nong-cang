export interface ProductArchiveListItem {
  id: string
  productCode: string
  productName: string
  productSpecification: string | null
  categoryId: string
  categoryName: string
  unitId: string
  unitName: string
  unitSymbol: string
  originId: string
  originName: string
  storageConditionId: string
  storageConditionName: string
  shelfLifeDays: number
  warningDays: number
  qualityGradeId: string
  qualityGradeName: string
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type ProductArchiveDetail = ProductArchiveListItem

export interface ProductArchiveOption {
  id: string
  label: string
  unitName: string
  unitSymbol: string
  status: number
}

export interface ProductArchiveListQuery {
  productCode?: string
  productName?: string
  categoryId?: string
  status?: number
}

export interface ProductArchiveFormPayload {
  productName: string
  productSpecification: string | null
  categoryId: number
  unitId: number
  originId: number
  storageConditionId: number
  shelfLifeDays: number
  warningDays: number
  qualityGradeId: number
  status: number
  sortOrder: number
  remarks: string | null
}
