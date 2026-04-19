import { request } from '../../api/http'

import type { InventoryStockListItem, InventoryStockListQuery } from './types'

export function getInventoryStockList(query?: InventoryStockListQuery) {
  return request<InventoryStockListItem[]>({
    method: 'GET',
    url: '/inventory-stock/list',
    params: query,
  })
}
