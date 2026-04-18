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
  defaultStorageConditionId: string | null
  defaultStorageType: string | null
  defaultStorageCondition: string | null
  shelfLifeDays: number | null
  warningDays: number | null
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
  defaultStorageConditionId: string | null
  defaultStorageType: string | null
  defaultStorageCondition: string | null
  shelfLifeDays: number | null
  warningDays: number | null
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
  defaultStorageConditionId: number | null
  shelfLifeDays: number | null
  warningDays: number | null
  remarks: string | null
}
