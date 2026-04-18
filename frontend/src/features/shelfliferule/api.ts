import { request } from '../../api/http'

import type {
  ShelfLifeRuleDetail,
  ShelfLifeRuleFormPayload,
  ShelfLifeRuleListItem,
  ShelfLifeRuleListQuery,
  ShelfLifeRuleOption,
} from './types'

export function getShelfLifeRuleList(query?: ShelfLifeRuleListQuery) {
  return request<ShelfLifeRuleListItem[]>({
    method: 'GET',
    url: '/shelf-life-rule/list',
    params: query,
  })
}

export function getShelfLifeRuleOptions() {
  return request<ShelfLifeRuleOption[]>({
    method: 'GET',
    url: '/shelf-life-rule/options',
  })
}

export function getShelfLifeRuleDetail(shelfLifeRuleId: string) {
  return request<ShelfLifeRuleDetail>({
    method: 'GET',
    url: `/shelf-life-rule/${shelfLifeRuleId}`,
  })
}

export function createShelfLifeRule(payload: ShelfLifeRuleFormPayload) {
  return request<ShelfLifeRuleDetail>({
    method: 'POST',
    url: '/shelf-life-rule',
    data: payload,
  })
}

export function updateShelfLifeRule(
  shelfLifeRuleId: string,
  payload: ShelfLifeRuleFormPayload,
) {
  return request<ShelfLifeRuleDetail>({
    method: 'PUT',
    url: `/shelf-life-rule/${shelfLifeRuleId}`,
    data: payload,
  })
}

export function updateShelfLifeRuleStatus(shelfLifeRuleId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/shelf-life-rule/${shelfLifeRuleId}/status`,
    data: { status },
  })
}

export function deleteShelfLifeRule(shelfLifeRuleId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/shelf-life-rule/${shelfLifeRuleId}`,
  })
}
