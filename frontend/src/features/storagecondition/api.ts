import { request } from '../../api/http'

import type {
  StorageConditionDetail,
  StorageConditionFormPayload,
  StorageConditionListItem,
  StorageConditionListQuery,
  StorageConditionOption,
} from './types'

export function getStorageConditionList(query?: StorageConditionListQuery) {
  return request<StorageConditionListItem[]>({
    method: 'GET',
    url: '/storage-condition/list',
    params: query,
  })
}

export function getStorageConditionOptions() {
  return request<StorageConditionOption[]>({
    method: 'GET',
    url: '/storage-condition/options',
  })
}

export function getStorageConditionDetail(storageConditionId: string) {
  return request<StorageConditionDetail>({
    method: 'GET',
    url: `/storage-condition/${storageConditionId}`,
  })
}

export function createStorageCondition(payload: StorageConditionFormPayload) {
  return request<StorageConditionDetail>({
    method: 'POST',
    url: '/storage-condition',
    data: payload,
  })
}

export function updateStorageCondition(
  storageConditionId: string,
  payload: StorageConditionFormPayload,
) {
  return request<StorageConditionDetail>({
    method: 'PUT',
    url: `/storage-condition/${storageConditionId}`,
    data: payload,
  })
}

export function updateStorageConditionStatus(
  storageConditionId: string,
  status: number,
) {
  return request<void>({
    method: 'PATCH',
    url: `/storage-condition/${storageConditionId}/status`,
    data: { status },
  })
}

export function deleteStorageCondition(storageConditionId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/storage-condition/${storageConditionId}`,
  })
}
