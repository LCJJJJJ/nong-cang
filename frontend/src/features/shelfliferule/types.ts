export interface ShelfLifeRuleListItem {
  id: string
  ruleCode: string
  ruleName: string
  categoryId: string | null
  categoryName: string | null
  storageConditionId: string | null
  storageConditionName: string | null
  storageType: string | null
  shelfLifeDays: number
  warningDays: number
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type ShelfLifeRuleDetail = ShelfLifeRuleListItem

export interface ShelfLifeRuleOption {
  id: string
  label: string
  shelfLifeDays: number
  warningDays: number
  status: number
}

export interface ShelfLifeRuleListQuery {
  ruleCode?: string
  ruleName?: string
  categoryId?: string
  status?: number
}

export interface ShelfLifeRuleFormPayload {
  ruleName: string
  categoryId: number | null
  storageConditionId: number | null
  shelfLifeDays: number
  warningDays: number
  status: number
  sortOrder: number
  remarks: string | null
}
