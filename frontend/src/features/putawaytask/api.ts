import { request } from '../../api/http'

import type {
  PutawayAssignPayload,
  PutawayTaskDetail,
  PutawayTaskListItem,
  PutawayTaskListQuery,
} from './types'

export function getPutawayTaskList(query?: PutawayTaskListQuery) {
  return request<PutawayTaskListItem[]>({
    method: 'GET',
    url: '/putaway-task/list',
    params: query,
  })
}

export function getPutawayTaskDetail(taskId: string) {
  return request<PutawayTaskDetail>({
    method: 'GET',
    url: `/putaway-task/${taskId}`,
  })
}

export function assignPutawayTask(taskId: string, payload: PutawayAssignPayload) {
  return request<PutawayTaskDetail>({
    method: 'PATCH',
    url: `/putaway-task/${taskId}/assign`,
    data: payload,
  })
}

export function completePutawayTask(taskId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/putaway-task/${taskId}/complete`,
  })
}

export function cancelPutawayTask(taskId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/putaway-task/${taskId}/cancel`,
  })
}
