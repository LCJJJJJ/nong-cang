export interface AlertRuleListItem {
  id: string
  ruleCode: string
  ruleName: string
  alertType: string
  severity: string
  thresholdValue: number
  thresholdUnit: string
  enabled: number
  enabledLabel: string
  description: string | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export type AlertRuleDetail = AlertRuleListItem

export interface AlertRuleListQuery {
  ruleCode?: string
  ruleName?: string
  enabled?: number
}

export interface AlertRuleUpdatePayload {
  severity: string
  thresholdValue: number
  thresholdUnit: string
  description: string | null
  sortOrder: number
}

export interface AlertRefreshResult {
  createdCount: number
  resolvedCount: number
  activeCount: number
  ignoredCount: number
}
