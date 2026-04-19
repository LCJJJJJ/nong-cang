import { request } from '../../api/http'

import type {
  WarehouseLocationDetail,
  WarehouseLocationFormPayload,
  WarehouseLocationListItem,
  WarehouseLocationListQuery,
  WarehouseLocationOption,
} from './types'

export function getWarehouseLocationList(query?: WarehouseLocationListQuery) {
  return request<WarehouseLocationListItem[]>({
    method: 'GET',
    url: '/warehouse-location/list',
    params: query,
  })
}

export function getWarehouseLocationOptions() {
  return request<WarehouseLocationOption[]>({
    method: 'GET',
    url: '/warehouse-location/options',
  })
}

export function getWarehouseLocationDetail(warehouseLocationId: string) {
  return request<WarehouseLocationDetail>({
    method: 'GET',
    url: `/warehouse-location/${warehouseLocationId}`,
  })
}

export function createWarehouseLocation(payload: WarehouseLocationFormPayload) {
  return request<WarehouseLocationDetail>({
    method: 'POST',
    url: '/warehouse-location',
    data: payload,
  })
}

export function updateWarehouseLocation(
  warehouseLocationId: string,
  payload: WarehouseLocationFormPayload,
) {
  return request<WarehouseLocationDetail>({
    method: 'PUT',
    url: `/warehouse-location/${warehouseLocationId}`,
    data: payload,
  })
}

export function updateWarehouseLocationStatus(
  warehouseLocationId: string,
  status: number,
) {
  return request<void>({
    method: 'PATCH',
    url: `/warehouse-location/${warehouseLocationId}/status`,
    data: { status },
  })
}

export function deleteWarehouseLocation(warehouseLocationId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/warehouse-location/${warehouseLocationId}`,
  })
}
