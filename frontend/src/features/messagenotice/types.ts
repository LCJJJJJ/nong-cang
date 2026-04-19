export interface MessageNoticeListItem {
  id: string
  noticeCode: string
  alertRecordId: string | null
  noticeType: string
  severity: string
  title: string
  content: string
  sourceType: string
  sourceId: string
  sourceCode: string
  status: number
  statusLabel: string
  createdAt: string
  readAt: string | null
}

export interface MessageNoticeListQuery {
  noticeCode?: string
  severity?: string
  status?: number
}
