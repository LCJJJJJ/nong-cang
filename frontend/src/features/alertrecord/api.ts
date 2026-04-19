import { request } from '../../api/http'

import type {
  AlertRecordListItem,
  AlertRecordListQuery,
  AlertRefreshResult,
} from './types'

export function getAlertRecordList(query?: AlertRecordListQuery) {
  return request<AlertRecordListItem[]>({
    method: 'GET',
    url: '/alert-record/list',
    params: query,
  })
}

export function refreshAlertRecords() {
  return request<AlertRefreshResult>({
    method: 'POST',
    url: '/alert-record/refresh',
  })
}

export function ignoreAlertRecord(alertId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/alert-record/${alertId}/ignore`,
  })
}
