import { request } from '../../api/http'

import type {
  InventoryTransactionListItem,
  InventoryTransactionListQuery,
} from './types'

export function getInventoryTransactionList(query?: InventoryTransactionListQuery) {
  return request<InventoryTransactionListItem[]>({
    method: 'GET',
    url: '/inventory-transaction/list',
    params: query,
  })
}
