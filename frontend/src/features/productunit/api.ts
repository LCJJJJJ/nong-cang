import { request } from '../../api/http'

import type {
  ProductUnitDetail,
  ProductUnitFormPayload,
  ProductUnitListItem,
  ProductUnitListQuery,
  ProductUnitOption,
} from './types'

export function getProductUnitList(query?: ProductUnitListQuery) {
  return request<ProductUnitListItem[]>({
    method: 'GET',
    url: '/product-unit/list',
    params: query,
  })
}

export function getProductUnitOptions() {
  return request<ProductUnitOption[]>({
    method: 'GET',
    url: '/product-unit/options',
  })
}

export function getProductUnitDetail(productUnitId: string) {
  return request<ProductUnitDetail>({
    method: 'GET',
    url: `/product-unit/${productUnitId}`,
  })
}

export function createProductUnit(payload: ProductUnitFormPayload) {
  return request<ProductUnitDetail>({
    method: 'POST',
    url: '/product-unit',
    data: payload,
  })
}

export function updateProductUnit(
  productUnitId: string,
  payload: ProductUnitFormPayload,
) {
  return request<ProductUnitDetail>({
    method: 'PUT',
    url: `/product-unit/${productUnitId}`,
    data: payload,
  })
}

export function updateProductUnitStatus(productUnitId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/product-unit/${productUnitId}/status`,
    data: { status },
  })
}

export function deleteProductUnit(productUnitId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/product-unit/${productUnitId}`,
  })
}
