import { request } from '../../api/http'

import type {
  ProductOriginDetail,
  ProductOriginFormPayload,
  ProductOriginListItem,
  ProductOriginListQuery,
  ProductOriginOption,
} from './types'

export function getProductOriginList(query?: ProductOriginListQuery) {
  return request<ProductOriginListItem[]>({
    method: 'GET',
    url: '/product-origin/list',
    params: query,
  })
}

export function getProductOriginOptions() {
  return request<ProductOriginOption[]>({
    method: 'GET',
    url: '/product-origin/options',
  })
}

export function getProductOriginDetail(productOriginId: string) {
  return request<ProductOriginDetail>({
    method: 'GET',
    url: `/product-origin/${productOriginId}`,
  })
}

export function createProductOrigin(payload: ProductOriginFormPayload) {
  return request<ProductOriginDetail>({
    method: 'POST',
    url: '/product-origin',
    data: payload,
  })
}

export function updateProductOrigin(
  productOriginId: string,
  payload: ProductOriginFormPayload,
) {
  return request<ProductOriginDetail>({
    method: 'PUT',
    url: `/product-origin/${productOriginId}`,
    data: payload,
  })
}

export function updateProductOriginStatus(productOriginId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/product-origin/${productOriginId}/status`,
    data: { status },
  })
}

export function deleteProductOrigin(productOriginId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/product-origin/${productOriginId}`,
  })
}
