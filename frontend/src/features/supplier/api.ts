import { request } from '../../api/http'

import type {
  SupplierDetail,
  SupplierFormPayload,
  SupplierListItem,
  SupplierListQuery,
  SupplierOption,
} from './types'

export function getSupplierList(query?: SupplierListQuery) {
  return request<SupplierListItem[]>({
    method: 'GET',
    url: '/supplier/list',
    params: query,
  })
}

export function getSupplierOptions() {
  return request<SupplierOption[]>({
    method: 'GET',
    url: '/supplier/options',
  })
}

export function getSupplierDetail(supplierId: string) {
  return request<SupplierDetail>({
    method: 'GET',
    url: `/supplier/${supplierId}`,
  })
}

export function createSupplier(payload: SupplierFormPayload) {
  return request<SupplierDetail>({
    method: 'POST',
    url: '/supplier',
    data: payload,
  })
}

export function updateSupplier(supplierId: string, payload: SupplierFormPayload) {
  return request<SupplierDetail>({
    method: 'PUT',
    url: `/supplier/${supplierId}`,
    data: payload,
  })
}

export function updateSupplierStatus(supplierId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/supplier/${supplierId}/status`,
    data: { status },
  })
}

export function deleteSupplier(supplierId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/supplier/${supplierId}`,
  })
}
