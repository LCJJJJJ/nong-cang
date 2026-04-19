import { request } from '../../api/http'

import type {
  AlertRefreshResult,
  AlertRuleDetail,
  AlertRuleListItem,
  AlertRuleListQuery,
  AlertRuleUpdatePayload,
} from './types'

export function getAlertRuleList(query?: AlertRuleListQuery) {
  return request<AlertRuleListItem[]>({
    method: 'GET',
    url: '/alert-rule/list',
    params: query,
  })
}

export function getAlertRuleDetail(ruleId: string) {
  return request<AlertRuleDetail>({
    method: 'GET',
    url: `/alert-rule/${ruleId}`,
  })
}

export function updateAlertRule(ruleId: string, payload: AlertRuleUpdatePayload) {
  return request<AlertRuleDetail>({
    method: 'PUT',
    url: `/alert-rule/${ruleId}`,
    data: payload,
  })
}

export function updateAlertRuleStatus(ruleId: string, enabled: number) {
  return request<void>({
    method: 'PATCH',
    url: `/alert-rule/${ruleId}/status`,
    data: { enabled },
  })
}

export function refreshAlerts() {
  return request<AlertRefreshResult>({
    method: 'POST',
    url: '/alert-record/refresh',
  })
}
