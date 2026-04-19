import { request } from '../../api/http'

import type {
  WarehouseDetail,
  WarehouseFormPayload,
  WarehouseListItem,
  WarehouseListQuery,
  WarehouseOption,
} from './types'

export function getWarehouseList(query?: WarehouseListQuery) {
  return request<WarehouseListItem[]>({
    method: 'GET',
    url: '/warehouse/list',
    params: query,
  })
}

export function getWarehouseOptions() {
  return request<WarehouseOption[]>({
    method: 'GET',
    url: '/warehouse/options',
  })
}

export function getWarehouseDetail(warehouseId: string) {
  return request<WarehouseDetail>({
    method: 'GET',
    url: `/warehouse/${warehouseId}`,
  })
}

export function createWarehouse(payload: WarehouseFormPayload) {
  return request<WarehouseDetail>({
    method: 'POST',
    url: '/warehouse',
    data: payload,
  })
}

export function updateWarehouse(warehouseId: string, payload: WarehouseFormPayload) {
  return request<WarehouseDetail>({
    method: 'PUT',
    url: `/warehouse/${warehouseId}`,
    data: payload,
  })
}

export function updateWarehouseStatus(warehouseId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/warehouse/${warehouseId}/status`,
    data: { status },
  })
}

export function deleteWarehouse(warehouseId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/warehouse/${warehouseId}`,
  })
}
