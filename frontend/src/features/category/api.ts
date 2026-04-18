import { request } from '../../api/http'

import type {
  CategoryDetail,
  CategoryFormPayload,
  CategoryOption,
  CategoryTreeItem,
  CategoryTreeQuery,
} from './types'

export function getCategoryTree(query?: CategoryTreeQuery) {
  return request<CategoryTreeItem[]>({
    method: 'GET',
    url: '/category/tree',
    params: query,
  })
}

export function getCategoryOptions() {
  return request<CategoryOption[]>({
    method: 'GET',
    url: '/category/options',
  })
}

export function getCategoryDetail(categoryId: string) {
  return request<CategoryDetail>({
    method: 'GET',
    url: `/category/${categoryId}`,
  })
}

export function createCategory(payload: CategoryFormPayload) {
  return request<CategoryDetail>({
    method: 'POST',
    url: '/category',
    data: payload,
  })
}

export function updateCategory(categoryId: string, payload: CategoryFormPayload) {
  return request<CategoryDetail>({
    method: 'PUT',
    url: `/category/${categoryId}`,
    data: payload,
  })
}

export function updateCategoryStatus(categoryId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/category/${categoryId}/status`,
    data: { status },
  })
}

export function deleteCategory(categoryId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/category/${categoryId}`,
  })
}
