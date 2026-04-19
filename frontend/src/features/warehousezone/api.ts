import { request } from '../../api/http'

import type {
  WarehouseZoneDetail,
  WarehouseZoneFormPayload,
  WarehouseZoneListItem,
  WarehouseZoneListQuery,
  WarehouseZoneOption,
} from './types'

export function getWarehouseZoneList(query?: WarehouseZoneListQuery) {
  return request<WarehouseZoneListItem[]>({
    method: 'GET',
    url: '/warehouse-zone/list',
    params: query,
  })
}

export function getWarehouseZoneOptions() {
  return request<WarehouseZoneOption[]>({
    method: 'GET',
    url: '/warehouse-zone/options',
  })
}

export function getWarehouseZoneDetail(warehouseZoneId: string) {
  return request<WarehouseZoneDetail>({
    method: 'GET',
    url: `/warehouse-zone/${warehouseZoneId}`,
  })
}

export function createWarehouseZone(payload: WarehouseZoneFormPayload) {
  return request<WarehouseZoneDetail>({
    method: 'POST',
    url: '/warehouse-zone',
    data: payload,
  })
}

export function updateWarehouseZone(
  warehouseZoneId: string,
  payload: WarehouseZoneFormPayload,
) {
  return request<WarehouseZoneDetail>({
    method: 'PUT',
    url: `/warehouse-zone/${warehouseZoneId}`,
    data: payload,
  })
}

export function updateWarehouseZoneStatus(warehouseZoneId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/warehouse-zone/${warehouseZoneId}/status`,
    data: { status },
  })
}

export function deleteWarehouseZone(warehouseZoneId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/warehouse-zone/${warehouseZoneId}`,
  })
}
