import { request } from '../../api/http'

import type {
  ProductArchiveDetail,
  ProductArchiveFormPayload,
  ProductArchiveListItem,
  ProductArchiveListQuery,
} from './types'

export function getProductArchiveList(query?: ProductArchiveListQuery) {
  return request<ProductArchiveListItem[]>({
    method: 'GET',
    url: '/product-archive/list',
    params: query,
  })
}

export function getProductArchiveDetail(productArchiveId: string) {
  return request<ProductArchiveDetail>({
    method: 'GET',
    url: `/product-archive/${productArchiveId}`,
  })
}

export function createProductArchive(payload: ProductArchiveFormPayload) {
  return request<ProductArchiveDetail>({
    method: 'POST',
    url: '/product-archive',
    data: payload,
  })
}

export function updateProductArchive(
  productArchiveId: string,
  payload: ProductArchiveFormPayload,
) {
  return request<ProductArchiveDetail>({
    method: 'PUT',
    url: `/product-archive/${productArchiveId}`,
    data: payload,
  })
}

export function updateProductArchiveStatus(productArchiveId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/product-archive/${productArchiveId}/status`,
    data: { status },
  })
}

export function deleteProductArchive(productArchiveId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/product-archive/${productArchiveId}`,
  })
}
