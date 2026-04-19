import { request } from '../../api/http'

import type {
  InventoryStocktakingCreatePayload,
  InventoryStocktakingDetail,
  InventoryStocktakingItemSavePayload,
  InventoryStocktakingListItem,
  InventoryStocktakingListQuery,
} from './types'

export function getInventoryStocktakingList(query?: InventoryStocktakingListQuery) {
  return request<InventoryStocktakingListItem[]>({
    method: 'GET',
    url: '/inventory-stocktaking/list',
    params: query,
  })
}

export function getInventoryStocktakingDetail(stocktakingId: string) {
  return request<InventoryStocktakingDetail>({
    method: 'GET',
    url: `/inventory-stocktaking/${stocktakingId}`,
  })
}

export function createInventoryStocktaking(payload: InventoryStocktakingCreatePayload) {
  return request<InventoryStocktakingDetail>({
    method: 'POST',
    url: '/inventory-stocktaking',
    data: payload,
  })
}

export function saveInventoryStocktakingItems(
  stocktakingId: string,
  items: InventoryStocktakingItemSavePayload[],
) {
  return request<InventoryStocktakingDetail>({
    method: 'PUT',
    url: `/inventory-stocktaking/${stocktakingId}/items`,
    data: { items },
  })
}

export function confirmInventoryStocktaking(stocktakingId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/inventory-stocktaking/${stocktakingId}/confirm`,
  })
}

export function cancelInventoryStocktaking(stocktakingId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/inventory-stocktaking/${stocktakingId}/cancel`,
  })
}
