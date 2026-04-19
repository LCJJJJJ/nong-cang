import { request } from '../../api/http'

import type {
  OutboundAssignPayload,
  OutboundTaskDetail,
  OutboundTaskListItem,
  OutboundTaskListQuery,
  OutboundTaskStockOption,
} from './types'

export function getOutboundTaskList(query?: OutboundTaskListQuery) {
  return request<OutboundTaskListItem[]>({
    method: 'GET',
    url: '/outbound-task/list',
    params: query,
  })
}

export function getOutboundTaskDetail(taskId: string) {
  return request<OutboundTaskDetail>({
    method: 'GET',
    url: `/outbound-task/${taskId}`,
  })
}

export function getOutboundTaskStockOptions(taskId: string) {
  return request<OutboundTaskStockOption[]>({
    method: 'GET',
    url: `/outbound-task/${taskId}/stock-options`,
  })
}

export function assignOutboundTask(taskId: string, payload: OutboundAssignPayload) {
  return request<OutboundTaskDetail>({
    method: 'PATCH',
    url: `/outbound-task/${taskId}/assign`,
    data: payload,
  })
}

export function pickOutboundTask(taskId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/outbound-task/${taskId}/pick`,
  })
}

export function completeOutboundTask(taskId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/outbound-task/${taskId}/complete`,
  })
}

export function cancelOutboundTask(taskId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/outbound-task/${taskId}/cancel`,
  })
}
