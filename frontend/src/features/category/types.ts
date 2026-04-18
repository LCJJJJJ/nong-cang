export interface CategoryTreeItem {
  id: string
  categoryCode: string
  categoryName: string
  parentId: string | null
  categoryLevel: number
  ancestorPath: string
  sortOrder: number
  status: number
  statusLabel: string
  defaultStorageType: string | null
  defaultStorageCondition: string | null
  shelfLifeDays: number | null
  warningDays: number | null
  requireQualityCheck: boolean
  remarks: string | null
  createdAt: string
  updatedAt: string
  children: CategoryTreeItem[]
}

export interface CategoryOption {
  id: string
  label: string
  categoryLevel: number
  ancestorPath: string
  children: CategoryOption[]
}

export interface CategoryDetail {
  id: string
  categoryCode: string
  categoryName: string
  parentId: string | null
  parentName: string | null
  categoryLevel: number
  ancestorPath: string
  sortOrder: number
  status: number
  statusLabel: string
  defaultStorageType: string | null
  defaultStorageCondition: string | null
  shelfLifeDays: number | null
  warningDays: number | null
  requireQualityCheck: boolean
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export interface CategoryTreeQuery {
  categoryCode?: string
  categoryName?: string
  parentId?: string
  level?: number
  status?: number
}

export interface CategoryFormPayload {
  categoryName: string
  parentId: number | null
  sortOrder: number
  status: number
  defaultStorageType: string | null
  defaultStorageCondition: string | null
  shelfLifeDays: number | null
  warningDays: number | null
  requireQualityCheck: boolean
  remarks: string | null
}
