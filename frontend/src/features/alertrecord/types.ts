export interface AlertRecordListItem {
  id: string
  alertCode: string
  ruleId: string
  ruleCode: string
  alertType: string
  severity: string
  sourceType: string
  sourceId: string
  sourceCode: string
  title: string
  content: string
  status: number
  statusLabel: string
  occurredAt: string
  resolvedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AlertRecordListQuery {
  alertCode?: string
  alertType?: string
  severity?: string
  status?: number
}

export interface AlertRefreshResult {
  createdCount: number
  resolvedCount: number
  activeCount: number
  ignoredCount: number
}
